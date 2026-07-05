package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.FilterPeriod

data class SummaryUiState(
    val filter: FilterPeriod = FilterPeriod.TODAY,
    val items: List<BreakdownItem> = emptyList(),
    val totalAmount: Long = 0L
)

data class BreakdownItem(
    val categoryId: Long,
    val categoryName: String,
    val amount: Long,
    val percentage: Float // 0.0 - 1.0
)
