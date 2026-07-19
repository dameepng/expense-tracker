package com.example.expense_tracker.ui.input

import com.example.expense_tracker.data.Category

enum class InputMode {
    TRANSACTION, BILL_REMINDER
}

data class InputUiState(
    val inputMode: InputMode = InputMode.TRANSACTION,
    val amountText: String = "",
    val description: String = "",
    val billReminderName: String = "",
    val billReminderDueDay: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isSaveEnabled: Boolean = false,
    val saved: Boolean = false,
    val transactionType: com.example.expense_tracker.data.TransactionType = com.example.expense_tracker.data.TransactionType.EXPENSE,
    val wallets: List<com.example.expense_tracker.data.Wallet> = emptyList(),
    val selectedWalletId: Long? = null
)
