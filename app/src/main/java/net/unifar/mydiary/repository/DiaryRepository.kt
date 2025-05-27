package net.unifar.mydiary.repository

import kotlinx.coroutines.flow.Flow
import net.unifar.mydiary.db.Diary
import net.unifar.mydiary.db.DiaryDao
import javax.inject.Inject

class DiaryRepository
@Inject constructor(
    private val diaryDao: DiaryDao
) {

    val allDiaries: Flow<List<Diary>> = diaryDao.getAllDiaries()

    suspend fun insert(diary: Diary) {
        diaryDao.insert(diary)
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


}