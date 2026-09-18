package com.example.expense_tracker.ui.profile

import com.example.expense_tracker.data.Category
import com.example.expense_tracker.data.Expense
import com.example.expense_tracker.data.ExpenseRepository
import com.example.expense_tracker.data.UserPreferencesRepository
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakePrefsRepo: FakeUserPreferencesRepository
    private lateinit var fakeExpenseRepo: FakeExpenseRepository
    private lateinit var viewModel: ProfileViewModel

    private class FakeUserPreferencesRepository : UserPreferencesRepository {
        private val _selectedWalletId = MutableStateFlow<Long?>(null)
        override val selectedWalletIdFlow: Flow<Long?> = _selectedWalletId.asStateFlow()

        private val _themeMode = MutableStateFlow("System Default")
        override val themeModeFlow: Flow<String> = _themeMode.asStateFlow()

        private val _currency = MutableStateFlow("IDR")
        override val currencyFlow: Flow<String> = _currency.asStateFlow()

        private val _language = MutableStateFlow("Indonesia")
        override val languageFlow: Flow<String> = _language.asStateFlow()

        private val _isBiometricsEnabled = MutableStateFlow(false)
        override val isBiometricsEnabledFlow: Flow<Boolean> = _isBiometricsEnabled.asStateFlow()

        private val _userName = MutableStateFlow("Adam")
        override val userNameFlow: Flow<String> = _userName.asStateFlow()

        private val _userPhotoUri = MutableStateFlow<String?>(null)
        override val userPhotoUriFlow: Flow<String?> = _userPhotoUri.asStateFlow()

        override suspend fun saveSelectedWalletId(walletId: Long?) {
            _selectedWalletId.value = walletId
        }

        override suspend fun saveThemeMode(mode: String) {
            _themeMode.value = mode
        }

        override suspend fun saveCurrency(currency: String) {
            _currency.value = currency
        }

        override suspend fun saveLanguage(language: String) {
            _language.value = language
        }

        override suspend fun saveBiometricsEnabled(enabled: Boolean) {
            _isBiometricsEnabled.value = enabled
        }

        override suspend fun saveUserProfile(name: String, photoUri: String?) {
            _userName.value = name
            _userPhotoUri.value = photoUri
        }

        override suspend fun clearAllPreferences() {
            _selectedWalletId.value = null
            _themeMode.value = "System Default"
            _currency.value = "IDR"
            _language.value = "Indonesia"
            _isBiometricsEnabled.value = false
            _userName.value = "Adam"
            _userPhotoUri.value = null
        }
    }

    private class FakeExpenseRepository : ExpenseRepository {
        override fun getCategories(): Flow<List<Category>> = flowOf(emptyList())
        override fun getCategoriesByType(type: String): Flow<List<Category>> = flowOf(emptyList())
        override fun insertExpense(expense: Expense) {}
        override fun deleteExpense(expense: Expense) {}
        override fun getExpenseById(id: Long): Expense? = null
        override fun getExpensesBetween(startTime: Long, endTime: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getAllTransactionsBetween(startTime: Long, endTime: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getAllTransactions(): List<Expense> = emptyList()
        override fun getTotalExpense(startTime: Long, endTime: Long): Flow<Long> = flowOf(0L)
        override fun getTotalIncome(startTime: Long, endTime: Long): Flow<Long> = flowOf(0L)
        override fun getTotalExpenseByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long> = flowOf(0L)
        override fun getTotalIncomeByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<Long> = flowOf(0L)
        override fun getTransactionsByWallet(walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun deleteExpensesByWalletId(walletId: Long) {}
        override fun getTransactionsByCategory(categoryId: Long, startTime: Long, endTime: Long): Flow<List<Expense>> = flowOf(emptyList())
        override fun getTransactionsByCategoryAndWallet(categoryId: Long, walletId: Long, startTime: Long, endTime: Long): Flow<List<Expense>> = flowOf(emptyList())
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakePrefsRepo = FakeUserPreferencesRepository()
        fakeExpenseRepo = FakeExpenseRepository()
        viewModel = ProfileViewModel(fakePrefsRepo, fakeExpenseRepo, testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState has default values`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("System Default", state.themeMode)
        assertEquals("IDR", state.currency)
        assertEquals("Indonesia", state.language)
        assertFalse(state.isBiometricsEnabled)
        assertEquals("Adam", state.userName)
        assertEquals(null, state.userPhotoUri)

        collectJob.cancel()
    }

    @Test
    fun `setThemeMode updates themeMode in state`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.setThemeMode("Dark Mode")
        advanceUntilIdle()

        assertEquals("Dark Mode", viewModel.uiState.value.themeMode)
        collectJob.cancel()
    }

    @Test
    fun `setCurrency updates currency in state`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.setCurrency("USD")
        advanceUntilIdle()

        assertEquals("USD", viewModel.uiState.value.currency)
        collectJob.cancel()
    }

    @Test
    fun `setLanguage updates language in state`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.setLanguage("English")
        advanceUntilIdle()

        assertEquals("English", viewModel.uiState.value.language)
        collectJob.cancel()
    }

    @Test
    fun `setBiometricsEnabled updates biometric status in state`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.setBiometricsEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isBiometricsEnabled)
        collectJob.cancel()
    }

    @Test
    fun `updateProfile updates name and photoUri in state`() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        viewModel.updateProfile("Budi", "content://media/photos/1")
        advanceUntilIdle()

        assertEquals("Budi", viewModel.uiState.value.userName)
        assertEquals("content://media/photos/1", viewModel.uiState.value.userPhotoUri)
        collectJob.cancel()
    }

    @Test
    fun `formatCsvRow formats expense correctly`() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val expense = Expense(
            id = 1L,
            amount = 50000L,
            type = "EXPENSE",
            categoryId = 2L,
            walletId = 3L,
            description = "Makan Siang",
            timestamp = 1700000000000L
        )

        val csvLine = formatCsvRow(expense, dateFormat)
        val expectedDate = dateFormat.format(java.util.Date(1700000000000L))

        assertEquals("${expectedDate},EXPENSE,50000,2,\"Makan Siang\",3\n", csvLine)
    }
}
