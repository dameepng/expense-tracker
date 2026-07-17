package com.example.expense_tracker.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Hindari issue robolectric dengan SDK terbaru jika belum support
class ExpenseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Gunakan in-memory database untuk testing
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetIncomeAndExpenseTotals() {
        // 1. Setup category using raw query since categoryDao is not exposed directly
        val db = database.openHelper.writableDatabase
        db.execSQL("INSERT INTO categories (id, name) VALUES (1, 'Makanan')")
        db.execSQL("INSERT INTO categories (id, name) VALUES (2, 'Gaji')")

        // 2. Insert dummy data
        val expense1 = Expense(
            amount = 50000,
            categoryId = 1,
            description = "Makan Siang",
            timestamp = 1000L,
            type = TransactionType.EXPENSE.name
        )
        val expense2 = Expense(
            amount = 25000,
            categoryId = 1,
            description = "Makan Malam",
            timestamp = 2000L,
            type = TransactionType.EXPENSE.name
        )
        val income1 = Expense(
            amount = 5000000,
            categoryId = 2,
            description = "Gaji",
            timestamp = 1500L,
            type = TransactionType.INCOME.name
        )
        val income2 = Expense(
            amount = 200000,
            categoryId = 2,
            description = "Bonus",
            timestamp = 2500L,
            type = TransactionType.INCOME.name
        )

        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        expenseDao.insertExpense(income1)
        expenseDao.insertExpense(income2)

        // 3. Test Total Expense
        val totalExpense = expenseDao.getTotalExpense(0L, 3000L)
        assertEquals("Total expense harus 75000", 75000L, totalExpense)

        // 4. Test Total Income
        val totalIncome = expenseDao.getTotalIncome(0L, 3000L)
        assertEquals("Total income harus 5200000", 5200000L, totalIncome)

        // 5. Test empty period
        val emptyExpense = expenseDao.getTotalExpense(4000L, 5000L)
        assertEquals("Total expense di luar range harus 0", 0L, emptyExpense)
    }
}
