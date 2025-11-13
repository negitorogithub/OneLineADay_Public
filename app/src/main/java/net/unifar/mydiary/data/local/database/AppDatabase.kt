package net.unifar.mydiary.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.unifar.mydiary.data.local.entity.Diary
import net.unifar.mydiary.data.local.dao.DiaryDao

@Database(entities = [Diary::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
}
