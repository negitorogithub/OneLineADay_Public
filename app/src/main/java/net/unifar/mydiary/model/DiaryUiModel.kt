package net.unifar.mydiary.model

import net.unifar.mydiary.db.DayType
import net.unifar.mydiary.db.Diary
import net.unifar.mydiary.db.toDayType
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class DiaryUiModel(
    val id: String,
    val date: Date,
    val dateString: String,    // "03" みたいな 日付のみ
    val dayOfWeek: String,     // "土"
    val dayType: DayType,
    val content: String,
    var isEditing: Boolean
)


fun Diary.toUiModel(isEditing: Boolean): DiaryUiModel {
    val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
    val dayOfWeekFormat = SimpleDateFormat("E", Locale.getDefault())
    val localDate = date.toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate().dayOfWeek
    val dayType = localDate.toDayType()
    return DiaryUiModel(
        id = this.id,
        date = this.date,
        dateString = dateFormat.format(this.date),
        dayOfWeek = dayOfWeekFormat.format(this.date),
        dayType = dayType,
        content = this.content,
        isEditing = isEditing
    )
}