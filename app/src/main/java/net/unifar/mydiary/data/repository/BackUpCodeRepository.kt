package net.unifar.mydiary.data.repository

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import net.unifar.mydiary.BuildConfig
import net.unifar.mydiary.MainActivity
import java.math.BigInteger
import java.security.MessageDigest

@Singleton
class BackUpCodeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) {

    private val _backUpCodeFlow = MutableStateFlow<String?>(null)
    val backUpCodeFlow = _backUpCodeFlow

    init {
        setBackUpCodeBySharedPreferences()
    }

    fun setBackUpCodeBySharedPreferences() {
        val sharedPreferences =
            context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
        _backUpCodeFlow.value = sharedPreferences.getString(MainActivity.BACKUP_CODE, null)
    }

    fun deleteBackUpCode() {
        val sharedPreferences =
            context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            remove(MainActivity.BACKUP_CODE)
        }
    }


    fun saveAsBackUpCode(purchaseToken: String): String {
        val sharedPreferences =
            context.getSharedPreferences(MainActivity.APP_PREFS, Context.MODE_PRIVATE)
        val backUpCode = purchaseToken.toAlphanumericHash()
        sharedPreferences.edit {
            putString(
                MainActivity.BACKUP_CODE,
                backUpCode
            ).apply()
        }
        return backUpCode
    }

    fun savePurchaseToken(purchaseToken: String, backupCode: String) {
        firestore.collection("backupCodes").document(backupCode).set(
            hashMapOf(
                "purchaseToken" to purchaseToken,
                "createdAt" to FieldValue.serverTimestamp()
            )
        )
    }


    /**
     * 文字列をSHA-256でハッシュ化し、
     * 結果を指定された長さの英数字（0-9, A-Z）に変換します。
     * @param length 取得したい文字列の長さ（デフォルトは12）
     * @return 変換後の英数字文字列
     */
    fun String.toAlphanumericHash(): String {
        val length = 12
        // 1. 文字列をSHA-256でハッシュ化し、バイト配列を取得
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this.toByteArray(Charsets.UTF_8))

        // 2. ハッシュのバイト配列を、正の整数として扱うBigIntegerに変換
        val bigInt = BigInteger(1, hashBytes)

        // 3. BigIntegerを36進数の文字列に変換（0-9, a-z）
        val base36 = bigInt.toString(36)

        val hash = base36.take(length).uppercase()
        if (BuildConfig.DEBUG) {
            return "DEBUG_$hash"
        }
        // 4. 指定された長さで切り取り、大文字に変換して返す
        return hash
    }

}