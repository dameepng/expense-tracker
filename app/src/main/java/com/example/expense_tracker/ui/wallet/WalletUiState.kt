package com.example.expense_tracker.ui.wallet

import androidx.compose.runtime.Immutable
import com.example.expense_tracker.data.Wallet

@Immutable
data class WalletUiState(
    val wallets: List<Wallet> = emptyList(),
    val balanceMap: Map<Long, Long> = emptyMap(),
    val isLoading: Boolean = false
)
