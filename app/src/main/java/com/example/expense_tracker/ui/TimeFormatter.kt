package com.example.expense_tracker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private const val DATE_PATTERN = "d MMM"
    private const val TIME_PATTERN = "HH:mm"

    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat(DATE_PATTERN, Locale.forLanguageTag("id-ID"))
    }

    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat(TIME_PATTERN, Locale.getDefault())
    }

    fun formatDate(epochMillis: Long): String {
        return dateFormat.get()?.format(Date(epochMillis)) ?: ""
    }

    fun formatTime(epochMillis: Long): String {
        return timeFormat.get()?.format(Date(epochMillis)) ?: ""
    }
}
