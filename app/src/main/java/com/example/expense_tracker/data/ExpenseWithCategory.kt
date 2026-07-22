package com.example.expense_tracker.data

import androidx.compose.runtime.Immutable

/**
 * Join result: Expense + Category name for displaying in list.
 */
@Immutable
data class ExpenseWithCategory(
    val id: Long,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String,
    val description: String = "",
    val timestamp: Long,
    val type: String = TransactionType.EXPENSE.name,
    val walletId: Long = 1L
)
