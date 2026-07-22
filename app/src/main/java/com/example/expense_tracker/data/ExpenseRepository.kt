package com.example.expense_tracker.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository abstraction for expense data access.
 * Allows ViewModel to be tested with a fake implementation.
 */
interface ExpenseRepository {
    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Long>
    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Long>
    fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>>
    fun getAllTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Expense>>
    fun getAllTransactions(): List<Expense>
    fun getCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: String): Flow<List<Category>>
    fun deleteExpense(expense: Expense)
    fun insertExpense(expense: Expense)
    fun getExpenseById(id: Long): Expense?
    fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long>
    fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long>
    fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>>
}
