package com.example.expense_tracker.ui.home

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.ExpenseWithCategory
import com.example.expense_tracker.data.Wallet

const val DEFAULT_WALLET_NAME = "All Wallets"
const val DEFAULT_PERIOD_LABEL = "Bulan Ini"
const val DEFAULT_USER_NAME = "Pelanggan"

@Immutable
data class HomeUiState(
    val totalAmount: Long = 0L,
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val transactions: List<ExpenseWithCategory> = emptyList(),
    val wallets: List<Wallet> = emptyList(),
    val selectedWalletId: Long? = null,
    val selectedWalletName: String = DEFAULT_WALLET_NAME,
    val activeRemindersCount: Int = 0,
    val isLoading: Boolean = false,
    val periodLabel: String = DEFAULT_PERIOD_LABEL,
    val userName: String = DEFAULT_USER_NAME,
    val userPhotoUri: String? = null
)
