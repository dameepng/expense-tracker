package com.example.expense_tracker.ui

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private const val DEFAULT_CURRENCY = "IDR"
    private var currentCurrency: String = DEFAULT_CURRENCY
    private var format: NumberFormat = createFormat(DEFAULT_CURRENCY)

    private fun createFormat(currency: String): NumberFormat {
        val locale = when (currency) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY // Using Germany as default for Euro formatting
            else -> Locale.forLanguageTag("id-ID")
        }
        return try {
            NumberFormat.getCurrencyInstance(locale)
        } catch (_: IllegalArgumentException) {
            NumberFormat.getCurrencyInstance(Locale.getDefault())
        }.apply { maximumFractionDigits = 0 }
    }

    @Synchronized
    fun setCurrency(currency: String) {
        if (currentCurrency != currency) {
            currentCurrency = currency
            format = createFormat(currency)
        }
    }

    @Synchronized
    fun format(amount: Long): String = format.format(amount)
}
