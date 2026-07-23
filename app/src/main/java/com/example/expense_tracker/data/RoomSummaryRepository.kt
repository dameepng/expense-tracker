package com.example.expense_tracker.data

import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.ui.summary.SummaryRepository

import kotlinx.coroutines.flow.Flow

class RoomSummaryRepository(
    private val dao: ExpenseDao
) : SummaryRepository {

    override fun getBreakdownByCategory(
        startTime: Long,
        endTime: Long,
        type: TransactionType,
        walletId: Long?
    ): Flow<List<CategoryBreakdown>> =
        if (walletId != null) {
            dao.getBreakdownByCategoryAndTypeAndWallet(startTime, endTime, type.name, walletId)
        } else {
            dao.getBreakdownByCategoryAndType(startTime, endTime, type.name)
        }

    override fun getTotalBalance(walletId: Long?): Flow<Long> =
        if (walletId != null) {
            dao.getTotalBalanceByWallet(walletId)
        } else {
            dao.getTotalBalance()
        }

    override fun getTotalIncome(startTime: Long, endTime: Long, walletId: Long?): Flow<Long> =
        if (walletId != null) {
            dao.getTotalIncomeByWallet(walletId, startTime, endTime)
        } else {
            dao.getTotalIncome(startTime, endTime)
        }

    override fun getTotalExpense(startTime: Long, endTime: Long, walletId: Long?): Flow<Long> =
        if (walletId != null) {
            dao.getTotalExpenseByWallet(walletId, startTime, endTime)
        } else {
            dao.getTotalExpense(startTime, endTime)
        }

    override fun getTransactionsBetween(startTime: Long, endTime: Long, walletId: Long?): Flow<List<Expense>> =
        if (walletId != null) {
            dao.getTransactionsByWallet(walletId, startTime, endTime)
        } else {
            dao.getAllTransactionsBetween(startTime, endTime)
        }
}
