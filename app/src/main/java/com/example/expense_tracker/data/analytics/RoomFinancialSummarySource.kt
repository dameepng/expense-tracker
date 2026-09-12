package com.example.expense_tracker.data.analytics

import com.example.expense_tracker.data.ExpenseDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomFinancialSummarySource(
    private val expenseDao: ExpenseDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FinancialSummarySource {
    override suspend fun loadEntries(
        startInclusiveEpochMillis: Long,
        endExclusiveEpochMillis: Long
    ): List<FinancialTransactionEntry> {
        require(startInclusiveEpochMillis <= endExclusiveEpochMillis) {
            "Range start must not be after its end"
        }
        if (startInclusiveEpochMillis == endExclusiveEpochMillis) return emptyList()

        return withContext(ioDispatcher) {
            expenseDao.getFinancialSummaryEntries(
                startTime = startInclusiveEpochMillis,
                endTime = endExclusiveEpochMillis
            )
        }
    }
}
