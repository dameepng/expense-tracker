package com.example.expense_tracker.data.ai.receipt

import android.net.Uri
import com.example.expense_tracker.data.Category
import java.time.LocalDate

data class ReceiptScanRequest(
    val imageUri: Uri,
    val referenceDate: LocalDate,
    val categories: List<Category>
)

interface ReceiptRepository {
    suspend fun scan(request: ReceiptScanRequest): ReceiptTransaction
}
