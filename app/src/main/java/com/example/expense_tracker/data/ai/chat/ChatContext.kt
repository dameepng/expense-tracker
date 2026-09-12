package com.example.expense_tracker.data.ai.chat

import com.example.expense_tracker.data.analytics.ExpenseComparison
import com.example.expense_tracker.data.analytics.PeriodExpenseSummary

internal enum class WalletScope(val wireValue: String) {
    ALL_WALLETS("all_wallets")
}

internal data class ChatContext(
    val snapshotEpochMillis: Long,
    val zoneId: String,
    val currency: String,
    val preferredLocale: String,
    val walletScope: WalletScope,
    val currentWeek: PeriodExpenseSummary,
    val currentMonth: PeriodExpenseSummary,
    val previousMonth: PeriodExpenseSummary,
    val monthComparison: ExpenseComparison
)

internal enum class ChatContextError {
    DATA_UNAVAILABLE,
    INVALID_PREFERENCES,
    INVALID_CONTEXT,
    CONTEXT_TOO_LARGE
}

/** Stable context failure exposed to later presentation-layer mapping. */
internal class ChatContextException(
    val reason: ChatContextError
) : Exception(reason.name)
