package net.unifar.mydiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import net.unifar.mydiary.data.local.entity.Diary

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(diary: Diary)

    @Delete
    suspend fun delete(diary: Diary)

    @Query("SELECT * FROM Diary WHERE id = :id")
    suspend fun getDiaryById(id: String): Diary

    @Query("DELETE FROM Diary WHERE id = :id")
    suspend fun deleteDiaryById(id: String)

    @Query("SELECT * FROM Diary ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<Diary>>

    @Update
    suspend fun update(diary: Diary)

    @Query("UPDATE Diary SET content = :newContent WHERE id = :id")
    suspend fun updateContent(id: String, newContent: String)

    @Query("UPDATE Diary SET date = :newDate WHERE id = :id")
    suspend fun updateDate(id: String, newDate: String)

    // バックアップ用（1回だけ取得版）
    @Query("SELECT * FROM Diary ORDER BY date DESC")
    suspend fun getAllDiariesOnce(): List<Diary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaries(diaries: List<Diary>)

    @Query("DELETE FROM diary")
    suspend fun deleteAllDiaries()
    
    @Query("SELECT COUNT(*) FROM Diary")
    suspend fun diariesCount(): Int
}

