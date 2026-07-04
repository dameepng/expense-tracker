package com.example.expense_tracker.data

import com.example.expense_tracker.ui.home.StreakRepository
import java.util.Calendar

/**
 * Room-backed implementation of StreakRepository.
 * Uses StreakCalculator to normalize timestamps to device timezone.
 */
class RoomStreakRepository(
    private val dao: ExpenseDao
) : StreakRepository {

    private val calculator = StreakCalculator()

    override fun calculateStreak(): Int {
        val rawTimestamps = dao.getDistinctDatesWithExpense()
        val todayMidnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return calculator.calculateStreak(rawTimestamps, todayMidnight)
    }
}
