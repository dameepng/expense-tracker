package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import kotlinx.coroutines.flow.Flow

/** Categories, wallets, and confirmed drafts backed by the app's existing transaction store. */
interface TransactionDraftRepository {
    fun getCategories(): Flow<List<Category>>
    fun getWallets(): Flow<List<Wallet>>
    suspend fun save(transaction: ParsedTransaction, walletId: Long)
    suspend fun saveReceipt(transaction: ParsedTransaction, walletId: Long, items: List<String>) {
        save(transaction.copy(note = ReceiptNoteFormatter.format(transaction.note, items)), walletId)
    }
}

object ReceiptNoteFormatter {
    fun format(note: String, items: List<String>): String {
        val summary = items.filter { it.isNotBlank() }.joinToString(", ")
        if (summary.isBlank()) return note.take(1000)
        val suffix = "\nItem: "
        val available = (1000 - note.length - suffix.length).coerceAtLeast(0)
        val bounded = summary.take(available)
        return (note + suffix + bounded + if (bounded.length < summary.length) "…" else "").take(1000)
    }
}
