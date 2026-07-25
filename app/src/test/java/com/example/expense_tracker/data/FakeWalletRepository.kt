package com.example.expense_tracker.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeWalletRepository : WalletRepository {
    private val _walletsFlow = MutableStateFlow<Map<Long, Wallet>>(emptyMap())
    private var nextId = 1L

    override fun getAllWallets(): Flow<List<Wallet>> {
        return _walletsFlow.map { it.values.toList() }
    }

    override fun getWalletById(id: Long): Wallet? {
        return _walletsFlow.value[id]
    }

    override fun insertWallet(wallet: Wallet) {
        val newId = if (wallet.id == 0L) nextId++ else wallet.id
        val newWallet = wallet.copy(id = newId)
        _walletsFlow.update { it + (newId to newWallet) }
    }

    override fun updateWallet(wallet: Wallet) {
        if (_walletsFlow.value.containsKey(wallet.id)) {
            _walletsFlow.update { it + (wallet.id to wallet) }
        }
    }

    override fun deleteWallet(wallet: Wallet) {
        _walletsFlow.update { it - wallet.id }
    }

    override fun getComputedBalance(walletId: Long): Long {
        return 0L
    }
}
