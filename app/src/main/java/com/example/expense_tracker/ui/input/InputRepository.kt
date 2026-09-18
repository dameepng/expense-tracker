package com.example.expense_tracker.ui.input

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import kotlinx.coroutines.flow.Flow

const val DEFAULT_WALLET_ID = 1L

interface InputRepository {
    fun getCategories(): Flow<List<Category>>
    fun getCategoriesByType(type: String): Flow<List<Category>>
    fun getExpenseById(id: Long): Expense?
    fun insertExpense(
        amount: Long,
        categoryId: Long,
        description: String,
        timestamp: Long,
        type: String,
        walletId: Long = DEFAULT_WALLET_ID,
        id: Long = 0L
    )
}
