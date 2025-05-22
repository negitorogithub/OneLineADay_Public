package net.unifar.mydiary.model.entity_and_dao

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.DayOfWeek

enum class DayType {
    WEEKDAY,
    SATURDAY,
    SUNDAY,
    HOLIDAY
}

fun DayOfWeek.toDayType(): DayType {
    return when (this) {
        DayOfWeek.SATURDAY -> DayType.SATURDAY
        DayOfWeek.SUNDAY -> DayType.SUNDAY
        else -> DayType.WEEKDAY
    }
}