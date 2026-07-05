package com.example.expense_tracker.data

/**
 * Room-backed implementation of ExpenseRepository.
 */
class RoomExpenseRepository(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getTotalExpense(startTime: Long, endTime: Long): Long =
        dao.getTotalExpense(startTime, endTime)

    override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> =
        dao.getExpensesBetween(startTime, endTime)

    override fun getCategories(): List<Category> =
        dao.getAllCategories()

    override fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense)

    override fun insertExpense(expense: Expense) =
        dao.insertExpense(expense)

    override fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)
}
