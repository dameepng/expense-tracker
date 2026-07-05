package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.CategoryBreakdown

interface SummaryRepository {
    fun getBreakdownByCategory(startTime: Long, endTime: Long): List<CategoryBreakdown>
}
