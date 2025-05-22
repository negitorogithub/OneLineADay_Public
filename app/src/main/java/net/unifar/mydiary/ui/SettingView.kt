package net.unifar.mydiary.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.unifar.mydiary.R
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.unifar.mydiary.model.entity_and_dao.Diary
import net.unifar.mydiary.model.entity_and_dao.DiaryDao
import net.unifar.mydiary.model.entity_and_dao.SettingViewModel
import java.io.File
import java.util.Date

private const val MAIL_ADDRESS = "unifarproject35@gmail.com"

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingView(snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingInquiryDetails = stringResource(R.string.setting_inquiry_details)
    val settingPleaseEnterYourMessageHere =
        stringResource(R.string.setting_please_enter_your_message_here)
    val settingInquiryAboutTheApp = stringResource(R.string.setting_inquiry_about_the_app)
    val settingChooseEmailApp = stringResource(R.string.setting_choose_email_app)
    val settingFailedToOpenTheEmailApp =
        stringResource(R.string.setting_failed_to_open_the_email_app)
    val settingNoEmailAppFound = stringResource(R.string.setting_no_email_app_found)
    val settingMailAddress = stringResource(R.string.setting_mail_address)

    val nativeAdState = remember { mutableStateOf<NativeAd?>(null) }
    val adLoader = remember {
        AdLoader.Builder(context, "ca-app-pub-3940256099942544/2247696110")  // ネイティブ広告用ユニットIDを使う
            .forNativeAd { nativeAd ->
                // ここでNativeAdオブジェクトを受け取れる
                // このnativeAdを使ってComposeにデータを渡す
                nativeAdState.value = nativeAd
            }
            .build()
    }
    LaunchedEffect(Unit) {
        adLoader.loadAd(AdRequest.Builder().build())
    }
    val scrollableState = rememberScrollState()

    val viewModel: SettingViewModel = hiltViewModel()
    Column(
        modifier = Modifier
            .verticalScroll(scrollableState)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
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
                            "App Version: $versionName"
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
                            }"
                                    ).toUri()
                        }

                        try {
                            // メールアプリ選択画面を出す
                            context.startActivity(
                                Intent.createChooser(
                                    intent,
                                    settingChooseEmailApp
                                )
                            )
                        } catch (e: Exception) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(settingFailedToOpenTheEmailApp)
                            }
                        }

                    },
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.setting_info),
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                settingMailAddress,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
        if (nativeAdState.value != null) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            NativeAdView_Setting_Bottom(nativeAdState.value!!)
        }
//        後で追加
//        HorizontalDivider(
//            modifier = Modifier.padding(horizontal = 16.dp),
//        )
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .combinedClickable(
//                    onClick = {
//                        coroutineScope.launch {
//                            backupDiaries(context, viewModel.diaryDao)
//                        }
//                    })
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                "バックアップを取る",
//                fontSize = 14.sp,
//                modifier = Modifier.weight(1f)
//            )
//            Icon(
//                Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                contentDescription = null,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//        HorizontalDivider(
//            modifier = Modifier.padding(horizontal = 16.dp)
//        )
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .combinedClickable(
//                    onClick = {
//                        coroutineScope.launch {
//                            restoreDiaries(context, viewModel.diaryDao)
//                        }
//                    })
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                "バックアップから復元する",
//                fontSize = 14.sp,
//                modifier = Modifier.weight(1f)
//            )
//            Icon(
//                Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                contentDescription = null,
//                modifier = Modifier.size(24.dp)
//            )
//        }
    }

}

object DateAsLongSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeLong(value.time) // ← JSONに保存するときはDateをLongにする
    }

    override fun deserialize(decoder: Decoder): Date {
        return Date(decoder.decodeLong()) // ← JSON読み込み時はLongからDateを復元
    }
}

suspend fun backupDiaries(context: Context, diaryDao: DiaryDao) {
    val diaries = diaryDao.getAllDiariesOnce()
    val jsonFormatter = Json {
        serializersModule = SerializersModule {
            contextual(Date::class, DateAsLongSerializer) // 👈 ここでDateのシリアライザを登録
        }
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    val json = jsonFormatter.encodeToString(diaries)

    val file = File(context.filesDir, "backup.json")
    file.writeText(json)
}

suspend fun restoreDiaries(context: Context, diaryDao: DiaryDao) {
    val file = File(context.filesDir, "backup.json")
    if (!file.exists()) return
    val json = file.readText()
    val jsonFormatter = Json {
        serializersModule = SerializersModule {
            contextual(Date::class, DateAsLongSerializer) // 👈 ここでDateのシリアライザを登録
        }
        encodeDefaults = true
        ignoreUnknownKeys = true
    }
    val diaries = jsonFormatter.decodeFromString<List<Diary>>(json)

    diaryDao.deleteAllDiaries() // 必要に応じて全削除
    diaryDao.insertDiaries(diaries)
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
        }
    )
}

private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {

}

