package com.example.expense_tracker.data

/**
 * Repository abstraction for wallet data access.
 * Allows ViewModel to be tested with a fake implementation.
 */
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun getAllWallets(): Flow<List<Wallet>>
    fun getWalletById(id: Long): Wallet?
    fun insertWallet(wallet: Wallet)
    fun deleteWallet(wallet: Wallet)
    fun getComputedBalance(walletId: Long): Long
}
