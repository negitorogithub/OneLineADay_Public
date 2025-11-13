package net.unifar.mydiary.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

fun generateSortableId(): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
    val now = Date()
    val timestamp = dateFormat.format(now)
    val randomPart = UUID.randomUUID().toString().take(8)

    return "${timestamp}_$randomPart"
}
