package com.example.expense_tracker.data.analytics

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseDao
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.Wallet
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomFinancialSummarySourceTest {
    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(AppDatabase.SeedCallback())
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `aggregates all wallets once and filters non-expense transaction types`() = runBlocking {
        val categories = expenseDao.getAllCategories().first()
        val food = categories.first { it.name == "Makanan" }
        val salary = categories.first { it.name == "Gaji" }
        database.walletDao().insertWallet(Wallet(name = "Second wallet"))
        val wallets = database.walletDao().getAllWallets().first()

        insert(30_000L, food.id, wallets[0].id, "2026-09-05T08:00:00Z")
        insert(20_000L, food.id, wallets[1].id, "2026-09-10T08:00:00Z")
        insert(100_000L, salary.id, wallets[0].id, "2026-09-10T09:00:00Z", TransactionType.INCOME.name)
        insert(70_000L, food.id, wallets[0].id, "2026-09-10T10:00:00Z", "UNKNOWN")
        insert(40_000L, food.id, wallets[0].id, "2026-08-20T08:00:00Z")
        insert(99_000L, food.id, wallets[0].id, "2026-09-12T10:00:00Z")

        val summary = aggregator().createSnapshot()

        assertEquals(50_000L, summary.currentMonth.totalExpense)
        assertEquals(50_000L, summary.currentMonth.categories.single().totalAmount)
        assertEquals(40_000L, summary.previousMonth.totalExpense)
        assertEquals(10_000L, summary.monthComparison.amountDifference)
    }

    @Test
    fun `a later snapshot reads newly inserted transactions`() = runBlocking {
        val food = expenseDao.getAllCategories().first().first { it.name == "Makanan" }
        val wallet = database.walletDao().getAllWallets().first().first()
        insert(10_000L, food.id, wallet.id, "2026-09-05T08:00:00Z")

        val first = aggregator().createSnapshot()
        insert(15_000L, food.id, wallet.id, "2026-09-10T08:00:00Z")
        val second = aggregator().createSnapshot()

        assertEquals(10_000L, first.currentMonth.totalExpense)
        assertEquals(25_000L, second.currentMonth.totalExpense)
    }

    @Test
    fun `room source returns only the requested half-open range and minimum fields`() = runBlocking {
        val food = expenseDao.getAllCategories().first().first { it.name == "Makanan" }
        val wallet = database.walletDao().getAllWallets().first().first()
        val start = Instant.parse("2026-09-01T00:00:00Z").toEpochMilli()
        val end = Instant.parse("2026-10-01T00:00:00Z").toEpochMilli()
        insert(10L, food.id, wallet.id, "2026-09-01T00:00:00Z")
        insert(20L, food.id, wallet.id, "2026-09-30T23:59:59.999Z")
        insert(30L, food.id, wallet.id, "2026-10-01T00:00:00Z")

        val entries = source().loadEntries(start, end)

        assertEquals(listOf(10L, 20L), entries.map { it.amount })
        assertEquals(listOf(food.id, food.id), entries.map { it.categoryId })
        assertEquals(listOf("Makanan", "Makanan"), entries.map { it.categoryName })
    }

    private fun aggregator(): TransactionAggregator = TransactionAggregator(
        source = source(),
        periodResolver = FinancialPeriodResolver(
            clock = Clock.fixed(SNAPSHOT, ZONE_ID),
            zoneId = ZONE_ID,
            locale = Locale.US,
            firstDayOfWeek = DayOfWeek.MONDAY
        ),
        calculationDispatcher = Dispatchers.Unconfined
    )

    private fun source() = RoomFinancialSummarySource(
        expenseDao = expenseDao,
        ioDispatcher = Dispatchers.Unconfined
    )

    private fun insert(
        amount: Long,
        categoryId: Long,
        walletId: Long,
        timestamp: String,
        type: String = TransactionType.EXPENSE.name
    ) {
        expenseDao.insertExpense(
            Expense(
                amount = amount,
                categoryId = categoryId,
                timestamp = Instant.parse(timestamp).toEpochMilli(),
                type = type,
                walletId = walletId
            )
        )
    }

    private companion object {
        val SNAPSHOT: Instant = Instant.parse("2026-09-12T10:00:00Z")
        val ZONE_ID: ZoneId = ZoneId.of("UTC")
    }
}
