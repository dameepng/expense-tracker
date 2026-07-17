package com.example.expense_tracker.ui.wallet

import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.Wallet

data class WalletDetailUiState(
    val wallet: Wallet? = null,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val isLoading: Boolean = false
)
