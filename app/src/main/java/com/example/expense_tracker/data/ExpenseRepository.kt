package com.example.expense_tracker.data

/**
 * Repository abstraction for expense data access.
 * Allows ViewModel to be tested with a fake implementation.
 */
interface ExpenseRepository {
    fun getTotalExpense(startTime: Long, endTime: Long): Long
    fun getTotalIncome(startTime: Long, endTime: Long): Long
    fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense>
    fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense>
    fun getCategories(): List<Category>
    fun getCategoriesByType(type: String): List<Category>
    fun deleteExpense(expense: Expense)
    fun insertExpense(expense: Expense)
    fun getExpenseById(id: Long): Expense?
    fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long
    fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long
    fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense>
}
