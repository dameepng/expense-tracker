package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import kotlinx.coroutines.flow.Flow

/** Categories, wallets, and confirmed drafts backed by the app's existing transaction store. */
interface TransactionDraftRepository {
    fun getCategories(): Flow<List<Category>>
    fun getWallets(): Flow<List<Wallet>>
    suspend fun save(transaction: ParsedTransaction, walletId: Long)
}
