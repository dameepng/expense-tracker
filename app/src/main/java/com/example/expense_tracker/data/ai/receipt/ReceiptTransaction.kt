package com.example.expense_tracker.data.ai.receipt

import com.example.expense_tracker.data.ai.ParsedTransaction

data class ReceiptTransaction(
    val transaction: ParsedTransaction,
    val items: List<String>
)
