package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Wallet
import kotlinx.coroutines.flow.Flow

/** Categories, wallets, and confirmed drafts backed by the app's existing transaction store. */
interface TransactionDraftRepository {
    fun getCategories(): Flow<List<Category>>
    fun getWallets(): Flow<List<Wallet>>
    suspend fun save(transaction: ParsedTransaction, walletId: Long)
    suspend fun saveAll(transactions: List<ParsedTransaction>, walletId: Long) {
        transactions.forEach { save(it, walletId) }
    }
    suspend fun saveAllWithWallets(transactions: List<Pair<ParsedTransaction, Long>>) {
        transactions.forEach { (transaction, walletId) -> save(transaction, walletId) }
    }
    suspend fun saveReceipt(transaction: ParsedTransaction, walletId: Long, items: List<String>) {
        save(transaction.copy(note = ReceiptNoteFormatter.format(transaction.note, items)), walletId)
    }
}

object ReceiptNoteFormatter {
    private const val MAX_NOTE_LENGTH = 1000

    fun format(note: String, items: List<String>): String {
        val summary = items.filter { it.isNotBlank() }.joinToString(", ")
        if (summary.isBlank()) return note.take(MAX_NOTE_LENGTH)
        val suffix = "\nItem: "
        val available = (MAX_NOTE_LENGTH - note.length - suffix.length).coerceAtLeast(0)
        val bounded = summary.take(available)
        return (note + suffix + bounded + if (bounded.length < summary.length) "…" else "").take(MAX_NOTE_LENGTH)
    }
}
