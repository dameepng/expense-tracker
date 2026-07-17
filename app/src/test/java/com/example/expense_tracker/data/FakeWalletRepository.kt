package com.example.expense_tracker.data

class FakeWalletRepository : WalletRepository {
    private val wallets = mutableListOf<Wallet>()
    private var nextId = 1L

    override fun getAllWallets(): List<Wallet> {
        return wallets.toList()
    }

    override fun getWalletById(id: Long): Wallet? {
        return wallets.find { it.id == id }
    }

    override fun insertWallet(wallet: Wallet) {
        val newWallet = wallet.copy(id = if (wallet.id == 0L) nextId++ else wallet.id)
        wallets.removeIf { it.id == newWallet.id }
        wallets.add(newWallet)
    }

    override fun deleteWallet(wallet: Wallet) {
        wallets.removeIf { it.id == wallet.id }
    }
}
