package com.example.expense_tracker.data

/**
 * Aggregate result from breakdown-by-category query.
 * Used for US4: Summary screen per-kategori breakdown.
 */
data class CategoryBreakdown(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Long
)
