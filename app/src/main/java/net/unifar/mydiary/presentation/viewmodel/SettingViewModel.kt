package net.unifar.mydiary.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BackUpRepository
import net.unifar.mydiary.data.repository.BackupResult
import net.unifar.mydiary.data.repository.BillingRepository
import net.unifar.mydiary.data.repository.RestoreBackupResult
import net.unifar.mydiary.data.repository.RestorePurchaseResult
import net.unifar.mydiary.domain.usecase.BackupUseCase
import net.unifar.mydiary.util.AnalyticsEvents
import net.unifar.mydiary.util.DialogButton
import net.unifar.mydiary.util.SettingAction
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    internal val diaryDao: DiaryDao,
    val billingRepository: BillingRepository,
    val backUpCodeRepository: BackUpCodeRepository,
    val backUpRepository: BackUpRepository,
    private val backupUseCase: BackupUseCase,
    private val analytics: FirebaseAnalytics,
) : ViewModel() {
    val isPremiumState = billingRepository.isPremiumFlow
    val backUpCodeState = backUpCodeRepository.backUpCodeFlow
    val lastBackupDateState = backUpRepository.lastBackupDate

    suspend fun backupDiaries(): BackupResult {
        val result = withContext(Dispatchers.IO) {
            backupUseCase.executeBackup(diaryDao)
        }
        return result
    }

    suspend fun restoreDiaries(): RestoreBackupResult {
        val result = withContext(Dispatchers.IO) {
            backupUseCase.executeRestore(diaryDao)
        }
        return result
    }

    /**
     * 指定されたURIに文字列データを書き込む
     * @param context ContentResolverを取得するためのContext
     * @param uri 書き込み先のファイルのURI
     * @param text 書き込む文字列
     */
    suspend fun exportDiariesToFile(context: Context, uri: Uri): WriteDataToFileResult {
        // withContext(Dispatchers.IO) を使うことで、このブロック内の処理が
        // I/O処理に適したバックグラウンドスレッドで実行されることを保証する

        val text = backUpRepository.getDiariesJson(diaryDao)
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("Failed to get OutputStream for the given URI.")

                outputStream.use { stream ->
                    stream.write(text.toByteArray(Charsets.UTF_8))
                }
                WriteDataToFileResult.Success
            } catch (e: Exception) {
                // IOExceptionやSecurityExceptionなど、あらゆる例外をキャッチ
                WriteDataToFileResult.Failure
            }
        }
    }

    suspend fun restorePurchase(): RestorePurchaseResult {
        val result = withContext(Dispatchers.IO) {
            billingRepository.restorePurchase()
        }
        return result
    }

    /**
     * 設定画面でのクリックイベントを送信
     * @param action アクション
     * @param isLocked ロックされているかどうか
     */
    fun logSettingAction(action: SettingAction, isLocked: Boolean = false) {
        analytics.logEvent(
            AnalyticsEvents.SETTING_ACTION,
            bundleOf(
                "action" to action.value,
                "is_locked" to isLocked
            )
        )
    }

    /**
     * プレミアムへ誘導するダイアログのボタンクリックを記録
     * @param feature 機能名
     * @param button クリックされたボタン
     */
    fun logPremiumDialogAction(feature: SettingAction, button: DialogButton) {
        analytics.logEvent(
            AnalyticsEvents.PREMIUM_DIALOG_ACTION,
            bundleOf(
                "feature" to feature.value,
                "button" to button.value
            )
        )
    }

    /**
     * 復元確認ダイアログのボタンクリックを記録
     * @param button クリックされたボタン
     */
    fun logRestoreDialogAction(button: DialogButton) {
        analytics.logEvent(
            AnalyticsEvents.RESTORE_DIALOG_ACTION,
            bundleOf(
                "button" to button.value
            )
        )
    }
}

sealed interface WriteDataToFileResult {
    object Success : WriteDataToFileResult
    object Failure : WriteDataToFileResult
}

