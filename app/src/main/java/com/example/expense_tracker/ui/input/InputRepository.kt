package com.example.expense_tracker.ui.input

import com.example.expense_tracker.data.Category

interface InputRepository {
    fun getCategories(): List<Category>
    fun insertExpense(amount: Long, categoryId: Long, timestamp: Long)
}
