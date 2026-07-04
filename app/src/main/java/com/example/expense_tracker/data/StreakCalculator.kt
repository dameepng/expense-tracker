package com.example.expense_tracker.data

import java.util.Calendar

/**
 * Calculates streak: consecutive days with at least 1 expense,
 * counting backward from today using device timezone.
 *
 * Algorithm:
 * 1. Normalize all raw timestamps to local date midnights (dedup)
 * 2. If today has an expense → include today, count backward
 * 3. If today has no expense → check from yesterday, count backward
 * 4. Stop when a gap > 1 day is found
 * 5. Empty list → streak = 0
 */
class StreakCalculator {

    private val oneDayMs = 86_400_000L

    /**
     * @param timestamps raw epoch-millis list from DAO (may be unsorted, may contain duplicates per day)
     * @param todayMidnight midnight of today in device timezone (epoch millis)
     * @return streak count (consecutive days)
     */
    fun calculateStreak(timestamps: List<Long>, todayMidnight: Long): Int {
        if (timestamps.isEmpty()) return 0

        // Normalize all timestamps to local midnights, deduplicate, sort DESC
        val calendar = Calendar.getInstance()
        val normalized = timestamps
            .map { ts ->
                calendar.timeInMillis = ts
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            }
            .distinct()
            .sortedDescending()

        // Determine the reference start: today if it has expense, else yesterday
        val startFrom = if (normalized.firstOrNull() == todayMidnight) todayMidnight else todayMidnight - oneDayMs

        var streak = 0
        var expected = startFrom

        for (date in normalized) {
            if (date > expected) continue  // skip dates ahead of reference
            if (date == expected) {
                streak++
                expected -= oneDayMs
            } else {
                break  // gap found
            }
        }

        return streak
    }
}
