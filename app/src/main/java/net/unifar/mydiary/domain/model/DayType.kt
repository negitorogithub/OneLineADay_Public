package net.unifar.mydiary.domain.model

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