package com.example.expense_tracker.data

/**
 * Room-backed implementation of ExpenseRepository.
 */
class RoomExpenseRepository(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getTotalExpense(startTime: Long, endTime: Long): Long {
        return dao.getTotalExpense(startTime, endTime)
    }

    override fun getTotalIncome(startTime: Long, endTime: Long): Long {
        return dao.getTotalIncome(startTime, endTime)
    }

    override fun getExpensesBetween(startTime: Long, endTime: Long): List<Expense> {
        return dao.getExpensesBetween(startTime, endTime)
    }

    override fun getAllTransactionsBetween(startTime: Long, endTime: Long): List<Expense> {
        return dao.getAllTransactionsBetween(startTime, endTime)
    }

    override fun getAllTransactions(): List<Expense> {
        return dao.getAllTransactions()
    }

    override fun getCategories(): List<Category> =
        dao.getAllCategories()

    override fun getCategoriesByType(type: String): List<Category> =
        dao.getCategoriesByType(type)
    override fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense)

    override fun insertExpense(expense: Expense) =
        dao.insertExpense(expense)

    override fun getExpenseById(id: Long): Expense? =
        dao.getExpenseById(id)

    override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
        return dao.getTotalExpenseByWallet(walletId, startTime, endTime)
    }

    override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Long {
        return dao.getTotalIncomeByWallet(walletId, startTime, endTime)
    }

    override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): List<Expense> {
        return dao.getTransactionsByWallet(walletId, startTime, endTime)
    }
}
