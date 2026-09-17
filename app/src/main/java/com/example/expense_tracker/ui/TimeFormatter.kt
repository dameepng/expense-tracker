package com.example.expense_tracker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private val dateFormat = ThreadLocal.withInitial {
        SimpleDateFormat("d MMM", Locale.forLanguageTag("id-ID"))
    }

    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    fun formatDate(epochMillis: Long): String {
        return dateFormat.get()?.format(Date(epochMillis)) ?: ""
    }

    fun formatTime(epochMillis: Long): String {
        return timeFormat.get()?.format(Date(epochMillis)) ?: ""
    }
}
