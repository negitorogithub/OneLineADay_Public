package net.unifar.mydiary.util

import android.util.Log
import net.unifar.mydiary.BuildConfig

/**
 * ログ出力のラッパークラス
 * デバッグビルドでのみログを出力します
 */
object Logger {
    
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.e(tag, message, throwable)
            } else {
                Log.e(tag, message)
            }
        }
    }
    
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }
    
    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, message)
        }
    }
    
    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, message)
        }
    }
}

