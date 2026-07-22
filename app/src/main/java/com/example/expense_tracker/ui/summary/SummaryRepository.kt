package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.CategoryBreakdown
import com.example.expense_tracker.data.TransactionType

import kotlinx.coroutines.flow.Flow

interface SummaryRepository {
    fun getBreakdownByCategory(
        startTime: Long,
        endTime: Long,
        type: TransactionType = TransactionType.EXPENSE
    ): Flow<List<CategoryBreakdown>>
}
