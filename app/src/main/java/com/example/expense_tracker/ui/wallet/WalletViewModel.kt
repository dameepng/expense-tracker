package com.example.expense_tracker.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense_tracker.data.Wallet
import com.example.expense_tracker.data.WalletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WalletViewModel(
    private val repository: WalletRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState(isLoading = true))
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(ioDispatcher) {
            val wallets = repository.getAllWallets()
            val balances = wallets.associate { wallet ->
                wallet.id to repository.getComputedBalance(wallet.id)
            }
            _uiState.update {
                it.copy(
                    wallets = wallets,
                    balanceMap = balances,
                    isLoading = false
                )
            }
        }
    }

    fun addWallet(
        name: String,
        cardNumber: String = "",
        cardHolderName: String = "",
        cardExpiry: String = "",
        color: String = ""
    ) {
        if (name.isBlank()) return
        viewModelScope.launch(ioDispatcher) {
            val newWallet = Wallet(
                name = name.trim(),
                cardNumber = cardNumber.trim(),
                cardHolderName = cardHolderName.trim(),
                cardExpiry = cardExpiry.trim(),
                color = color.trim()
            )
            repository.insertWallet(newWallet)
            refresh()
        }
    }

    fun deleteWallet(wallet: Wallet) {
        viewModelScope.launch(ioDispatcher) {
            repository.deleteWallet(wallet)
            refresh()
        }
    }
}
