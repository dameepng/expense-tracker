package com.example.expense_tracker.data.ai.receipt

data class ReceiptResponse(
    val amount: Long? = null,
    val category: String? = null,
    val merchant: String? = null,
    val date: String? = null,
    val note: String? = null,
    val items: List<String>? = null,
    val isRecurring: Boolean? = null,
    val error: String? = null
)

enum class ReceiptParseError {
    UNCLEAR_RECEIPT, NOT_A_RECEIPT, MISSING_TOTAL, INVALID_RESPONSE
}

class ReceiptParseException(val reason: ReceiptParseError) : Exception(reason.name)
