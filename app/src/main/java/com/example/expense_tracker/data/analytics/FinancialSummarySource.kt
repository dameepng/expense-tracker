package com.example.expense_tracker.data.analytics

/**
 * Minimum transaction projection needed to calculate financial summaries.
 * It deliberately excludes descriptions, merchants, and wallet details.
 */
data class FinancialTransactionEntry(
    val amount: Long,
    val categoryId: Long,
    val categoryName: String,
    val timestamp: Long,
    val type: String
)

interface FinancialSummarySource {
    /**
     * Returns entries in [startInclusiveEpochMillis, endExclusiveEpochMillis).
     * Implementations must read the requested range from one consistent snapshot.
     */
    suspend fun loadEntries(
        startInclusiveEpochMillis: Long,
        endExclusiveEpochMillis: Long
    ): List<FinancialTransactionEntry>
}
