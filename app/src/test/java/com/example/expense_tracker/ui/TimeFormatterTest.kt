package com.example.expense_tracker.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class TimeFormatterTest {

    @Test
    fun `formatDate formats epoch millis to day and month in Indonesian locale`() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jakarta")).apply {
            set(2026, Calendar.SEPTEMBER, 17, 14, 30, 0)
        }
        val formatted = TimeFormatter.formatDate(calendar.timeInMillis)
        assertTrue(formatted.startsWith("17"))
        assertTrue(formatted.contains("Sep", ignoreCase = true))
    }

    @Test
    fun `formatTime formats epoch millis to HH mm`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 5)
        }
        val formatted = TimeFormatter.formatTime(calendar.timeInMillis)
        assertEquals("09:05", formatted)
    }
}
