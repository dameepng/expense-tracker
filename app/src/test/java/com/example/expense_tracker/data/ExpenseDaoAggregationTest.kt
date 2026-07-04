package com.example.expense_tracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.TimeZone

/**
 * TDD tests for:
 * - getTotalExpense(startTime, endTime) — US3
 * - getExpensesBetween(startTime, endTime) — US3
 * - getBreakdownByCategory(startTime, endTime) — US4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExpenseDaoAggregationTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExpenseDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .addCallback(AppDatabase.SeedCallback())
            .allowMainThreadQueries()
            .build()
        dao = db.expenseDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ── Helper ─────────────────────────────────────────────────────

    private fun insertExpense(amount: Long, categoryId: Long, timestamp: Long) {
        dao.insertExpense(Expense(amount = amount, categoryId = categoryId, timestamp = timestamp))
    }

    private fun todayStart(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    // ── getTotalExpense Tests ──────────────────────────────────────

    @Test
    fun `getTotalExpense returns sum of amounts in range`() {
        val categories = dao.getAllCategories()
        val start = todayStart()
        val end = start + 86_400_000L // full day

        insertExpense(10_000L, categories[0].id, start + 1000)
        insertExpense(20_000L, categories[1].id, start + 2000)
        insertExpense(30_000L, categories[0].id, start + 3000)

        val total = dao.getTotalExpense(start, end)
        assertEquals(60_000L, total)
    }

    @Test
    fun `getTotalExpense excludes expenses outside range`() {
        val categories = dao.getAllCategories()
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(10_000L, categories[0].id, start + 1000)
        insertExpense(99_000L, categories[0].id, start - 1000)   // before range
        insertExpense(99_000L, categories[0].id, end + 1000)      // after range

        val total = dao.getTotalExpense(start, end)
        assertEquals(10_000L, total)
    }

    @Test
    fun `getTotalExpense returns 0 when no expenses in range`() {
        val start = todayStart()
        val end = start + 86_400_000L

        val total = dao.getTotalExpense(start, end)
        assertEquals(0L, total)
    }

    @Test
    fun `getTotalExpense handles boundary — inclusive start, exclusive end`() {
        val categories = dao.getAllCategories()
        val start = todayStart()
        val end = start + 86_400_000L

        // Exactly at start boundary → counted
        insertExpense(10_000L, categories[0].id, start)
        // Exactly at end boundary → NOT counted (timestamp < end)
        insertExpense(30_000L, categories[0].id, end)

        val total = dao.getTotalExpense(start, end)
        assertEquals(10_000L, total)
    }

    // ── getExpensesBetween Tests ───────────────────────────────────

    @Test
    fun `getExpensesBetween returns expenses in timestamp DESC order`() {
        val categories = dao.getAllCategories()
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(10_000L, categories[0].id, start + 1000)
        insertExpense(30_000L, categories[1].id, start + 3000)
        insertExpense(20_000L, categories[0].id, start + 2000)

        val results = dao.getExpensesBetween(start, end)
        assertEquals(3, results.size)
        // newest first (DESC)
        assertEquals(30_000L, results[0].amount)
        assertEquals(20_000L, results[1].amount)
        assertEquals(10_000L, results[2].amount)
    }

    @Test
    fun `getExpensesBetween returns empty list when no data in range`() {
        val start = todayStart()
        val end = start + 86_400_000L

        val results = dao.getExpensesBetween(start, end)
        assertTrue(results.isEmpty())
    }

    @Test
    fun `getExpensesBetween excludes outside-range expenses`() {
        val categories = dao.getAllCategories()
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(10_000L, categories[0].id, start + 1000)
        insertExpense(99_000L, categories[0].id, start - 1000)

        val results = dao.getExpensesBetween(start, end)
        assertEquals(1, results.size)
        assertEquals(10_000L, results[0].amount)
    }

    // ── getBreakdownByCategory Tests ───────────────────────────────

    @Test
    fun `getBreakdownByCategory returns sum per category in range`() {
        val categories = dao.getAllCategories()
        val makanan = categories.first { it.name == "Makanan" }
        val transport = categories.first { it.name == "Transport" }
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(10_000L, makanan.id, start + 1000)
        insertExpense(20_000L, makanan.id, start + 2000)
        insertExpense(15_000L, transport.id, start + 3000)

        val breakdown = dao.getBreakdownByCategory(start, end)
        assertEquals(2, breakdown.size)

        val makananBreakdown = breakdown.first { it.categoryName == "Makanan" }
        assertEquals(30_000L, makananBreakdown.totalAmount)

        val transportBreakdown = breakdown.first { it.categoryName == "Transport" }
        assertEquals(15_000L, transportBreakdown.totalAmount)
    }

    @Test
    fun `getBreakdownByCategory returns empty list when no data`() {
        val start = todayStart()
        val end = start + 86_400_000L

        val breakdown = dao.getBreakdownByCategory(start, end)
        assertTrue(breakdown.isEmpty())
    }

    @Test
    fun `getBreakdownByCategory excludes outside-range expenses`() {
        val categories = dao.getAllCategories()
        val makanan = categories.first { it.name == "Makanan" }
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(10_000L, makanan.id, start + 1000)
        insertExpense(50_000L, makanan.id, end + 1000)  // outside range

        val breakdown = dao.getBreakdownByCategory(start, end)
        assertEquals(1, breakdown.size)
        assertEquals(10_000L, breakdown[0].totalAmount)
    }

    @Test
    fun `getBreakdownByCategory returns correct categoryId and categoryName`() {
        val categories = dao.getAllCategories()
        val hiburan = categories.first { it.name == "Hiburan" }
        val start = todayStart()
        val end = start + 86_400_000L

        insertExpense(25_000L, hiburan.id, start + 1000)

        val breakdown = dao.getBreakdownByCategory(start, end)
        assertEquals(1, breakdown.size)
        assertEquals(hiburan.id, breakdown[0].categoryId)
        assertEquals("Hiburan", breakdown[0].categoryName)
    }
}
