package com.example.expense_tracker.data

import com.example.expense_tracker.ui.input.InputRepository
import kotlinx.coroutines.flow.Flow

class RoomInputRepository(
    private val dao: ExpenseDao
) : InputRepository {

    override fun getCategories(): Flow<List<Category>> = dao.getAllCategories()

    override fun getCategoriesByType(type: String): Flow<List<Category>> = dao.getCategoriesByType(type)

    override fun getExpenseById(id: Long) = dao.getExpenseById(id)

    override fun insertExpense(amount: Long, categoryId: Long, description: String, timestamp: Long, type: String, walletId: Long, id: Long) {
        val expense = Expense(
            id = id,
            amount = amount,
            categoryId = categoryId,
            description = description,
            timestamp = timestamp,
            type = type,
            walletId = walletId
        )
        dao.insertExpense(expense)
    }
}
