package com.example.expense_tracker.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        .apply { maximumFractionDigits = 0 }

    fun format(amount: Long): String = format.format(amount)
}
