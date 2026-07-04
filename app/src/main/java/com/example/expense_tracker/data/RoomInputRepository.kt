package com.example.expense_tracker.data

import com.example.expense_tracker.ui.input.InputRepository

class RoomInputRepository(
    private val dao: ExpenseDao
) : InputRepository {

    override fun getCategories(): List<Category> = dao.getAllCategories()

    override fun insertExpense(amount: Long, categoryId: Long, timestamp: Long) {
        dao.insertExpense(
            Expense(amount = amount, categoryId = categoryId, timestamp = timestamp)
        )
    }
}
