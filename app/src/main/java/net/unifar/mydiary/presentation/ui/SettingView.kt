package net.unifar.mydiary.presentation.ui

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.launch
import net.unifar.mydiary.R
import net.unifar.mydiary.presentation.viewmodel.SettingViewModel
import net.unifar.mydiary.data.repository.BackupResult
import net.unifar.mydiary.data.repository.GetLastBackupDateResult
import net.unifar.mydiary.data.repository.RestorePurchaseResult
import net.unifar.mydiary.presentation.theme.MyDiaryTheme
import net.unifar.mydiary.util.DialogButton
import net.unifar.mydiary.util.SettingAction
import net.unifar.mydiary.util.getCurrentDateFormatted

private const val MAIL_ADDRESS = "unifarproject35@gmail.com"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingView(snackbarHostState: SnackbarHostState, onChangeTab: (Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingInquiryDetails = stringResource(R.string.setting_inquiry_details)
    val settingPleaseEnterYourMessageHere =
        stringResource(R.string.setting_please_enter_your_message_here)
    val settingInquiryAboutTheApp = stringResource(R.string.setting_inquiry_about_the_app)
    val settingChooseEmailApp = stringResource(R.string.setting_choose_email_app)
    val settingFailedToOpenTheEmailApp =
        stringResource(R.string.setting_failed_to_open_the_email_app)
    val settingMailAddress = stringResource(R.string.setting_mail_address)
    val appName = stringResource(R.string.app_name)

    val viewModel: SettingViewModel = hiltViewModel()
    val isPremium = viewModel.isPremiumState.collectAsState().value
    val backUpCode = viewModel.backUpCodeState.collectAsState().value
    val lastBackupDateResult = viewModel.lastBackupDateState.collectAsState().value

    val openRestoreDialog = remember { mutableStateOf(false) }
    val openToPremiumDialog = remember { mutableStateOf(false) }
    val premiumDialogFeature = remember { mutableStateOf<SettingAction?>(null) }


    val nativeAdState = remember { mutableStateOf<NativeAd?>(null) }
    val adLoader = remember {
        AdLoader.Builder(context, "ca-app-pub-6418178360564076/3662115623")  // ネイティブ広告用ユニットIDを使う
            .forNativeAd { nativeAd ->
                // ここでNativeAdオブジェクトを受け取れる
                // このnativeAdを使ってComposeにデータを渡す
                nativeAdState.value = nativeAd
            }.build()
    }
    LaunchedEffect(isPremium) {
        if (isPremium) return@LaunchedEffect
        adLoader.loadAd(AdRequest.Builder().build())
    }
    val scrollableState = rememberScrollState()


    // ファイル作成インテントの結果を受け取るためのActivityResultLauncherを準備
    // rememberLauncherForActivityResult でランチャーをComposable内で記憶する
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 結果がOKの場合（ユーザーがファイルを選択した場合）
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // 取得したURIにデータを書き込む
                coroutineScope.launch {
                    viewModel.exportDiariesToFile(context, uri)
                    // TODO: error handling
                }
            }
        } else {
            // ユーザーがファイル選択をキャンセルした場合など
            coroutineScope.launch {
                snackbarHostState.showSnackbar(context.getString(R.string.export_cancelled))
            }
        }
    }


    Column(
        modifier = Modifier
            .verticalScroll(scrollableState)
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        SettingSectionHeader(stringResource(R.string.support))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // コンタクト
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        viewModel.logSettingAction(SettingAction.InquiryEmail)
                        val packageManager = context.packageManager
                        val packageName = context.packageName
                        val versionName = try {
                            packageManager.getPackageInfo(packageName, 0).versionName
                        } catch (e: Exception) {
                            "Unknown"
                        }

                        // 端末情報取得
                        val deviceInfo = listOf(
                            "Device: ${Build.MANUFACTURER} ${Build.MODEL}",
                            "Android Version: ${Build.VERSION.RELEASE}",
                            "App Version: $versionName",
                            "Backup Code: ${backUpCode ?: "None"}"
                        )

                        // メール本文
                        val body = listOf(
                            settingInquiryDetails,
                            settingPleaseEnterYourMessageHere,
                            "",
                            "-----------",
                            ""
                        ).plus(deviceInfo).joinToString("\n")
                        val subject = settingInquiryAboutTheApp
                        // メールIntent
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = ("mailto:$MAIL_ADDRESS?subject=${Uri.encode(subject)}&body=${
                                Uri.encode(body)
                            }").toUri()
                        }

                        try {
                            // メールアプリ選択画面を出す
                            context.startActivity(
                                Intent.createChooser(
                                    intent, settingChooseEmailApp
                                )
                            )
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(settingFailedToOpenTheEmailApp)
                            }
                        }

                    },
                )
                .padding(16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.setting_info),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // メールアドレス
        val clipboardManager = LocalClipboard.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = {
                    viewModel.logSettingAction(SettingAction.CopyEmail)
                    coroutineScope.launch {
                        clipboardManager.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "Support Mail Address",
                                    MAIL_ADDRESS
                                )
                            )
                        )
                        snackbarHostState.showSnackbar(context.getString(R.string.copied_to_clipboard))
                    }
                })
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                settingMailAddress, fontSize = 14.sp, modifier = Modifier.weight(1f)
            )
        }
        SettingSectionHeader(stringResource(R.string.backup))


//        後で追加
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // バックアップ
        LockableRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            isLocked = !isPremium,
            onClick = {
                viewModel.logSettingAction(SettingAction.Backup, isLocked = false)
                val job = coroutineScope.launch {
                    snackbarHostState.showSnackbar(context.getString(R.string.backup_started))
                }
                coroutineScope.launch {
                    val result = viewModel.backupDiaries()
                    job.cancel()
                    when (result) {
                        is BackupResult.Success -> {
                            snackbarHostState.showSnackbar(context.getString(R.string.backup_success))
                        }

                        is BackupResult.Error -> {
                            snackbarHostState.showSnackbar(context.getString(R.string.backup_failed))
                        }
                    }
                }
            },
            lockedOnClick = {
                viewModel.logSettingAction(SettingAction.Backup, isLocked = true)
                premiumDialogFeature.value = SettingAction.Backup
                openToPremiumDialog.value = true
            },
            content = {
                Column {
                    Text(
                        stringResource(R.string.take_backup),
                        fontSize = 14.sp,
                    )
                    if (isPremium) {
                        if (lastBackupDateResult is GetLastBackupDateResult.Success) {
                            Text(
                                "${stringResource(R.string.last_backup_at)} ${lastBackupDateResult.date}",
                                fontSize = 12.sp,
                            )
                        }
                        if (lastBackupDateResult is GetLastBackupDateResult.Loading) {
                            Text(
                                stringResource(R.string.loading),
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            })

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        // バックアップから復元
        LockableRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = {
                Text(
                    stringResource(R.string.recover_from_backup),
                    fontSize = 14.sp,
                )
            },
            lockedOnClick = {
                viewModel.logSettingAction(SettingAction.Restore, isLocked = true)
                premiumDialogFeature.value = SettingAction.Restore
                openToPremiumDialog.value = true
            },
            isLocked = !isPremium,
            onClick = {
                viewModel.logSettingAction(SettingAction.Restore, isLocked = false)
                openRestoreDialog.value = true

            },
        )
        if (openRestoreDialog.value) {
            RestoreDialog(
                onClose = {
                    viewModel.logRestoreDialogAction(DialogButton.Cancel)
                    openRestoreDialog.value = false
                },
                onRestore = {
                    viewModel.logRestoreDialogAction(DialogButton.Ok)
                    coroutineScope.launch {
                        viewModel.restoreDiaries()
                        snackbarHostState.showSnackbar(context.getString(R.string.restore_success))
                    }
                },
            )
        }
        if (openToPremiumDialog.value) {
            premiumDialogFeature.value?.let { feature ->
                ToPremiumDialog(
                    onClose = {
                        viewModel.logPremiumDialogAction(feature, DialogButton.Cancel)
                        openToPremiumDialog.value = false
                    },
                    onOk = {
                        viewModel.logPremiumDialogAction(feature, DialogButton.Ok)
                        onChangeTab(1)
                    },
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // エキスポート
        LockableRow(
            content = {
                Text(
                    stringResource(R.string.export_to_file),
                    fontSize = 14.sp,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            lockedOnClick = {
                viewModel.logSettingAction(SettingAction.Export, isLocked = true)
                premiumDialogFeature.value = SettingAction.Export
                openToPremiumDialog.value = true
            },
            onClick = {
                viewModel.logSettingAction(SettingAction.Export, isLocked = false)
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TITLE,
                        "${appName}_export_${getCurrentDateFormatted()}.txt"
                    )
                }
                createFileLauncher.launch(intent)
            },
            isLocked = !isPremium,
        )
        // バックアップコード
        if (isPremium) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            val clipboardManager = LocalClipboard.current

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.logSettingAction(SettingAction.CopyBackupCode)
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "Backup Code",
                                        backUpCode
                                    )
                                )
                            )
                            snackbarHostState.showSnackbar(context.getString(R.string.copied_to_clipboard))
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${stringResource(R.string.backup_code)} $backUpCode ${stringResource(R.string.backup_code_recommendation)}",
                    fontSize = 14.sp,
                )
            }
        }

        SettingSectionHeader(stringResource(R.string.restore_purchase_section))
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        // 購入の復元
        Row(
            content = {
                Text(
                    stringResource(R.string.restore_purchases),
                    fontSize = 14.sp,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.logSettingAction(SettingAction.RestorePurchase)
                    coroutineScope.launch {
                        val result = viewModel.restorePurchase()
                        when (result) {
                            RestorePurchaseResult.Success -> {
                                snackbarHostState.showSnackbar(context.getString(R.string.restore_purchase_success))
                            }

                            RestorePurchaseResult.Failure, RestorePurchaseResult.NoPurchases -> {
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.restore_purchase_failed)
                                )
                            }

                            RestorePurchaseResult.AlreadyPremium -> snackbarHostState.showSnackbar(
                                context.getString(R.string.already_premium_member)
                            )
                        }
                    }
                }
                .padding(16.dp),
        )
        if (!isPremium && nativeAdState.value != null) {
            NativeAdView_Setting_Bottom(nativeAdState.value!!)
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title, style = MaterialTheme.typography.labelLarge, // 見出し用のスタイル
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(top = 16.dp) // 他のセクションとの間隔を確保
    )
}

@Composable
fun NativeAdView_Setting_Bottom(nativeAd: NativeAd) {
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    AndroidView(
        factory = { context ->
            // 1. レイアウトXMLからNativeAdViewをinflateする（XML必要）
            val adView = LayoutInflater.from(context)
                .inflate(R.layout.native_ad_layout, null) as NativeAdView

            adView.headlineView = adView.findViewById(R.id.ad_headline)
            (adView.headlineView as TextView).text = nativeAd.headline
            (adView.headlineView as TextView).setTextColor(onSurface)

            adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
            (adView.callToActionView as Button).text = nativeAd.callToAction

            adView.mediaView = adView.findViewById(R.id.ad_media)

            // 角丸・Ripple入りDrawableを使う！
            (adView.callToActionView as Button).background =
                ContextCompat.getDrawable(context, R.drawable.ad_button_background)
            // テキスト色だけは別で設定する（これはOK）
            (adView.callToActionView as Button).setBackgroundTintList(
                ColorStateList.valueOf(primaryContainer)
            )
            (adView.callToActionView as Button).setTextColor(
                onPrimaryContainer
            )
            adView.setNativeAd(nativeAd)

            adView
        })
}

@Composable
fun ToPremiumDialog(onClose: () -> Unit, onOk: () -> Unit) {
    // 3. 状態に基づいて AlertDialog を表示
    AlertDialog(
        // ダイアログの外側をタップしたり、戻るボタンを押したときの処理
        onDismissRequest = {
            onClose()
        },
        // タイトル
        title = {
            Text(text = stringResource(R.string.to_premium_dialog_title))
        },
        // メインコンテンツ
        text = {
            Text(stringResource(R.string.to_premium_dialog_message), fontSize = 14.sp)
        },
        // 確認ボタン（通常は右側）
        confirmButton = {
            Button(
                onClick = {
                    onOk()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        // キャンセルボタン（通常は左側）
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onClose()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun RestoreDialog(onClose: () -> Unit, onRestore: () -> Unit) {
    // 3. 状態に基づいて AlertDialog を表示
    AlertDialog(
        // ダイアログの外側をタップしたり、戻るボタンを押したときの処理
        onDismissRequest = {
            onClose()
        },
        // タイトル
        title = {
            Text(text = stringResource(R.string.restore_confirmation_title))
        },
        // メインコンテンツ
        text = {
            Text(stringResource(R.string.restore_confirmation_message), fontSize = 14.sp)
        },
        // 確認ボタン（通常は右側）
        confirmButton = {
            Button(
                onClick = {
                    // 処理を実行
                    onRestore()
                    onClose()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        // キャンセルボタン（通常は左側）
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onClose()
                }
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview
@Composable
fun RestoreDialogPreview() {
    MyDiaryTheme {
        RestoreDialog(onClose = {}, onRestore = {})
    }
}


@Preview
@Composable
fun ToPremiumDialogPreview() {
    MyDiaryTheme {
        ToPremiumDialog(onClose = {}, onOk = {})
    }
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

}

