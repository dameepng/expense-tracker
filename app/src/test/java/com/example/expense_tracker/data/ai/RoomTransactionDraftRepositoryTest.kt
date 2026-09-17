package com.example.expense_tracker.data.ai

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.RoomInputRepository
import com.example.expense_tracker.data.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class RoomTransactionDraftRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: RoomTransactionDraftRepository
    private val zone = ZoneId.of("Asia/Jakarta")

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .addCallback(AppDatabase.SeedCallback())
            .allowMainThreadQueries()
            .build()
        repository = RoomTransactionDraftRepository(
            database = database,
            ioDispatcher = Dispatchers.Unconfined,
            zoneIdProvider = { zone },
            timeProvider = { LocalTime.MIDNIGHT }
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `confirmed draft keeps date merchant note and recurring metadata`() = runBlocking {
        val category = repository.getCategories().first().first()
        val wallet = repository.getWallets().first().first()
        val draft = transaction(category.id)

        repository.save(draft, wallet.id)

        val saved = database.expenseDao().getAllExpenses().single()
        assertEquals(draft.amount, saved.amount)
        assertEquals(category.id, saved.categoryId)
        assertEquals(wallet.id, saved.walletId)
        assertEquals(draft.note, saved.description)
        assertEquals("Netflix", saved.merchant)
        assertTrue(saved.isRecurring)
        assertEquals(TransactionType.EXPENSE.name, saved.type)
        assertEquals(Instant.parse("2026-09-08T17:00:00Z").toEpochMilli(), saved.timestamp)
        assertEquals(draft.date, Instant.ofEpochMilli(saved.timestamp).atZone(zone).toLocalDate())
        assertTrue(database.billReminderDao().getAllReminders().isEmpty())
    }

    @Test
    fun `categories come from Room and include EXPENSE BOTH and INCOME`() = runBlocking {
        database.openHelper.writableDatabase.execSQL(
            "INSERT INTO categories (name, type) VALUES ('Kopi Kantor', 'EXPENSE')"
        )

        val categories = repository.getCategories().first()

        assertTrue(categories.any { it.name == "Kopi Kantor" })
        assertTrue(categories.any { it.type == "BOTH" })
        assertTrue(categories.any { it.type == TransactionType.INCOME.name })
    }

    @Test
    fun `income category can be saved as an income transaction`() = runBlocking {
        val category = repository.getCategories().first().first { it.type == TransactionType.INCOME.name }
        val wallet = repository.getWallets().first().first()
        val incomeDraft = transaction(category.id).copy(type = TransactionType.INCOME.name)

        repository.save(incomeDraft, wallet.id)

        val saved = database.expenseDao().getAllTransactions().single()
        assertEquals(TransactionType.INCOME.name, saved.type)
        assertEquals(category.id, saved.categoryId)
    }

    @Test
    fun `missing wallet is rejected without inserting an expense`() = runBlocking {
        val category = repository.getCategories().first().first()

        expectInvalidSelection { repository.save(transaction(category.id), 999L) }

        assertTrue(database.expenseDao().getAllTransactions().isEmpty())
    }

    @Test
    fun `missing category is rejected without inserting an expense`() = runBlocking {
        val wallet = repository.getWallets().first().first()

        expectInvalidSelection { repository.save(transaction(999L), wallet.id) }

        assertTrue(database.expenseDao().getAllTransactions().isEmpty())
    }

    @Test
    fun `income category is rejected without inserting an expense`() = runBlocking {
        val category = database.expenseDao().getAllCategories().first()
            .first { it.type == TransactionType.INCOME.name }
        val wallet = repository.getWallets().first().first()

        expectInvalidSelection { repository.save(transaction(category.id), wallet.id) }

        assertTrue(database.expenseDao().getAllTransactions().isEmpty())
    }

    @Test
    fun `BOTH category can be saved as an expense`() = runBlocking {
        val category = repository.getCategories().first().first { it.type == "BOTH" }
        val wallet = repository.getWallets().first().first()

        repository.save(transaction(category.id), wallet.id)

        assertEquals(category.id, database.expenseDao().getAllExpenses().single().categoryId)
    }

    @Test
    fun `manual edit preserves merchant and recurring metadata`() = runBlocking {
        val category = repository.getCategories().first().first()
        val wallet = repository.getWallets().first().first()
        repository.save(transaction(category.id), wallet.id)
        val original = database.expenseDao().getAllExpenses().single()

        RoomInputRepository(database.expenseDao()).insertExpense(
            amount = 150_000,
            categoryId = category.id,
            description = "Harga langganan dikoreksi",
            timestamp = original.timestamp,
            type = TransactionType.EXPENSE.name,
            walletId = wallet.id,
            id = original.id
        )

        val edited = database.expenseDao().getAllExpenses().single()
        assertEquals(original.copy(amount = 150_000, description = "Harga langganan dikoreksi"), edited)
    }

    @Test
    fun `draft saved with custom time provider captures specific hour and minute`() = runBlocking {
        val customRepo = RoomTransactionDraftRepository(
            database = database,
            ioDispatcher = Dispatchers.Unconfined,
            zoneIdProvider = { zone },
            timeProvider = { LocalTime.of(14, 30) }
        )
        val category = customRepo.getCategories().first().first()
        val wallet = customRepo.getWallets().first().first()
        val draft = transaction(category.id)

        customRepo.save(draft, wallet.id)

        val saved = database.expenseDao().getAllExpenses().first { it.amount == draft.amount }
        val zonedDateTime = Instant.ofEpochMilli(saved.timestamp).atZone(zone)
        assertEquals(14, zonedDateTime.hour)
        assertEquals(30, zonedDateTime.minute)
    }

    private fun transaction(categoryId: Long) = ParsedTransaction(
        amount = 120_000,
        categoryId = categoryId,
        merchant = "Netflix",
        date = LocalDate.of(2026, 9, 9),
        note = "langganan bulanan",
        isRecurring = true
    )

    private suspend fun expectInvalidSelection(action: suspend () -> Unit) {
        try {
            action()
            fail("Expected an invalid category or wallet to be rejected")
        } catch (_: IllegalArgumentException) {
            // Validation must finish before any transaction is inserted.
        }
    }
}
