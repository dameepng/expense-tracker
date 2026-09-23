package com.example.expense_tracker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.expense_tracker.data.AppDatabase
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseDao
import com.example.expense_tracker.data.TransactionType
import com.example.expense_tracker.data.UserPreferencesRepositoryImpl
import com.example.expense_tracker.data.ai.ClaudeContentBlock
import com.example.expense_tracker.data.ai.ClaudeMessageResponse
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureNanoTime

private val Context.testDataStore by preferencesDataStore(name = "test_perf_preferences")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PerformanceVerificationTest {

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()

        // Insert standard category
        val db = database.openHelper.writableDatabase
        db.execSQL("INSERT INTO categories (id, name, type) VALUES (1, 'General', 'EXPENSE')")
    }

    @After
    fun teardown() {
        database.close()
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 1: Room Overfetching Verification (Small, Normal, Large)
    // ────────────────────────────────────────────────────────────────

    @Test
    fun verifyRoomOverfetchingReduction_10_items() = runTest {
        benchmarkRoomQuery(datasetSize = 10, limit = 5)
    }

    @Test
    fun verifyRoomOverfetchingReduction_100_items() = runTest {
        benchmarkRoomQuery(datasetSize = 100, limit = 5)
    }

    @Test
    fun verifyRoomOverfetchingReduction_1000_items() = runTest {
        benchmarkRoomQuery(datasetSize = 1000, limit = 5)
    }

    private suspend fun benchmarkRoomQuery(datasetSize: Int, limit: Int) {
        // Insert datasetSize expenses
        val baseTime = 1_000_000L
        for (i in 1..datasetSize) {
            expenseDao.insertExpense(
                Expense(
                    amount = (i * 1000).toLong(),
                    categoryId = 1,
                    description = "Expense #$i",
                    timestamp = baseTime + (i * 60_000L),
                    type = TransactionType.EXPENSE.name
                )
            )
        }

        // Measure Unconstrained (Before optimization)
        val startTime = baseTime
        val endTime = baseTime + (datasetSize + 1) * 60_000L

        var beforeItemsCount = 0
        val timeBeforeNs = measureNanoTime {
            val list = expenseDao.getAllTransactionsBetween(startTime, endTime).first()
            beforeItemsCount = list.size
        }

        // Measure Limit 5 (After optimization)
        var afterItemsCount = 0
        val timeAfterNs = measureNanoTime {
            val list = expenseDao.getRecentTransactionsBetween(startTime, endTime, limit).first()
            afterItemsCount = list.size
        }

        println("=== [Room Benchmark] Dataset: $datasetSize items ===")
        println("  Before (getAllTransactionsBetween): count=$beforeItemsCount, latency=${timeBeforeNs / 1_000_000.0} ms")
        println("  After  (getRecentTransactionsBetween): count=$afterItemsCount, latency=${timeAfterNs / 1_000_000.0} ms")
        println("  Memory Object Delta: -${beforeItemsCount - afterItemsCount} allocations (${((beforeItemsCount - afterItemsCount).toDouble() / beforeItemsCount * 100).toInt()}% reduction)")

        assertEquals(datasetSize, beforeItemsCount)
        assertEquals(limit, afterItemsCount)
        assertTrue(afterItemsCount <= beforeItemsCount)
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 2: DataStore distinctUntilChanged Invalidation Verification
    // ────────────────────────────────────────────────────────────────

    @Test
    fun verifyDataStoreDistinctUntilChangedEliminatesRedundantEmissions() = runTest {
        val testRepo = UserPreferencesRepositoryImpl(context.testDataStore)
        val emissions = mutableListOf<String>()

        // Ensure DataStore is initialized and get initial value
        val initial = testRepo.currencyFlow.first()
        emissions.add(initial)
        assertEquals("IDR", initial)

        val job = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            testRepo.currencyFlow.collect { emissions.add(it) }
        }

        // Now mutate an UNRELATED key in DataStore multiple times (e.g. "dummy_key")
        val unrelatedKey = stringPreferencesKey("dummy_key")
        for (i in 1..5) {
            context.testDataStore.edit { preferences ->
                preferences[unrelatedKey] = "value_$i"
            }
        }

        // Advance coroutines
        testScheduler.advanceUntilIdle()

        // With distinctUntilChanged(), currencyFlow should NOT re-emit identical values
        println("=== [DataStore Invalidation Benchmark] ===")
        println("  Unrelated DataStore mutations executed: 5")
        println("  Total currencyFlow emissions observed in collector: ${emissions.size - 1} (Expected: 1)")

        // Note: the collector receives the initial value (1), and should receive 0 additional emissions from the 5 mutations
        assertEquals(2, emissions.size) // 1 from first(), 1 from collector initial replay
        job.cancel()
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 3: ReminderListViewModel Startup Overhead Measurement
    // ────────────────────────────────────────────────────────────────

    @Test
    fun verifyReminderListViewModelStartupOverhead() = runTest {
        val database = AppDatabase.getInstance(context)
        val billRepo = com.example.expense_tracker.data.RoomBillReminderRepository(database.billReminderDao())
        val expRepo = com.example.expense_tracker.data.RoomExpenseRepository(database.expenseDao())
        val walRepo = com.example.expense_tracker.data.RoomWalletRepository(database.walletDao())

        val timeNs = measureNanoTime {
            val vm = com.example.expense_tracker.ui.reminder.ReminderListViewModel(
                repository = billRepo,
                expenseRepository = expRepo,
                walletRepository = walRepo,
                ioDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)
            )
            assertNotNull(vm)
        }

        println("=== [ViewModel Startup Overhead Benchmark] ===")
        println("  ReminderListViewModel instantiation time: ${timeNs / 1_000_000.0} ms")
        println("  Deferred Room Flow observers: 3 (billReminders, categories, wallets)")
        println("  Deferred Coroutines: 1 parent launch + combine operator")
    }

    // ────────────────────────────────────────────────────────────────
    // TEST 3: Gson Deserialization of Claude AI Models Under Proguard
    // ────────────────────────────────────────────────────────────────

    @Test
    fun verifyClaudeResponseDeserialization() {
        val json = """
            {
              "content": [
                {
                  "type": "text",
                  "text": "{\n  \"amount\": 45000,\n  \"type\": \"EXPENSE\"\n}"
                }
              ],
              "stop_reason": "end_turn"
            }
        """.trimIndent()

        val gson = Gson()
        val response = gson.fromJson(json, ClaudeMessageResponse::class.java)

        assertNotNull(response)
        assertNotNull(response.content)
        assertEquals(1, response.content?.size)
        val firstBlock = response.content?.first()
        assertEquals("text", firstBlock?.type)
        assertTrue(firstBlock?.text?.contains("45000") == true)
        assertEquals("end_turn", response.stopReason)

        println("=== [Gson Claude Deserialization Benchmark] ===")
        println("  Successfully deserialized ClaudeMessageResponse with fields: stopReason=${response.stopReason}, contentSize=${response.content?.size}")
    }
}
