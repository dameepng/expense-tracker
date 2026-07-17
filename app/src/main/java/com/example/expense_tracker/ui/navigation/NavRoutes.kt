package com.example.expense_tracker.ui.navigation

object NavRoutes {
    const val HOME = "home"
    const val INPUT = "input?expenseId={expenseId}"
    const val SUMMARY = "summary"
    const val WALLET = "wallet"
    const val PROFILE = "profile"

    fun inputRoute(expenseId: Long? = null): String {
        return if (expenseId != null) "input?expenseId=$expenseId" else "input"
    }

    const val WALLET_DETAIL = "wallet/{walletId}"
    fun walletDetailRoute(walletId: Long): String = "wallet/$walletId"

    const val REMINDER_LIST = "reminder_list"
    const val REMINDER_FORM = "reminder_form?reminderId={reminderId}"
    fun reminderFormRoute(reminderId: Long? = null): String {
        return if (reminderId != null) "reminder_form?reminderId=$reminderId" else "reminder_form"
    }
}
