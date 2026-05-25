package com.olegkos.vnengine.engine.academy

import kotlinx.serialization.Serializable

/** Цикл: [weekdays] будних, затем [weekendDays] выходных. [AcademyConfig.dayVar] с 1. */
@Serializable
data class AcademyWeekSchedule(
  val weekdays: Int = 5,
  val weekendDays: Int = 2,
) {
  val cycleLength: Int get() = weekdays + weekendDays

  private fun cycleIndex(academyDay: Int): Int {
    val c = cycleLength
    return ((academyDay - 1) % c + c) % c
  }

  fun dayKind(academyDay: Int): AcademyDayKind =
    if (cycleIndex(academyDay) < weekdays) AcademyDayKind.WEEKDAY else AcademyDayKind.WEEKEND

  fun dayIndexInBlock(academyDay: Int): Int {
    val i = cycleIndex(academyDay)
    return if (i < weekdays) i + 1 else i - weekdays + 1
  }
}

enum class AcademyDayKind {
  WEEKDAY,
  WEEKEND,
  ;

  val label: String
    get() = if (this == WEEKDAY) "Будний день" else "Выходной"

  val shortLabel: String
    get() = if (this == WEEKDAY) "Будни" else "Выходные"
}

/** always | weekday | weekend (вместе = always) */
enum class AcademyScheduleScope {
  ALWAYS,
  WEEKDAY,
  WEEKEND,
  ;

  fun availableOn(dayKind: AcademyDayKind): Boolean = when (this) {
    ALWAYS -> true
    WEEKDAY -> dayKind == AcademyDayKind.WEEKDAY
    WEEKEND -> dayKind == AcademyDayKind.WEEKEND
  }

  /** [verb]: `"build"`, `"visit"` или null — общая подсказка. */
  fun lockedReason(dayKind: AcademyDayKind, verb: String? = null): String? {
    if (availableOn(dayKind)) return null
    return when (verb) {
      "build" -> if (this == WEEKDAY) "Строить можно только в будни" else "Строить можно только в выходные"
      "visit" -> if (this == WEEKDAY) "Посещать можно только в будни" else "Посещать можно только в выходные"
      else -> if (this == WEEKDAY) "Только в будни" else "Только в выходные"
    }
  }

  companion object {
    fun fromJson(raw: String?): AcademyScheduleScope = when (raw?.trim()?.lowercase()) {
      null, "", "always", "together", "вместе", "all" -> ALWAYS
      "weekday", "weekdays", "будни", "будний" -> WEEKDAY
      "weekend", "weekends", "выходные", "выходной" -> WEEKEND
      else -> ALWAYS
    }
  }
}
