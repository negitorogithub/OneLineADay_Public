package net.unifar.mydiary.presentation.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.unifar.mydiary.R
import net.unifar.mydiary.data.repository.PurchaseError
import net.unifar.mydiary.presentation.theme.MyDiaryTheme
import net.unifar.mydiary.presentation.viewmodel.SubscriptionViewModel

const val PREMIUM_MONTHLY_ID = "premium-plan-base"
const val PREMIUM_ANNUAL_ID = "premium-plan-annual" // 年額プランのIDを追加

@Composable
fun SubscriptionScreen(snackbarHostState: SnackbarHostState) {
    val viewModel: SubscriptionViewModel = hiltViewModel()

    // Activityを取得 (購入フロー開始に必要)
    val activity = LocalActivity.current

    // ViewModelから商品情報をStateとして監視
    val productDetailsList by viewModel.productDetails.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val purchaseError by viewModel.purchaseError.collectAsState()

    // Snackbar表示用のスコープ
    val scope = rememberCoroutineScope()

    // エラーが発生したらSnackbarを表示
    val errorMessageUserCanceled = stringResource(R.string.purchase_error_user_canceled)
    val errorMessageNetwork = stringResource(R.string.purchase_error_network)
    val errorMessageItemUnavailable = stringResource(R.string.purchase_error_item_unavailable)
    val errorMessageAlreadyOwned = stringResource(R.string.purchase_error_already_owned)
    val errorMessageUnknown = stringResource(R.string.purchase_error_unknown)

    LaunchedEffect(purchaseError) {
        purchaseError?.let { error ->
            val message = when (error) {
                is PurchaseError.UserCanceled -> errorMessageUserCanceled
                is PurchaseError.NetworkError -> errorMessageNetwork
                is PurchaseError.ItemUnavailable -> errorMessageItemUnavailable
                is PurchaseError.AlreadyOwned -> errorMessageAlreadyOwned
                is PurchaseError.Unknown -> errorMessageUnknown
            }
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
            viewModel.clearPurchaseError()
        }
    }

    val scrollableState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollableState)
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isPremium) {
            Description()
        }
        when {
            // プレミアム会員の場合
            isPremium -> PremiumActiveCard()
            // 商品情報がロード中の場合
            productDetailsList == null -> LoadingCard()
            // 商品情報がロード完了した場合
            else -> productDetailsList?.let { detailsList ->
                Column {
                    detailsList.map { productDetails -> // 変数名を 'it' から 'productDetails' に変更して分かりやすくします

                        // サブスクリプションのオファー詳細を取得します。
                        // 通常、基本的なサブスクリプションではリストの最初の要素を使用します。
                        val offerDetailsList = productDetails.subscriptionOfferDetails
                        if (offerDetailsList == null) {
                            // TODO: implement error handling
                            return@Column
                        }
                        val titleMap = mapOf(
                            PREMIUM_MONTHLY_ID to stringResource(R.string.monthly_plan_name),
                            PREMIUM_ANNUAL_ID to stringResource(R.string.annual_plan_name)
                        )
                        val lengthMap = mapOf(
                            PREMIUM_MONTHLY_ID to stringResource(R.string.month),
                            PREMIUM_ANNUAL_ID to stringResource(R.string.year)
                        )
                        offerDetailsList.map { offerDetails ->
                            val priceText =
                                offerDetails.pricingPhases.pricingPhaseList.first().formattedPrice
                            val title = titleMap[offerDetails.basePlanId]
                            if (title == null) return@map
                            val length = lengthMap[offerDetails.basePlanId]
                            if (length == null) return@map
                            SubscriptionCard(
                                onPurchaseClick = {
                                    viewModel.initiatePurchase(
                                        activity = activity!!,
                                        selectedDetails = productDetails,
                                        offerToken = offerDetails.offerToken,
                                        basePlanId = offerDetails.basePlanId
                                    )
                                },
                                // 取得した価格情報を渡します
                                priceText = priceText,
                                title = title,
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Description() {
    Column(
        Modifier
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.unlock_everything),
            fontSize = 32.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Backup,
                    contentDescription = stringResource(R.string.backup_icon_description),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.automatic_backup),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.FileUpload,
                    contentDescription = stringResource(R.string.export_icon_description),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.export_anytime),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DoNotDisturb,
                    contentDescription = stringResource(R.string.no_ads_icon_description),
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.no_ads),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun SubscriptionCard(
    priceText: String,
    title: String,
    onPurchaseClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPurchaseClick),
        // (2) Modifier.clip の代わりに、Cardの shape 引数を指定
        shape = RoundedCornerShape(size = 16.dp),

        // (3) Modifier.border の代わりに、Cardの border 引数を指定
        border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = "Premium Icon",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary // 青色
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 価格と購入ボタン
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = priceText,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 4.dp),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onPurchaseClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        stringResource(R.string.subscribe_now),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                stringResource(R.string.loading_plan_info),
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun PremiumActiveCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WorkspacePremium,
                contentDescription = "Active Icon",
                modifier = Modifier
                    .size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.premium_member),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.premium_member_message),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewDescription() {
    MyDiaryTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(
                Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Description()
            }
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewLoadingCard() {
    MyDiaryTheme {
        LoadingCard()
    }
}

@PreviewLightDark
@Composable
fun PreviewPremiumCard() {
    MyDiaryTheme {
        PremiumActiveCard()
    }
}

@PreviewLightDark
@Composable
fun PreviewSubscriptionCard() {
    MyDiaryTheme {
        SubscriptionCard(
            priceText = "300円",
            title = "Monthly Plan",
            onPurchaseClick = {},
        )
    }
}

