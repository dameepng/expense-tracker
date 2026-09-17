package com.example.expense_tracker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private val timeFormat = ThreadLocal.withInitial {
        SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    fun formatTime(epochMillis: Long): String {
        return timeFormat.get()?.format(Date(epochMillis)) ?: ""
    }
}
