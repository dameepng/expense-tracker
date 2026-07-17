package com.example.expense_tracker.ui.wallet

import com.example.expense_tracker.data.Wallet

data class WalletUiState(
    val wallets: List<Wallet> = emptyList(),
    val isLoading: Boolean = false
)
