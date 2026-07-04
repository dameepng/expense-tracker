package com.example.expense_tracker.data

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

/**
 * TDD tests for StreakCalculator.calculateStreak()
 *
 * Based on US5 acceptance criteria:
 * - Streak bertambah kalau ada expense setiap hari
 * - Streak reset ke 0 kalau ada gap 1 hari
 * - Hari ini belum ada expense → mundur dari kemarin
 * - Belum ada expense sama sekali → 0
 * - Multiple expense di hari sama → tidak double-count
 */
class StreakCalculatorTest {

    private val calculator = StreakCalculator()

    // ── Helpers ────────────────────────────────────────────────────

    /** Midnight N days ago in device timezone */
    private fun midnightNDaysAgo(days: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    /** Raw timestamp on a given day (midnight + offset hours) */
    private fun tsOnDay(daysAgo: Int, hourOffset: Int = 10): Long {
        return midnightNDaysAgo(daysAgo) + hourOffset * 3600_000L
    }

    private fun todayMidnight(): Long = midnightNDaysAgo(0)
    private fun yesterdayMidnight(): Long = midnightNDaysAgo(1)

    // ── Streak Tests ───────────────────────────────────────────────

    @Test
    fun `streak returns 0 when no expenses exist`() {
        val result = calculator.calculateStreak(
            timestamps = emptyList(),
            todayMidnight = todayMidnight()
        )
        assertEquals(0, result)
    }

    @Test
    fun `streak returns 1 when expense only today`() {
        val today = todayMidnight()
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(0)),  // raw timestamp on today
            todayMidnight = today
        )
        assertEquals(1, result)
    }

    @Test
    fun `streak incremented for consecutive days`() {
        val today = todayMidnight()
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(0), tsOnDay(1), tsOnDay(2), tsOnDay(3)),
            todayMidnight = today
        )
        assertEquals(4, result)
    }

    @Test
    fun `streak resets to 0 after a 1-day gap`() {
        val today = todayMidnight()
        // Today, yesterday → gap (2 days ago missing) → 3 days ago
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(0), tsOnDay(1), tsOnDay(3)),
            todayMidnight = today
        )
        assertEquals(2, result)
    }

    @Test
    fun `streak starts from yesterday if today has no expense`() {
        val today = todayMidnight()
        // Today: no expense. Yesterday + 2 days ago + 3 days ago → streak = 3
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(1), tsOnDay(2), tsOnDay(3)),
            todayMidnight = today
        )
        assertEquals(3, result)
    }

    @Test
    fun `streak returns 0 if today AND yesterday have no expenses`() {
        val today = todayMidnight()
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(2), tsOnDay(3)),
            todayMidnight = today
        )
        assertEquals(0, result)
    }

    @Test
    fun `multiple expenses on same day do not double-count streak`() {
        val today = todayMidnight()
        // Two raw timestamps on the same today → both normalize to today's midnight → still streak = 1
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(0, 9), tsOnDay(0, 14)),
            todayMidnight = today
        )
        assertEquals(1, result)
    }

    @Test
    fun `streak handles unsorted timestamps — sorted by calculator`() {
        val today = todayMidnight()
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(2), tsOnDay(0), tsOnDay(1)),
            todayMidnight = today
        )
        assertEquals(3, result)
    }

    @Test
    fun `streak returns 1 when only yesterday has expense (today empty)`() {
        val today = todayMidnight()
        val result = calculator.calculateStreak(
            timestamps = listOf(tsOnDay(1)),
            todayMidnight = today
        )
        assertEquals(1, result)
    }
}
