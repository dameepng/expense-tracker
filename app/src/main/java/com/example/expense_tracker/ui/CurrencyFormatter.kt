package com.example.expense_tracker.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val format: NumberFormat by lazy {
        try {
            NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        } catch (_: IllegalArgumentException) {
            // Fallback: Indonesian locale not available on this device
            NumberFormat.getCurrencyInstance(Locale.getDefault())
        }.apply { maximumFractionDigits = 0 }
    }

    fun format(amount: Long): String = format.format(amount)
}
