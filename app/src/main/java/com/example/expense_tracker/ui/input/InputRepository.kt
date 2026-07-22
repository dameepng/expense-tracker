package com.example.expense_tracker.ui.input

import com.example.expense_tracker.data.Category

import kotlinx.coroutines.flow.Flow

interface InputRepository {
    fun getCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: String): Flow<List<Category>>
    fun getExpenseById(id: Long): com.example.expense_tracker.data.Expense?
    fun insertExpense(amount: Long, categoryId: Long, description: String, timestamp: Long, type: String, walletId: Long = 1L, id: Long = 0L)
}
