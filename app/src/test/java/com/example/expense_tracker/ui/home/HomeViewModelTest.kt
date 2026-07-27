package com.example.expense_tracker.ui.home

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.FakeBillReminderRepository
import com.example.expense_tracker.data.CategoryBreakdown
import com.example.expense_tracker.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    private lateinit var fakeWalletRepository: FakeWalletRepository
    private lateinit var fakeBillReminderRepository: FakeBillReminderRepository
    private lateinit var fakeUserPreferencesRepository: FakeUserPreferencesRepository
    private val testDispatcher = StandardTestDispatcher()

    // ── Fake Repository ────────────────────────────────────────────

    private class FakeWalletRepository(
        private var wallets: List<com.example.expense_tracker.data.Wallet> = emptyList()
    ) : com.example.expense_tracker.data.WalletRepository {
        private val _flow = kotlinx.coroutines.flow.MutableStateFlow(wallets)
        override fun getAllWallets(): kotlinx.coroutines.flow.Flow<List<com.example.expense_tracker.data.Wallet>> = _flow
        override fun getWalletById(id: Long): com.example.expense_tracker.data.Wallet? = _flow.value.find { it.id == id }
        override fun insertWallet(wallet: com.example.expense_tracker.data.Wallet) { 
            wallets = wallets + wallet
            _flow.value = wallets 
        }
        override fun updateWallet(wallet: com.example.expense_tracker.data.Wallet) {
            wallets = wallets.map { if (it.id == wallet.id) wallet else it }
            _flow.value = wallets
        }
        override fun deleteWallet(wallet: com.example.expense_tracker.data.Wallet) { 
            wallets = wallets.filterNot { it.id == wallet.id }
            _flow.value = wallets 
        }
        override fun getComputedBalance(walletId: Long): Long = 0L
        fun setData(newWallets: List<com.example.expense_tracker.data.Wallet>) { 
            wallets = newWallets
            _flow.value = newWallets
        }
    }

    private class FakeUserPreferencesRepository : com.example.expense_tracker.data.UserPreferencesRepository {
        private val _flow = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
        override val selectedWalletIdFlow: kotlinx.coroutines.flow.Flow<Long?> = _flow
        
        private val _themeFlow = kotlinx.coroutines.flow.MutableStateFlow("System Default")
        override val themeModeFlow: kotlinx.coroutines.flow.Flow<String> = _themeFlow
        
        private val _currencyFlow = kotlinx.coroutines.flow.MutableStateFlow("IDR")
        override val currencyFlow: kotlinx.coroutines.flow.Flow<String> = _currencyFlow
        
        private val _languageFlow = kotlinx.coroutines.flow.MutableStateFlow("Indonesia")
        override val languageFlow: kotlinx.coroutines.flow.Flow<String> = _languageFlow

        private val _isBiometricsEnabledFlow = kotlinx.coroutines.flow.MutableStateFlow(false)
        override val isBiometricsEnabledFlow: kotlinx.coroutines.flow.Flow<Boolean> = _isBiometricsEnabledFlow

        private val _userNameFlow = kotlinx.coroutines.flow.MutableStateFlow("Adam")
        override val userNameFlow: kotlinx.coroutines.flow.Flow<String> = _userNameFlow



        private val _userPhotoUriFlow = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
        override val userPhotoUriFlow: kotlinx.coroutines.flow.Flow<String?> = _userPhotoUriFlow

        override suspend fun saveSelectedWalletId(walletId: Long?) {
            _flow.value = walletId
        }
        
        override suspend fun saveThemeMode(mode: String) {
            _themeFlow.value = mode
        }
        
        override suspend fun saveCurrency(currency: String) {
            _currencyFlow.value = currency
        }
        
        override suspend fun saveLanguage(language: String) {
            _languageFlow.value = language
        }
        
        override suspend fun saveBiometricsEnabled(enabled: Boolean) {
            _isBiometricsEnabledFlow.value = enabled
        }
        
        override suspend fun saveUserProfile(name: String, photoUri: String?) {
            _userNameFlow.value = name
            _userPhotoUriFlow.value = photoUri
        }
        
        override suspend fun clearAllPreferences() {
            // clear fake
        }
    }

    private class FakeExpenseRepository(
        private var expenses: List<Expense> = emptyList(),
        private var categories: List<Category> = listOf()
    ) : ExpenseRepository {

        override fun getTotalExpense(startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<Long> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sumOf { it.amount })
        }
        
        override fun getTotalIncome(startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<Long> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.INCOME.name }
                .sumOf { it.amount })
        }

        override fun getExpensesBetween(startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<List<Expense>> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sortedByDescending { it.timestamp })
        }
        
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<List<Expense>> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.timestamp in startTime until endTime }
                .sortedByDescending { it.timestamp })
        }

        override fun getAllTransactions(): List<Expense> {
            return expenses.sortedByDescending { it.timestamp }
        }

        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<List<Expense>> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime }
                .sortedByDescending { it.timestamp })
        }

        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<Long> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime && it.type == TransactionType.EXPENSE.name }
                .sumOf { it.amount })
        }

        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): kotlinx.coroutines.flow.Flow<Long> = kotlinx.coroutines.flow.flow {
            emit(expenses
                .filter { it.walletId == walletId && it.timestamp in startTime until endTime && it.type == TransactionType.INCOME.name }
                .sumOf { it.amount })
        }

        override fun getCategories(): kotlinx.coroutines.flow.Flow<List<Category>> = kotlinx.coroutines.flow.flow { emit(categories.toList()) }
        override fun getCategoriesByType(type: String): kotlinx.coroutines.flow.Flow<List<Category>> = kotlinx.coroutines.flow.flow {
            emit(categories.filter { it.type == type || it.type == "BOTH" })
        }

        fun setData(expenses: List<Expense>, categories: List<Category>) {
            this.expenses = expenses.toList()
            this.categories = categories.toList()
        }

        override fun deleteExpense(expense: Expense) {
            expenses = expenses.filterNot { it.id == expense.id }
        }

        override fun insertExpense(expense: Expense) {
            expenses = expenses + expense
        }

        override fun getExpenseById(id: Long): Expense? {
            return expenses.find { it.id == id }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────

    private fun todayMidnight(): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private val oneDay = 86_400_000L

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeExpenseRepository()
        fakeWalletRepository = FakeWalletRepository()
        fakeBillReminderRepository = FakeBillReminderRepository()
        fakeUserPreferencesRepository = FakeUserPreferencesRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initAndAdvance() {
        viewModel = HomeViewModel(fakeRepository, fakeWalletRepository, fakeBillReminderRepository, fakeUserPreferencesRepository, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    // ── Total & List Tests ─────────────────────────────────────────

    @Test
    fun `total reflects sum of incomes minus expenses`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Gaji"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 10_000L, categoryId = 1, timestamp = today + 1000, type = TransactionType.EXPENSE.name),
                Expense(amount = 50_000L, categoryId = 2, timestamp = today + 2000, type = TransactionType.INCOME.name),
                Expense(amount = 20_000L, categoryId = 1, timestamp = today + 3000, type = TransactionType.EXPENSE.name)
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(30_000L, state.totalExpense)
        assertEquals(50_000L, state.totalIncome)
        assertEquals(20_000L, state.totalAmount) // 50k - 30k
        assertEquals(3, state.transactions.size)
    }

    @Test
    fun `wallets are loaded successfully`() {
        fakeWalletRepository.setData(listOf(
            com.example.expense_tracker.data.Wallet(1, "Cash", 50_000L),
            com.example.expense_tracker.data.Wallet(2, "BCA", 150_000L)
        ))
        initAndAdvance()
        
        val state = viewModel.uiState.value
        assertEquals(2, state.wallets.size)
        assertEquals("Cash", state.wallets[0].name)
        assertEquals("BCA", state.wallets[1].name)
    }

    @Test
    fun `list items include category name via join`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"), Category(2, "Gaji"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(amount = 15_000L, categoryId = 1, timestamp = today + 1000, type = TransactionType.EXPENSE.name),
                Expense(amount = 25_000L, categoryId = 2, timestamp = today + 2000, type = TransactionType.INCOME.name),
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(2, state.transactions.size)
        // DESC order: newest first
        assertEquals("Gaji", state.transactions[0].categoryName)
        assertEquals("Makanan", state.transactions[1].categoryName)
    }

    // ── Empty State Tests ──────────────────────────────────────────

    @Test
    fun `empty state when no expenses`() {
        initAndAdvance()
        val state = viewModel.uiState.value
        assertEquals(0L, state.totalAmount)
        assertEquals(0L, state.totalIncome)
        assertEquals(0L, state.totalExpense)
        assertTrue(state.transactions.isEmpty())
    }

    // ── Transaction Limit Tests ─────────────────────────────────────

    @Test
    fun `transactions are limited to 5 most recent`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"))
        val expenses = (1..10).map { i ->
            Expense(
                id = i.toLong(),
                amount = 1000L * i,
                categoryId = 1,
                timestamp = today + (i * 1000L),
                type = TransactionType.EXPENSE.name
            )
        }
        fakeRepository.setData(expenses = expenses, categories = categories)
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(5, state.transactions.size)
        // Verify they are the 5 most recent (DESC order: 10, 9, 8, 7, 6)
        assertEquals(10_000L, state.transactions[0].amount)
        assertEquals(6_000L, state.transactions[4].amount)
    }

    @Test
    fun `fewer than 5 transactions are all shown`() {
        val today = todayMidnight()
        val categories = listOf(Category(1, "Makanan"))
        fakeRepository.setData(
            expenses = listOf(
                Expense(id = 1, amount = 5000L, categoryId = 1, timestamp = today + 1000, type = TransactionType.EXPENSE.name),
                Expense(id = 2, amount = 8000L, categoryId = 1, timestamp = today + 2000, type = TransactionType.EXPENSE.name),
                Expense(id = 3, amount = 3000L, categoryId = 1, timestamp = today + 3000, type = TransactionType.EXPENSE.name)
            ),
            categories = categories
        )
        initAndAdvance()

        val state = viewModel.uiState.value
        assertEquals(3, state.transactions.size)
    }
}
