package net.unifar.mydiary.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import net.unifar.mydiary.data.local.entity.Diary
import net.unifar.mydiary.data.local.dao.DiaryDao
import net.unifar.mydiary.util.getCurrentDateFormatted
import net.unifar.mydiary.util.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class BackUpRepository @Inject constructor(
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) {

    private val _lastBackupDate =
        MutableStateFlow<GetLastBackupDateResult>(GetLastBackupDateResult.Loading)
    val lastBackupDate = _lastBackupDate


    suspend fun refreshLastBackupDate(backupCode: String?): GetLastBackupDateResult {
        if (backupCode == null) {
            _lastBackupDate.value = GetLastBackupDateResult.Error
            return GetLastBackupDateResult.Error
        }

        _lastBackupDate.value = GetLastBackupDateResult.Loading
        try {

            // try-catchで失敗時の例外処理を行う
            val result = firestore
                .collection("backupCodes")
                .document(backupCode)
                .collection("backups")
                .orderBy(
                    "createdAt",
                    Query.Direction.DESCENDING
                ).limit(1)
                .get()
                .await() // 非同期処理の結果を待って取得し、例外があればここでthrowされる

            if (result.documents.isEmpty()) {
                // バックアップデータが存在しない場合の処理
                _lastBackupDate.value = GetLastBackupDateResult.Empty
                return GetLastBackupDateResult.Empty
            } else {
                // "createdAt"フィールドをString型で取得
                // フィールドが存在しない、または型が異なる場合はCastExceptionやNullPointerExceptionの可能性があるので注意
                val lastBackupTimeStamp =
                    result.documents.first().data?.get("createdAt") as Timestamp
                val lastBackupDateTimeString =
                    convertTimestampToLocalString(lastBackupTimeStamp)
                _lastBackupDate.value =
                    GetLastBackupDateResult.Success(lastBackupDateTimeString)
                return GetLastBackupDateResult.Success(lastBackupDateTimeString)
            }
        } catch (e: Exception) {
            _lastBackupDate.value = GetLastBackupDateResult.Error
            return GetLastBackupDateResult.Error
        }
    }

    private fun convertTimestampToLocalString(timestamp: Timestamp): String {
        // Step 1: Timestampを標準のjava.util.Dateに変換
        val date: Date = timestamp.toDate()

        // Step 2: Dateをローカルのタイムゾーンとロケールでフォーマット
        // 例: "2025/10/19 13:22:09"
        val formatter = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

        // ロケール設定を省略すると、デバイスのデフォルトロケールが使用されます
        return formatter.format(date)
    }


    object DateAsLongSerializer : KSerializer<Date> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Date", PrimitiveKind.LONG)

        override fun serialize(encoder: Encoder, value: Date) {
            encoder.encodeLong(value.time) // ← JSONに保存するときはDateをLongにする
        }

        override fun deserialize(decoder: Decoder): Date {
            return Date(decoder.decodeLong()) // ← JSON読み込み時はLongからDateを復元
        }
    }

    suspend fun backupDiaries(diaryDao: DiaryDao, backupCode: String?): BackupResult {
        if (backupCode == null) return BackupResult.Error
        val json = getDiariesJson(diaryDao)
        try {
            firestore
                .collection("backupCodes")
                .document(backupCode)
                .collection("backups")
                .document(getCurrentDateFormatted())
                .set(mapOf("content" to json, "createdAt" to FieldValue.serverTimestamp()))
                .await()

            Logger.d("backup", "backup success")
            return BackupResult.Success
        } catch (e: Exception) {
            Logger.e("backup", "backup failed $e")
            return BackupResult.Error
        }
    }

    suspend fun getDiariesJson(diaryDao: DiaryDao): String {
        val diaries = diaryDao.getAllDiariesOnce()
        val jsonFormatter = Json {
            serializersModule = SerializersModule {
                contextual(Date::class, DateAsLongSerializer) // 👈 ここでDateのシリアライザを登録
            }
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
        val json = jsonFormatter.encodeToString(diaries)
        return json
    }


    suspend fun restoreDiaries(
        diaryDao: DiaryDao,
        backupCode: String?
    ): RestoreBackupResult {
        // 1. scope引数を削除し、関数をシンプルにする
        if (backupCode == null) return RestoreBackupResult.Error

        try {
            // Firestoreからバックアップデータを取得 (await()で待機)
            val result = firestore
                .collection("backupCodes")
                .document(backupCode)
                .collection("backups")
                .orderBy(
                    "createdAt",
                    Query.Direction.DESCENDING
                ).limit(1).get().await()

            // データの存在チェック
            if (result.documents.isEmpty()) {
                return RestoreBackupResult.Empty // 仮にEmptyという結果があると仮定
            }

            // JSONデータの取得とデコード
            val json = result.documents.first().data?.get("content") as? String
                ?: return RestoreBackupResult.Error // contentフィールドがない、または型が違う場合

            val jsonFormatter = Json {
                // ... シリアライザー設定 ...
                serializersModule = SerializersModule {
                    contextual(Date::class, DateAsLongSerializer)
                }
                ignoreUnknownKeys = true
            }

            // デコード処理
            val diaries = runCatching {
                jsonFormatter.decodeFromString<List<Diary>>(json)
            }.getOrElse {
                // JSONパース失敗
                return RestoreBackupResult.Error
            }

            // 2. データベース操作（I/O処理）を withContext で IOディスパッチャに切り替え
            //    かつ、launchを使わず直接 await/suspend して完了を待つ
            withContext(Dispatchers.IO) {
                diaryDao.insertDiaries(diaries) // 👈 これが完了するまで待機する
            }

            // データベース書き込みまで全て成功したら、初めて成功を返す
            return RestoreBackupResult.Success

        } catch (e: Exception) {
            // Firestoreエラー、await()中のネットワークエラー、その他の予期せぬエラーをキャッチ
            Logger.e("restore", "restore failed $e")
            return RestoreBackupResult.Error
        }
    }
}

sealed interface GetLastBackupDateResult {
    data class Success(val date: String) : GetLastBackupDateResult
    data object Loading : GetLastBackupDateResult
    data object Empty : GetLastBackupDateResult
    data object Error : GetLastBackupDateResult
}

sealed interface RestoreBackupResult {
    data object Success : RestoreBackupResult
    data object Empty : RestoreBackupResult
    data object Error : RestoreBackupResult
}

sealed interface BackupResult {
    data object Success : BackupResult
    data object Error : BackupResult
}
