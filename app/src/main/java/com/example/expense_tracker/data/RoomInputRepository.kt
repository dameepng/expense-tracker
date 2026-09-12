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
        // The manual editor does not edit AI metadata, so keep it when replacing a row.
        val existing = if (id != 0L) dao.getExpenseById(id) else null
        val expense = Expense(
            id = id,
            amount = amount,
            categoryId = categoryId,
            description = description,
            timestamp = timestamp,
            type = type,
            walletId = walletId,
            merchant = existing?.merchant.orEmpty(),
            isRecurring = existing?.isRecurring ?: false
        )
        dao.insertExpense(expense)
    }
}
