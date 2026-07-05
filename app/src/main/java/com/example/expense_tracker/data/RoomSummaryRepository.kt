package com.example.expense_tracker.data

import com.example.expense_tracker.ui.summary.SummaryRepository

class RoomSummaryRepository(
    private val dao: ExpenseDao
) : SummaryRepository {

    override fun getBreakdownByCategory(startTime: Long, endTime: Long): List<CategoryBreakdown> =
        dao.getBreakdownByCategory(startTime, endTime)
}
