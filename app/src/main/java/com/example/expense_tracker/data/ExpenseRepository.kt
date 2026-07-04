package com.example.expense_tracker.data

/**
 * Repository abstraction for expense data access.
 * Allows ViewModel to be tested with a fake implementation.
 */
interface ExpenseRepository {
    fun getTotalExpense(startTime: Long, endTime: Long): Long
    fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense>
    fun getCategories(): List<Category>
}
