package net.unifar.mydiary.presentation.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch
import net.unifar.mydiary.R
import net.unifar.mydiary.presentation.viewmodel.DiariesViewModel
import net.unifar.mydiary.util.TabName

@Composable
fun getKeyboardHeight(): Int {
    val insets = WindowInsets.ime
    return insets.getBottom(LocalDensity.current)
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DiariesScreen() {

    val viewModel: DiariesViewModel = hiltViewModel()
    val diariesUiModel by viewModel.diariesUiModel.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var shouldShowDeleteDiaryDialog by remember { mutableStateOf(false) }

    val lastDiaryCount = remember { mutableIntStateOf(0) }
    LaunchedEffect(diariesUiModel.size) {
        if (diariesUiModel.size > lastDiaryCount.intValue) {
            listState.scrollToItem(0)
        }
        lastDiaryCount.intValue = diariesUiModel.size
    }
    val keyboardHeightPx = getKeyboardHeight().toFloat()
    val screenHeightPx =
        with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    val availableHeightPx = screenHeightPx - keyboardHeightPx
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(viewModel.editingId, diariesUiModel) {
        val targetIndex = diariesUiModel.indexOfFirst { it.id == viewModel.editingId.value }
        if (targetIndex >= 0) {
            val visibleItems = listState.layoutInfo.visibleItemsInfo
            val targetItem = visibleItems.find { it.index == targetIndex }

            if (targetItem == null ||  // そもそも画面にいないか
                targetItem.offset < 0 ||  // 上にはみ出ているか
                (targetItem.offset + targetItem.size) > availableHeightPx // キーボード含めた高さを超えて下にはみ出ているか
            ) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val isPremium by viewModel.isPremium.collectAsState()

    // 初回表示時とタブ変更時にAnalyticsイベントを送信
    LaunchedEffect(selectedTab) {
        val tabName = when (selectedTab) {
            0 -> TabName.Home
            1 -> TabName.Premium
            2 -> TabName.Settings
            else -> TabName.Home
        }
        viewModel.logTabSelected(selectedTab, tabName)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { viewModel.stopEditingCurrent() }),
        floatingActionButton = {
            when (selectedTab) {
                0 ->
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                viewModel.stopEditingCurrent()
                                val id = viewModel.addNewDiary()
                                viewModel.startEditing(id)
                            }
                        },
                        modifier = Modifier
                            .padding(bottom = 40.dp)
                            .width(IntrinsicSize.Max),
                        shape = RoundedCornerShape(
                            16.dp
                        ),
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),  // お好みで余白
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit, // ここは好きなアイコンに
                                contentDescription = stringResource(R.string.icon_description),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // アイコンとテキストの間に隙間
                            Text(
                                text = stringResource(R.string.write),
                                modifier = Modifier.weight(1f),
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                        }
                    }

            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    when (selectedTab) {
                        0 -> if (viewModel.isEditing()) {
                            IconButton(
                                onClick = { shouldShowDeleteDiaryDialog = true },
                                colors = IconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = Color.Unspecified,
                                    disabledContainerColor = Color.Unspecified,
                                    disabledContentColor = Color.Unspecified
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Column {
                if (diariesUiModel.size > 1 && !isPremium) {
                    AdaptiveAdBanner(
                        "ca-app-pub-6418178360564076/5600199041"
                    )
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                Icons.Default.Home,
                                contentDescription = stringResource(R.string.home_description)
                            )
                        },
                        label = { Text(stringResource(R.string.home)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = stringResource(R.string.premium_description)
                            )
                        },
                        label = { Text(stringResource(R.string.plans)) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_description)
                            )
                        },
                        label = { Text(stringResource(R.string.settings)) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            when (selectedTab) {
                0 -> DiaryList(listState, bringIntoViewRequester)
                1 -> SubscriptionScreen(snackbarHostState)
                2 -> SettingView(snackbarHostState, {
                    selectedTab = it
                })
            }
        }
        when (selectedTab) {
            0 ->
                if (shouldShowDeleteDiaryDialog) {
                    AlertDialog(
                        onDismissRequest = { shouldShowDeleteDiaryDialog = false },
                        title = { Text(stringResource(R.string.delete_diary_dialog_title)) },
                        text = {
                            Text(
                                stringResource(R.string.delete_diary_dialog_content),
                                fontSize = 16.sp
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    viewModel.viewModelScope.launch {
                                        viewModel.deleteEditingDiary()
                                    }
                                    shouldShowDeleteDiaryDialog = false
                                },
                            ) {
                                Text(
                                    stringResource(R.string.delete_diary_dialog_yes),
                                    fontSize = 16.sp,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { shouldShowDeleteDiaryDialog = false }
                            ) {
                                Text(
                                    stringResource(R.string.delete_diary_dialog_no),
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    )
                }
        }
    }
}

@Composable
fun AdaptiveAdBanner(adUnitId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // --- 画面幅に合わせてAdaptive Bannerサイズを計算
    val displayMetrics = context.resources.displayMetrics
    val adWidthPixels = displayMetrics.widthPixels.toFloat()
    val adWidthDp = adWidthPixels / displayMetrics.density

    val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
        context,
        adWidthDp.toInt()
    )
    val adHeightPx = adSize.getHeightInPixels(context)
    val adHeightDp = with(LocalDensity.current) { adHeightPx.toDp() } // ← dpに変換！
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(adHeightDp)
    ) {
        AndroidView(
            factory = {
                val adView = AdView(context).apply {
                    this.adUnitId = adUnitId
                }

                // --- 画面幅に合わせてAdaptive Bannerサイズを設定 ---
                val displayMetrics = context.resources.displayMetrics
                val adWidthPixels = displayMetrics.widthPixels.toFloat()
                val adWidthDp = adWidthPixels / displayMetrics.density

                adView.setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context,
                        adWidthDp.toInt()
                    )
                )

                adView.loadAd(AdRequest.Builder().build())
                adView
            },
            update = { adView ->
                // 必要に応じて再読み込みとかできる
            },
            modifier = modifier
        )
    }
}