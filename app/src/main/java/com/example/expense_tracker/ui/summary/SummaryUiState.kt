package com.example.expense_tracker.ui.summary

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TransactionType

@Immutable
data class SummaryUiState(
    val filter: FilterPeriod = FilterPeriod.TODAY,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val items: List<BreakdownItem> = emptyList(),
    val totalAmount: Long = 0L,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val isLoading: Boolean = false
)

@Immutable
data class BreakdownItem(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float // 0.0 - 1.0
)
