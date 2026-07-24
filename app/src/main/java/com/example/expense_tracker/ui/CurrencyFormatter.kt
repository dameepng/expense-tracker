package com.example.expense_tracker.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private var currentCurrency: String = "IDR"
    private var format: NumberFormat = createFormat("IDR")

    private fun createFormat(currency: String): NumberFormat {
        val locale = when (currency) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY // Using Germany as default for Euro formatting
            else -> Locale("id", "ID")
        }
        return try {
            NumberFormat.getCurrencyInstance(locale)
        } catch (_: IllegalArgumentException) {
            NumberFormat.getCurrencyInstance(Locale.getDefault())
        }.apply { maximumFractionDigits = 0 }
    }

    fun setCurrency(currency: String) {
        if (currentCurrency != currency) {
            currentCurrency = currency
            format = createFormat(currency)
        }
    }

    fun format(amount: Long): String = format.format(amount)
}
