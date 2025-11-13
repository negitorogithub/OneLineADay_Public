package net.unifar.mydiary.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BackUpRepository
import net.unifar.mydiary.data.repository.BillingRepository
import net.unifar.mydiary.domain.usecase.BackupUseCase
import javax.inject.Inject

/**
 * アプリ全体の初期化処理を管理するViewModel
 * 各画面のViewModelではなく、アプリケーション全体のライフサイクルに関わる処理を担当
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val backUpRepository: BackUpRepository,
    private val backUpCodeRepository: BackUpCodeRepository,
    @Suppress("unused") billingRepository: BillingRepository, // 購入の復元を走らせる
    private val backupUseCase: BackupUseCase,
    private val diaryDao: DiaryDao,
) : ViewModel() {

    init {
        initializeApp()
    }

    /**
     * アプリケーション起動時の初期化処理
     */
    private fun initializeApp() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. バックアップコードの取得
            val backupCode = backUpCodeRepository.backUpCodeFlow.value

            // 2. バックアップからの復元（初回起動時やデータがない場合）
            if (backupCode != null) {
                backUpRepository.restoreDiaries(diaryDao, backupCode)
            }

            // 3. 最終バックアップ日時の取得
            backupUseCase.refreshLastBackupDate()

            // 4. 自動バックアップの実行（データがある場合）
            if (backupCode != null) {
                backUpRepository.backupDiaries(diaryDao, backupCode)
            }
        }
    }

}

