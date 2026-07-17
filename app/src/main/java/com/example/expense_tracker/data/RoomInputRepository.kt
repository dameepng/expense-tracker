package com.example.expense_tracker.data

import com.example.expense_tracker.ui.input.InputRepository

class RoomInputRepository(
    private val dao: ExpenseDao
) : InputRepository {

    override fun getCategories(): List<Category> = dao.getAllCategories()

    override fun getExpenseById(id: Long) = dao.getExpenseById(id)

    override fun insertExpense(amount: Long, categoryId: Long, description: String, timestamp: Long, type: String, id: Long) {
        val expense = Expense(
            id = id,
            amount = amount,
            categoryId = categoryId,
            description = description,
            timestamp = timestamp,
            type = type
        )
        dao.insertExpense(expense)
    }
}
