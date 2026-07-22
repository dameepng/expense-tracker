package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TransactionType

data class SummaryUiState(
    val filter: FilterPeriod = FilterPeriod.TODAY,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val items: List<BreakdownItem> = emptyList(),
    val totalAmount: Long = 0L,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val isLoading: Boolean = false
)

data class BreakdownItem(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float // 0.0 - 1.0
)
