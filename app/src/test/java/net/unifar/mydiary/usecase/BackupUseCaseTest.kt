package net.unifar.mydiary.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BackUpRepository
import net.unifar.mydiary.data.repository.BackupResult
import net.unifar.mydiary.data.repository.GetLastBackupDateResult
import net.unifar.mydiary.data.repository.RestoreBackupResult
import net.unifar.mydiary.domain.usecase.BackupUseCase
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BackupUseCaseTest {

    private lateinit var backUpRepository: BackUpRepository
    private lateinit var backUpCodeRepository: BackUpCodeRepository
    private lateinit var backupUseCase: BackupUseCase
    private lateinit var diaryDao: DiaryDao

    @Before
    fun setup() {
        backUpRepository = mockk()
        backUpCodeRepository = mockk()
        diaryDao = mockk()
        backupUseCase = BackupUseCase(backUpRepository, backUpCodeRepository)
    }

    @Test
    fun `executeBackup returns Error when backupCode is null`() = runTest {
        // Given
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(null)

        // When
        val result = backupUseCase.executeBackup(diaryDao)

        // Then
        assertEquals(BackupResult.Error, result)
        coVerify(exactly = 0) { backUpRepository.backupDiaries(any(), any()) }
    }

    @Test
    fun `executeBackup calls backupDiaries and refreshLastBackupDate on success`() = runTest {
        // Given
        val backupCode = "test-backup-code"
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(backupCode)
        coEvery { backUpRepository.backupDiaries(diaryDao, backupCode) } returns BackupResult.Success
        coEvery { backUpRepository.refreshLastBackupDate(backupCode) } returns GetLastBackupDateResult.Success("2025-10-25")

        // When
        val result = backupUseCase.executeBackup(diaryDao)

        // Then
        assertEquals(BackupResult.Success, result)
        coVerify(exactly = 1) { backUpRepository.backupDiaries(diaryDao, backupCode) }
        coVerify(exactly = 1) { backUpRepository.refreshLastBackupDate(backupCode) }
    }

    @Test
    fun `executeBackup does not call refreshLastBackupDate on failure`() = runTest {
        // Given
        val backupCode = "test-backup-code"
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(backupCode)
        coEvery { backUpRepository.backupDiaries(diaryDao, backupCode) } returns BackupResult.Error

        // When
        val result = backupUseCase.executeBackup(diaryDao)

        // Then
        assertEquals(BackupResult.Error, result)
        coVerify(exactly = 1) { backUpRepository.backupDiaries(diaryDao, backupCode) }
        coVerify(exactly = 0) { backUpRepository.refreshLastBackupDate(any()) }
    }

    @Test
    fun `executeRestore returns result from repository`() = runTest {
        // Given
        val backupCode = "test-backup-code"
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(backupCode)
        coEvery { backUpRepository.restoreDiaries(diaryDao, backupCode) } returns RestoreBackupResult.Success
        coEvery { backUpRepository.refreshLastBackupDate(backupCode) } returns GetLastBackupDateResult.Success("2025-10-25")

        // When
        val result = backupUseCase.executeRestore(diaryDao)

        // Then
        assertEquals(RestoreBackupResult.Success, result)
        coVerify(exactly = 1) { backUpRepository.restoreDiaries(diaryDao, backupCode) }
        coVerify(exactly = 1) { backUpRepository.refreshLastBackupDate(backupCode) }
    }

    @Test
    fun `refreshLastBackupDate returns result from repository`() = runTest {
        // Given
        val backupCode = "test-backup-code"
        val expectedResult = GetLastBackupDateResult.Success("2025-10-25")
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(backupCode)
        coEvery { backUpRepository.refreshLastBackupDate(backupCode) } returns expectedResult

        // When
        val result = backupUseCase.refreshLastBackupDate()

        // Then
        assertEquals(expectedResult, result)
        coVerify(exactly = 1) { backUpRepository.refreshLastBackupDate(backupCode) }
    }
}

