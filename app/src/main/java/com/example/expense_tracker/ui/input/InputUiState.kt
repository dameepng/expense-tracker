package com.example.expense_tracker.ui.input

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet

enum class InputMode {
    TRANSACTION, BILL_REMINDER
}

enum class InputTypeOption {
    INCOME, EXPENSE, BILL_REMINDER
}

@Immutable
data class InputUiState(
    val isLoading: Boolean = true,
    val inputTypeOption: InputTypeOption = InputTypeOption.EXPENSE,
    val inputMode: InputMode = InputMode.TRANSACTION,
    val amountText: String = "",
    val description: String = "",
    val billReminderName: String = "",
    val billReminderDueDay: String = "",
    val isRepeat: Boolean = true,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isSaveEnabled: Boolean = false,
    val saved: Boolean = false,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null
)
