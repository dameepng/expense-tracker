package com.example.expense_tracker.data.ai

import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet
import java.time.ZoneId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RoomTransactionDraftRepository(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val zoneIdProvider: () -> ZoneId = { ZoneId.systemDefault() }
) : TransactionDraftRepository {
    override fun getCategories(): Flow<List<Category>> =
        database.expenseDao().getAllCategories()

    override fun getWallets(): Flow<List<Wallet>> = database.walletDao().getAllWallets()

    override suspend fun save(transaction: ParsedTransaction, walletId: Long) {
        withContext(ioDispatcher) {
            database.runInTransaction {
                val category = database.expenseDao().getCategoryById(transaction.categoryId)
                require(category != null && category.type in setOf(transaction.type, "BOTH")) {
                    "Kategori tidak sesuai dengan jenis transaksi. Pilih kategori lain."
                }
                require(database.walletDao().getWalletById(walletId) != null) {
                    "Dompet tidak tersedia. Pilih dompet lain."
                }
                database.expenseDao().insertExpense(
                    Expense(
                        amount = transaction.amount,
                        categoryId = transaction.categoryId,
                        description = transaction.note,
                        timestamp = transaction.date.atStartOfDay(zoneIdProvider()).toInstant().toEpochMilli(),
                        type = transaction.type,
                        walletId = walletId,
                        merchant = transaction.merchant,
                        isRecurring = transaction.isRecurring
                    )
                )
            }
        }
    }
}
