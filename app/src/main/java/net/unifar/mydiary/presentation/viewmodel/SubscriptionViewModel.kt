package net.unifar.mydiary.presentation.viewmodel

import android.app.Activity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import net.unifar.mydiary.data.repository.BillingRepository
import net.unifar.mydiary.util.AnalyticsEvents
import net.unifar.mydiary.util.PlanType

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {
    // Repositoryから商品詳細のFlowを公開
    val productDetails: StateFlow<List<ProductDetails>?> = billingRepository.productDetailsList

    // 実際の購入処理をRepositoryに委譲
    fun initiatePurchase(
        activity: Activity,
        selectedDetails: ProductDetails,
        offerToken: String,
        basePlanId: String
    ) {
        if (offerToken.isEmpty()) {
            // オファートークンがない場合、購入フローを開始できない
            // エラーハンドリングをここに記述
            return
        }
        
        // プラン情報を取得
        val planType = when (basePlanId) {
            "premium-plan-base" -> PlanType.Monthly
            "premium-plan-annual" -> PlanType.Annual
            else -> null
        }
        
        // 価格情報を取得
        val offerDetails = selectedDetails.subscriptionOfferDetails?.find { it.offerToken == offerToken }
        val price = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "Unknown"
        
        // Analyticsイベントを送信
        logPurchaseInitiated(
            planType = planType,
            price = price,
            productId = selectedDetails.productId
        )
        
        billingRepository.launchPurchaseFlow(
            activity = activity,
            selectedDetails = selectedDetails,
            offerToken = offerToken // offerTokenを渡す
        )
    }
    
    /**
     * 購入開始イベントを送信
     * @param planType プランタイプ（月額/年額）
     * @param price 価格（フォーマット済み文字列）
     * @param productId 商品ID
     */
    private fun logPurchaseInitiated(planType: PlanType?, price: String, productId: String) {
        analytics.logEvent(
            AnalyticsEvents.PURCHASE_INITIATED,
            bundleOf(
                "plan_type" to (planType?.value ?: "unknown"),
                "price" to price,
                "product_id" to productId
            )
        )
    }

    val isPremium: StateFlow<Boolean> = billingRepository.isPremiumFlow
    
    // 購入エラー状態を公開
    val purchaseError = billingRepository.purchaseErrorFlow
    
    // エラーを消費する（UIで表示した後に呼び出す）
    fun clearPurchaseError() {
        billingRepository.clearPurchaseError()
    }
}