package com.example.expense_tracker.ui

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormatter {
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun formatTime(epochMillis: Long): String = timeFormat.format(Date(epochMillis))
}
