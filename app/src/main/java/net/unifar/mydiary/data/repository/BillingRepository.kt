package net.unifar.mydiary.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.util.Logger


// 複数のサブスクリプションの商品IDを定義
const val SUBSCRIPTION_ID = "premium_plan_annual"

private val ALL_SUBSCRIPTION_IDS = listOf(SUBSCRIPTION_ID)

const val TAG_NAME = "BillingRepository"

@Singleton
class BillingRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val backUpCodeRepository: BackUpCodeRepository,
    private val backUpRepository: BackUpRepository,
    private val diaryDao: DiaryDao,
    ioDispatcher: CoroutineDispatcher
) {


    // 注入されたDispatcherとSupervisorJobでリポジトリ専用のスコープを作成
    // SupervisorJobを使うと、子コルーチンが失敗してもスコープ全体はキャンセルされない
    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    // 購入完了時のリスナー
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            // 購入が成功した後の処理
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
            // 購入成功時にエラーをクリア
            _purchaseErrorFlow.value = null
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // ユーザーが購入をキャンセルした場合
            _purchaseErrorFlow.value = PurchaseError.UserCanceled
            Logger.d(TAG_NAME, "Purchase cancelled by user")
        } else {
            // その他のエラー
            val error = when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                BillingClient.BillingResponseCode.NETWORK_ERROR -> PurchaseError.NetworkError
                
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> PurchaseError.ItemUnavailable
                
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> PurchaseError.AlreadyOwned
                
                else -> PurchaseError.Unknown(billingResult.debugMessage)
            }
            _purchaseErrorFlow.value = error
            Logger.e(
                TAG_NAME,
                "Purchase failed. Response code: ${billingResult.responseCode}, Debug message: ${billingResult.debugMessage}"
            )
        }
    }


    val isPremiumFlow = MutableStateFlow(false)

    // 購入エラー状態を管理するFlow
    private val _purchaseErrorFlow = MutableStateFlow<PurchaseError?>(null)
    val purchaseErrorFlow: StateFlow<PurchaseError?> = _purchaseErrorFlow.asStateFlow()

    // クエリ対象とする全ての商品IDのリスト

    private lateinit var billingClient: BillingClient

    // UIに公開するサブスクリプションの詳細情報
    private val _productDetailsList = MutableStateFlow<List<ProductDetails>?>(null)

    // StateFlowをList<ProductDetails>に変更
    val productDetailsList: StateFlow<List<ProductDetails>?> = _productDetailsList.asStateFlow()

    init {
        initializeBillingClient()
        initializePremiumStatus()
    }

    fun setPremium(isPremium: Boolean) {
        isPremiumFlow.value = isPremium
    }

    /**
     * エラーメッセージを消費（クリア）する
     * UIでエラーを表示した後に呼び出す
     */
    fun clearPurchaseError() {
        _purchaseErrorFlow.value = null
    }


    fun initializePremiumStatus() {
        val backUpCode = backUpCodeRepository.backUpCodeFlow.value
        if (backUpCode != null) {
            setPremium(true)
        }
    }

    suspend fun verifySubscriptionStatus() {
        if (!isPremiumFlow.value) {
            return
        }

        if (hasActiveSubscription() == false) {
            backUpCodeRepository.deleteBackUpCode()
            setPremium(false)
        }
    }

    // suspend関数として定義するのが良い
    suspend fun hasActiveSubscription(): Boolean? {
        // 1. クライアントが準備完了か確認
        if (!billingClient.isReady) {
            // 必要に応じて接続処理を入れる
            return null
        }

        // 2. サブスクリプションタイプの購入情報を問い合わせる
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = billingClient.queryPurchasesAsync(params)

        // 3. 結果をチェック
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val purchasesList = result.purchasesList
            // 4. 有効な購入情報があり、かつその状態が「購入済み」であるかを確認
            return purchasesList.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
        } else {
            // エラーハンドリング
            return null
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        // 1. 購入が有効な状態（PURCHASED）であるかを確認
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // 2. まだ承認されていない購入のみを処理
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // 3. 承認に成功したら、プレミアム状態を保存・通知
                        Logger.d("BillingRepository", "Purchase acknowledged successfully.")
                        // ここでDataStoreなどに保存する処理を呼び出す
                        val backupCode =
                            backUpCodeRepository.saveAsBackUpCode(purchase.purchaseToken)
                        backUpCodeRepository.savePurchaseToken(purchase.purchaseToken, backupCode)
                        backUpCodeRepository.setBackUpCodeBySharedPreferences()
                        scope.launch {
                            backUpRepository.backupDiaries(diaryDao, backupCode)
                            backUpRepository.refreshLastBackupDate(backupCode)
                        }
                        // UIに通知
                        setPremium(true)
                    }
                }
            } else {
                val backupCode =
                    backUpCodeRepository.saveAsBackUpCode(purchase.purchaseToken)
                backUpCodeRepository.setBackUpCodeBySharedPreferences()
                scope.launch {
                    backUpRepository.restoreDiaries(diaryDao, backupCode)
                    backUpRepository.refreshLastBackupDate(backupCode)
                }
                // UIに通知
                setPremium(true)
            }
        }
    }


    // コンストラクタやinitブロックで呼び出す
    fun initializeBillingClient() {
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts() // 使わないが、enableにしないとエラーが出る
            .build()

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()

        // Google Playとの接続を開始
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 接続が成功したら、サブスクリプション情報を取得
                    // ここで利用可能な商品情報をクエリする処理を呼ぶ
                    queryProductDetails()
                    scope.launch {
                        verifySubscriptionStatus()
                        restorePurchase()
                    }
                    Logger.d(TAG_NAME, "Billing setup finished successfully.")
                } else {
                    Logger.e(
                        TAG_NAME,
                        "Billing setup failed. Response code: ${billingResult.responseCode}, Debug message: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                // 接続が切断された場合の処理（再接続を試みるなど）
            }
        })
    }

    fun queryProductDetails() {
        val productList = ALL_SUBSCRIPTION_IDS.map { productId ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _productDetailsList.value = productDetailsList.productDetailsList
                Logger.d(TAG_NAME, "Product details retrieved successfully.")
                Logger.d(TAG_NAME, "productDetailsList: ${productDetailsList.productDetailsList}")
                Logger.d(TAG_NAME, "unfetchedProductList: ${productDetailsList.unfetchedProductList}")
                Logger.d(
                    TAG_NAME,
                    "unfetchedProduct Status: ${productDetailsList.unfetchedProductList.first().statusCode}"
                )
            } else {
                Logger.d(
                    TAG_NAME,
                    "Product details retrieval failed. Response code: ${billingResult.responseCode}"
                )
            }
        }
    }

    fun launchPurchaseFlow(
        activity: Activity,
        selectedDetails: ProductDetails,
        offerToken: String
    ) {

        // offerToken が空かチェックするのは堅牢な実装です。
        if (offerToken.isEmpty()) {
            Logger.e("BillingRepository", "Offer token is empty. Cannot launch purchase flow.")
            // ここで例外をスローするか、エラーをUIに通知する仕組みを実装しても良い
            return
        }

        // ★★★ 修正点 ★★★
        // ViewModelから渡された selectedDetails と offerToken を直接使います。
        // リポジトリ内のリストから再検索する必要はありません。
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(selectedDetails) // UIから渡されたProductDetailsを直接使用
                .setOfferToken(offerToken) // UIから渡されたofferTokenを直接使用
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        // 変更なし
        val billingResult = billingClient.launchBillingFlow(activity, billingFlowParams)

        // 起動結果をログに出力するとデバッグに役立ちます
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Logger.e(
                "BillingRepository",
                "Failed to launch billing flow. Response code: ${billingResult.responseCode}, Debug message: ${billingResult.debugMessage}"
            )
        }
    }


    suspend fun restorePurchase(): RestorePurchaseResult {
        // 1. クライアントが準備完了か確認
        if (!billingClient.isReady) {
            return RestorePurchaseResult.Failure
        }

        if (backUpCodeRepository.backUpCodeFlow.value != null) {
            return RestorePurchaseResult.AlreadyPremium
        }

        // 2. サブスクリプションタイプの購入情報を問い合わせる
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        // 3. queryPurchasesAsyncを呼び出し、結果を待つ (ラムダは使わない)
        val result = billingClient.queryPurchasesAsync(params)
        val billingResult = result.billingResult
        val purchasesList = result.purchasesList

        // 4. 結果を判定して返す
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            if (purchasesList.isNotEmpty()) {
                // 取得した購入情報をループで処理
                for (purchase in purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        // 既存の購入処理メソッドを呼び出す
                        handlePurchase(purchase)
                    }
                }
                // 正常に処理が完了したことを通知
                return RestorePurchaseResult.Success
            } else {
                // 有効な購入情報がなかった場合
                Logger.d(TAG_NAME, "No active subscriptions found to restore.")
                return RestorePurchaseResult.NoPurchases
            }
        } else {
            // エラーが発生した場合
            Logger.e(TAG_NAME, "Failed to restore purchases. Error: ${billingResult.debugMessage}")
            return RestorePurchaseResult.Failure
        }
    }
}

sealed interface RestorePurchaseResult {
    object Success : RestorePurchaseResult
    object NoPurchases : RestorePurchaseResult
    object Failure : RestorePurchaseResult
    object AlreadyPremium : RestorePurchaseResult
}

/**
 * 購入時のエラー種別
 */
sealed class PurchaseError {
    object UserCanceled : PurchaseError()
    object NetworkError : PurchaseError()
    object ItemUnavailable : PurchaseError()
    object AlreadyOwned : PurchaseError()
    data class Unknown(val message: String) : PurchaseError()
}