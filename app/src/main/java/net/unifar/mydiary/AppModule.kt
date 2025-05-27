package net.unifar.mydiary

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.unifar.mydiary.db.AppDatabase
import net.unifar.mydiary.db.DiaryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "my_diary_db"
        ).build()
    }

    @Provides
    fun provideDiaryDao(
        db: AppDatabase
    ): DiaryDao {
        return db.diaryDao()
    }
}
