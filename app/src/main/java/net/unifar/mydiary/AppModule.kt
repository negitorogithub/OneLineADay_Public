package net.unifar.mydiary

import android.content.Context
import androidx.room.Room
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.app
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.data.local.database.AppDatabase
import net.unifar.mydiary.util.Logger
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

// Firebase関連のモジュール
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirestore(@ApplicationContext context: Context): FirebaseFirestore {
        val firestore = Firebase.firestore

        // Firebaseプロジェクト情報をログ出力
        val firebaseApp = Firebase.app
        val projectId = firebaseApp.options.projectId
        Logger.d("Firebase", "📱 Connected to Firebase Project: $projectId")
        Logger.d("Firebase", "📦 Application ID: ${firebaseApp.options.applicationId}")

        if (BuildConfig.DEBUG) {
            // Debugモードの場合の設定
            Logger.d("Firebase", "🔧 Debug mode: Firestore settings configured for testing")
        } else {
            Logger.d("Firebase", "🚀 Release mode: Using production Firestore")
        }

        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        val analytics = FirebaseAnalytics.getInstance(context)

        if (BuildConfig.DEBUG) {
            // Debugモードの場合：DebugViewを自動的に有効化
            analytics.setAnalyticsCollectionEnabled(true)
            Logger.d("Firebase", "🔧 Debug mode: Analytics DebugView enabled")
        } else {
            analytics.setAnalyticsCollectionEnabled(true)
            Logger.d("Firebase", "🚀 Release mode: Using production Analytics")
        }

        return analytics
    }
}

// DI用のモジュールを作成
@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}