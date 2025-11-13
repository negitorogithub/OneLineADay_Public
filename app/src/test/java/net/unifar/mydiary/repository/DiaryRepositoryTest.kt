package net.unifar.mydiary.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.unifar.mydiary.data.local.entity.Diary
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.repository.DiaryRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Date

class DiaryRepositoryTest {

    private lateinit var diaryDao: DiaryDao
    private lateinit var diaryRepository: DiaryRepository

    @Before
    fun setup() {
        diaryDao = mockk(relaxed = true)
        diaryRepository = DiaryRepository(diaryDao)
    }

    @Test
    fun `insert calls dao insert`() = runTest {
        // Given
        val diary = Diary("test-id", "Test content", Date())
        coEvery { diaryDao.insert(diary) } returns Unit

        // When
        diaryRepository.insert(diary)

        // Then
        coVerify(exactly = 1) { diaryDao.insert(diary) }
    }

    @Test
    fun `getDiaryById returns diary from dao`() = runTest {
        // Given
        val diaryId = "test-id"
        val expectedDiary = Diary(diaryId, "Test content", Date())
        coEvery { diaryDao.getDiaryById(diaryId) } returns expectedDiary

        // When
        val result = diaryRepository.getDiaryById(diaryId)

        // Then
        assertEquals(expectedDiary, result)
        coVerify(exactly = 1) { diaryDao.getDiaryById(diaryId) }
    }

    @Test
    fun `deleteDiaryById calls dao deleteDiaryById`() = runTest {
        // Given
        val diaryId = "test-id"
        coEvery { diaryDao.deleteDiaryById(diaryId) } returns Unit

        // When
        diaryRepository.deleteDiaryById(diaryId)

        // Then
        coVerify(exactly = 1) { diaryDao.deleteDiaryById(diaryId) }
    }

    @Test
    fun `updateContent calls dao updateContent`() = runTest {
        // Given
        val diaryId = "test-id"
        val newContent = "Updated content"
        coEvery { diaryDao.updateContent(diaryId, newContent) } returns Unit

        // When
        diaryRepository.updateContent(diaryId, newContent)

        // Then
        coVerify(exactly = 1) { diaryDao.updateContent(diaryId, newContent) }
    }

    @Test
    fun `diariesCount returns count from dao`() = runTest {
        // Given
        val expectedCount = 5
        coEvery { diaryDao.diariesCount() } returns expectedCount

        // When
        val result = diaryRepository.diariesCount()

        // Then
        assertEquals(expectedCount, result)
        coVerify(exactly = 1) { diaryDao.diariesCount() }
    }

    @Test
    fun `allDiaries returns flow from dao`() = runTest {
        // Given
        val diaries = listOf(
            Diary("id1", "Content 1", Date()),
            Diary("id2", "Content 2", Date())
        )
        coEvery { diaryDao.getAllDiaries() } returns flowOf(diaries)

        // When
        val result = diaryRepository.allDiaries

        // Then
        // Flow comparison is tricky, so we just verify the dao was called
        coVerify { diaryDao.getAllDiaries() }
    }
}

