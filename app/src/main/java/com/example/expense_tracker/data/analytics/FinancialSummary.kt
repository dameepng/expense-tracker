package com.example.expense_tracker.data.analytics

import java.math.BigDecimal

enum class FinancialPeriodKind {
    CURRENT_WEEK,
    CURRENT_MONTH,
    PREVIOUS_MONTH
}

data class FinancialPeriod(
    val kind: FinancialPeriodKind,
    val startInclusiveEpochMillis: Long,
    val endExclusiveEpochMillis: Long,
    val isComplete: Boolean
) {
    init {
        require(startInclusiveEpochMillis <= endExclusiveEpochMillis) {
            "Period start must not be after its end"
        }
    }
}

data class CategoryExpenseTotal(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Long
) {
    init {
        require(totalAmount >= 0L) { "Category total must not be negative" }
    }
}

data class PeriodExpenseSummary(
    val period: FinancialPeriod,
    val totalExpense: Long,
    val categories: List<CategoryExpenseTotal>
) {
    init {
        require(totalExpense >= 0L) { "Expense total must not be negative" }
    }
}

data class ExpenseComparison(
    val amountDifference: Long,
    val percentageChange: BigDecimal?
)

data class FinancialSummary(
    val snapshotEpochMillis: Long,
    val zoneId: String,
    val currentWeek: PeriodExpenseSummary,
    val currentMonth: PeriodExpenseSummary,
    val previousMonth: PeriodExpenseSummary,
    val monthComparison: ExpenseComparison
)

class FinancialAggregationException(
    message: String,
    cause: Throwable? = null
) : IllegalStateException(message, cause)
