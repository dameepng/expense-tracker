package com.example.expense_tracker.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val INPUT = "input?expenseId={expenseId}"
    const val SUMMARY = "summary"
    const val WALLET = "wallet"
    const val PROFILE = "profile"
    const val ONBOARDING = "onboarding"

    fun inputRoute(expenseId: Long? = null): String {
        return if (expenseId != null) "input?expenseId=$expenseId" else "input"
    }

    const val REMINDER_LIST = "reminder_list"
}
