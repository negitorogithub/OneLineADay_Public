package net.unifar.mydiary.domain.usecase

import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BackUpRepository
import net.unifar.mydiary.data.repository.BackupResult
import net.unifar.mydiary.data.repository.RestoreBackupResult
import javax.inject.Inject

class BackupUseCase @Inject constructor(
    private val backUpRepository: BackUpRepository,
    private val backUpCodeRepository: BackUpCodeRepository
) {

    /**
     * バックアップを実行し、成功時は最終バックアップ日時を更新する
     */
    suspend fun executeBackup(diaryDao: DiaryDao): BackupResult {
        val backupCode = backUpCodeRepository.backUpCodeFlow.value
        if (backupCode == null) {
            return BackupResult.Error
        }

        val result = backUpRepository.backupDiaries(diaryDao, backupCode)
        if (result is BackupResult.Success) {
            backUpRepository.refreshLastBackupDate(backupCode)
        }
        return result
    }

    /**
     * バックアップから復元を実行する
     */
    suspend fun executeRestore(diaryDao: DiaryDao): RestoreBackupResult {
        val backupCode = backUpCodeRepository.backUpCodeFlow.value

        val result = backUpRepository.restoreDiaries(diaryDao, backupCode)
        if (result is RestoreBackupResult.Success) {
            backUpRepository.refreshLastBackupDate(backupCode)
        }
        return result
    }

    /**
     * 最終バックアップ日時を取得する
     */
    suspend fun refreshLastBackupDate(): net.unifar.mydiary.data.repository.GetLastBackupDateResult {
        val backupCode = backUpCodeRepository.backUpCodeFlow.value
        return backUpRepository.refreshLastBackupDate(backupCode)
    }
}
