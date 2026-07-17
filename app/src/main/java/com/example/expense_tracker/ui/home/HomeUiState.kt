package com.example.expense_tracker.ui.home

import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod

data class HomeUiState(
    val totalAmount: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val isLoading: Boolean = false,
    val periodLabel: String = "Bulan Ini"
)
