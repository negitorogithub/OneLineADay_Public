package net.unifar.mydiary

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.BackUpCodeRepository
import net.unifar.mydiary.data.repository.BackUpRepository
import net.unifar.mydiary.data.repository.BackupResult
import net.unifar.mydiary.data.repository.BillingRepository
import net.unifar.mydiary.data.repository.GetLastBackupDateResult
import net.unifar.mydiary.data.repository.RestoreBackupResult
import net.unifar.mydiary.domain.usecase.BackupUseCase
import net.unifar.mydiary.presentation.viewmodel.MainViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var backUpRepository: BackUpRepository
    private lateinit var backUpCodeRepository: BackUpCodeRepository
    private lateinit var billingRepository: BillingRepository
    private lateinit var backupUseCase: BackupUseCase
    private lateinit var diaryDao: DiaryDao
    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        backUpRepository = mockk(relaxed = true)
        backUpCodeRepository = mockk()
        billingRepository = mockk()
        backupUseCase = mockk(relaxed = true)
        diaryDao = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializeApp restores and backs up when backupCode is available`() = runTest {
        // Given
        val backupCode = "test-backup-code"
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(backupCode)
        coEvery {
            backUpRepository.restoreDiaries(
                diaryDao,
                backupCode
            )
        } returns RestoreBackupResult.Success
        coEvery { backupUseCase.refreshLastBackupDate() } returns GetLastBackupDateResult.Success("2025-10-25")
        coEvery {
            backUpRepository.backupDiaries(
                diaryDao,
                backupCode
            )
        } returns BackupResult.Success

        // When
        mainViewModel = MainViewModel(
            backUpRepository,
            backUpCodeRepository,
            billingRepository,
            backupUseCase,
            diaryDao
        )
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { backUpRepository.restoreDiaries(diaryDao, backupCode) }
        coVerify(exactly = 1) { backupUseCase.refreshLastBackupDate() }
        coVerify(exactly = 1) { backUpRepository.backupDiaries(diaryDao, backupCode) }
    }

    @Test
    fun `initializeApp does not restore or backup when backupCode is null`() = runTest {
        // Given
        coEvery { backUpCodeRepository.backUpCodeFlow } returns MutableStateFlow(null)
        coEvery { backupUseCase.refreshLastBackupDate() } returns GetLastBackupDateResult.Empty

        // When
        mainViewModel = MainViewModel(
            backUpRepository,
            backUpCodeRepository,
            billingRepository,
            backupUseCase,
            diaryDao
        )
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { backUpRepository.restoreDiaries(any(), any()) }
        coVerify(exactly = 1) { backupUseCase.refreshLastBackupDate() }
        coVerify(exactly = 0) { backUpRepository.backupDiaries(any(), any()) }
    }
}

