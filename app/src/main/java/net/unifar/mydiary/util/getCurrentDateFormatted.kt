package net.unifar.mydiary.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun getCurrentDateFormatted(): String {
    val now: LocalDateTime = LocalDateTime.now()

    // 2. フォーマッタを作成 (例: "2025年10月18日 21時06分06秒")
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd-HH_mm_ss")
    // 3. フォーマットを適用して文字列を取得
    val formattedDateTime: String = now.format(formatter)

    return formattedDateTime
}