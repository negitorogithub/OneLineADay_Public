package net.unifar.mydiary.data.repository

import kotlinx.coroutines.flow.Flow
import net.unifar.mydiary.data.local.entity.Diary
import net.unifar.mydiary.data.local.dao.DiaryDao
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

class DiaryRepository
@Inject constructor(
    private val diaryDao: DiaryDao
) {

    val allDiaries: Flow<List<Diary>> = diaryDao.getAllDiaries()

    suspend fun insert(diary: Diary) {
        diaryDao.insert(diary)
    }

    suspend fun insertAll(diaries: List<Diary>) {
        diaryDao.insertDiaries(diaries)
    }

    suspend fun getDiaryById(id: String): Diary {
        return diaryDao.getDiaryById(id)
    }


    suspend fun deleteDiaryById(id: String) {
        diaryDao.deleteDiaryById(id)
    }

    suspend fun update(diary: Diary) {
        diaryDao.update(diary)
    }

    suspend fun updateContent(id: String, content: String) {
        diaryDao.updateContent(id, content)
    }

    suspend fun diariesCount(): Int {
        return diaryDao.diariesCount()
    }

    suspend fun deleteAllDiaries() {
        diaryDao.deleteAllDiaries()
    }

    suspend fun updateDiaryDateToNext(id: String) {
        val diary = diaryDao.getDiaryById(id)

        val currentLocalDate = diary.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nextLocalDate = currentLocalDate.plusDays(1)

        val nextDate = Date.from(
            nextLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
        )

        diaryDao.update(diary.copy(date = nextDate))
    }

    suspend fun updateDiaryDateToPrevious(id: String) {
        val diary = diaryDao.getDiaryById(id)

        val currentLocalDate = diary.date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val nextLocalDate =
            currentLocalDate.minusDays(1).atTime(23, 59, 59).atZone(ZoneId.systemDefault())

        val nextDate = Date.from(
            nextLocalDate.toInstant()
        )

        diaryDao.update(diary.copy(date = nextDate))
    }

}