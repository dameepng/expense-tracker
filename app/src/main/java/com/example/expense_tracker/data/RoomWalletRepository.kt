package com.example.expense_tracker.data

/**
 * Room-backed implementation of WalletRepository.
 */
class RoomWalletRepository(
    private val dao: WalletDao
) : WalletRepository {

    override fun getAllWallets(): List<Wallet> {
        return dao.getAllWallets()
    }

    override fun getWalletById(id: Long): Wallet? {
        return dao.getWalletById(id)
    }

    override fun insertWallet(wallet: Wallet) {
        dao.insertWallet(wallet)
    }

    override fun deleteWallet(wallet: Wallet) {
        dao.deleteWallet(wallet)
    }
}
