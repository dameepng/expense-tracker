package com.example.expense_tracker.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val INPUT = "input?expenseId={expenseId}"
    const val SUMMARY = "summary"

    fun inputRoute(expenseId: Long? = null): String {
        return if (expenseId != null) "input?expenseId=$expenseId" else "input"
    }
}
