package net.unifar.mydiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import net.unifar.mydiary.presentation.ui.DiariesScreen
import net.unifar.mydiary.presentation.theme.MyDiaryTheme
import net.unifar.mydiary.presentation.viewmodel.MainViewModel

private const val MIN_VERSION_FLEXIBLE = "min_version_flexible"
private const val MIN_VERSION_IMMEDIATE = "min_version_immediate"
private const val REQUEST_CODE_UPDATE = 1001
private const val DAYS_FOR_FLEXIBLE_UPDATE = 7

const val LAST_FLEXIBLE_CANCEL_TIME = "last_flexible_cancel_time"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 1. Launcherを登録
        val updateLauncherImmediate =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // アップデート完了 → 通常通りアプリ継続
                } else {
                    // 強制アップデートなのにキャンセルされた → アプリ終了するべき
                    this.finish()
                }
            }
        val updateLauncherFlexible =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // アップデート完了 → 通常通りアプリ継続
                } else {
                    val prefs = getSharedPreferences("update_prefs", MODE_PRIVATE)
                    prefs.edit {
                        putLong(LAST_FLEXIBLE_CANCEL_TIME, System.currentTimeMillis())
                    }

                }
            }

        // 2. アップデートチェックして起動

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                val priority = appUpdateInfo.updatePriority()

                if (priority >= 4 && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncherImmediate,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                } else if (
                    (appUpdateInfo.clientVersionStalenessDays()
                        ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE &&
                    priority >= 3 && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    val prefs = getSharedPreferences("update_prefs", MODE_PRIVATE)
                    val cancelTime = prefs.getLong("last_flexible_cancel_time", -1)
                    if (cancelTime > 0) {
                        val threeDaysLater = cancelTime + (3 * 24 * 60 * 60 * 1000)

                        if (System.currentTimeMillis() < threeDaysLater) {
                            // 後でを押して三日以内だったら再通知しない
                            return@addOnSuccessListener
                        }
                    }

                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        updateLauncherFlexible,
                        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                    )
                }
            }
        }

        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!prefs.contains(FIRST_LAUNCH_DATE)) {
            prefs.edit {
                putLong(FIRST_LAUNCH_DATE, System.currentTimeMillis())
            }
        }
        MobileAds.initialize(this)

        enableEdgeToEdge()
        setContent {
            hiltViewModel<MainViewModel>() // 初期化を走らせる
            MyDiaryTheme {
                DiariesScreen()
            }
        }
    }

    companion object {
        const val FIRST_LAUNCH_DATE = "first_launch_date"
        const val APP_PREFS = "app_prefs"
        const val REVIEW_SHOWN = "review_shown"
        const val BACKUP_CODE = "backup_code"
    }
}