package com.example.expense_tracker.ui.home

import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.Wallet

data class HomeUiState(
    val totalAmount: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null,
    val selectedWalletName: String = "All Wallets",
    val activeRemindersCount: Int = 0,
    val isLoading: Boolean = false,
    val periodLabel: String = "Bulan Ini"
)
