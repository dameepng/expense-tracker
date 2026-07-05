package com.example.expense_tracker.data

import java.util.Calendar

/**
 * Shared utility for calculating time ranges based on FilterPeriod.
 * Used by HomeViewModel and SummaryViewModel to avoid duplication.
 */
object TimeRangeCalculator {

    private const val ONE_DAY_MS = 86_400_000L

    fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    fun calculateRange(filter: FilterPeriod): Pair<Long, Long> {
        val todayEnd = todayStart() + ONE_DAY_MS
        return when (filter) {
            FilterPeriod.TODAY -> Pair(todayStart(), todayEnd)

            FilterPeriod.WEEK -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                }
                Pair(cal.timeInMillis, todayEnd)
            }

            FilterPeriod.MONTH -> {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                Pair(cal.timeInMillis, todayEnd)
            }
        }
    }
}
