package net.unifar.mydiary.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.unifar.mydiary.model.entity_and_dao.Diary
import net.unifar.mydiary.model.entity_and_dao.DiaryDao

@Database(entities = [Diary::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){
    abstract fun diaryDao(): DiaryDao
}
