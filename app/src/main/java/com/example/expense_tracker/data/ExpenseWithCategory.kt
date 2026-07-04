package com.example.expense_tracker.data

/**
 * Join result: Expense + Category name for displaying in list.
 */
data class ExpenseWithCategory(
    val id: Long,
    val amount: Long,
    val categoryId: Long,
    val categoryName: String,
    val timestamp: Long
)
