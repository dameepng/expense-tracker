package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.CategoryBreakdown
import com.example.expense_tracker.data.TransactionType

import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    fun getBreakdownByCategory(
        startTime: Long,
        endTime: Long,
        type: TransactionType = TransactionType.EXPENSE,
        walletId: Long? = null
    ): Flow<List<CategoryBreakdown>>

    fun getTotalBalance(walletId: Long? = null): Flow<Long>
    fun getTotalIncome(startTime: Long, endTime: Long, walletId: Long? = null): Flow<Long>
    fun getTotalExpense(startTime: Long, endTime: Long, walletId: Long? = null): Flow<Long>
    fun getTransactionsBetween(startTime: Long, endTime: Long, walletId: Long? = null): Flow<List<com.example.expense_tracker.data.Expense>>
}
