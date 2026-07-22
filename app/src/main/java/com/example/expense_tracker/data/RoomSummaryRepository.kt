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
        type: TransactionType
    ): Flow<List<CategoryBreakdown>> =
        dao.getBreakdownByCategoryAndType(startTime, endTime, type.name)
}
