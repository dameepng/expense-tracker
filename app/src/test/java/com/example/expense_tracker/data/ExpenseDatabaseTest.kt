package com.example.expense_tracker.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * TDD tests for:
 * - Category entity + preset seeding (US2)
 * - Expense entity + CRUD (US1)
 * - Validation: amount must be > 0
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExpenseDatabaseTest {

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

    // ── Category Tests ────────────────────────────────────────────

    @Test
    fun `getAllCategories returns 11 preset categories after first install`() = runBlocking {
        val categories = dao.getAllCategories().first()
        assertEquals(11, categories.size)

        val expectedNames = setOf(
            "Makanan", "Transport", "Belanja",
            "Hiburan", "Tagihan", "Kesehatan", "Lainnya",
            "Gaji", "Freelance", "Bonus", "Transfer Masuk"
        )
        val actualNames = categories.map { it.name }.toSet()
        assertEquals(expectedNames, actualNames)
    }

    @Test
    fun `getCategoryById returns correct category`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val target = categories.first()

        val result = dao.getCategoryById(target.id)
        assertNotNull(result)
        assertEquals(target.name, result!!.name)
    }

    @Test
    fun `getCategoryById returns null for non-existent id`() {
        val result = dao.getCategoryById(999)
        assertNull(result)
    }

    // ── Expense Insert Tests ───────────────────────────────────────

    @Test
    fun `insertExpense persists expense successfully`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val category = categories.first()

        val expense = Expense(
            amount = 50_000L,
            categoryId = category.id,
            timestamp = System.currentTimeMillis()
        )
        dao.insertExpense(expense)

        val all = dao.getAllExpenses()
        assertEquals(1, all.size)
        assertEquals(50_000L, all[0].amount)
        assertEquals(category.id, all[0].categoryId)
    }

    @Test
    fun `getAllExpenses returns in timestamp descending order`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val category = categories.first()
        val baseTime = System.currentTimeMillis()

        // Insert 3 expenses with different timestamps
        dao.insertExpense(Expense(amount = 10_000L, categoryId = category.id, timestamp = baseTime - 2000))
        dao.insertExpense(Expense(amount = 20_000L, categoryId = category.id, timestamp = baseTime))
        dao.insertExpense(Expense(amount = 30_000L, categoryId = category.id, timestamp = baseTime - 1000))

        val all = dao.getAllExpenses()
        assertEquals(3, all.size)
        // Must be ordered by timestamp DESC (newest first)
        assertEquals(20_000L, all[0].amount)
        assertEquals(30_000L, all[1].amount)
        assertEquals(10_000L, all[2].amount)
    }

    // ── Validation Tests ──────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `insertExpense with amount 0 throws IllegalArgumentException`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val expense = Expense(
            amount = 0L,
            categoryId = categories.first().id,
            timestamp = System.currentTimeMillis()
        )
        dao.insertExpense(expense)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `insertExpense with negative amount throws IllegalArgumentException`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val expense = Expense(
            amount = -5000L,
            categoryId = categories.first().id,
            timestamp = System.currentTimeMillis()
        )
        dao.insertExpense(expense)
    }

    // ── Explicit ID Tests ─────────────────────────────────────────

    @Test
    fun `expense id is auto-generated and unique`() = runBlocking {
        val categories = dao.getAllCategories().first()
        val cat = categories.first()
        val now = System.currentTimeMillis()

        dao.insertExpense(Expense(amount = 10_000L, categoryId = cat.id, timestamp = now))
        dao.insertExpense(Expense(amount = 20_000L, categoryId = cat.id, timestamp = now + 1))

        val all = dao.getAllExpenses()
        assertEquals(2, all.size)
        assertNotEquals(all[0].id, all[1].id)
        assertTrue(all[0].id > 0)
        assertTrue(all[1].id > 0)
    }

    @Test
    fun `category id is auto-generated`() = runBlocking {
        val categories = dao.getAllCategories().first()
        for (cat in categories) {
            assertTrue(cat.id > 0)
        }
    }
}
