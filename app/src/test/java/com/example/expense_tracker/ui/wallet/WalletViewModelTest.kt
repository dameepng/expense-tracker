package com.example.expense_tracker.ui.wallet

import com.example.expense_tracker.data.FakeWalletRepository
import com.example.expense_tracker.data.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
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
class WalletViewModelTest {

    private lateinit var viewModel: WalletViewModel
    private lateinit var repository: FakeWalletRepository
    private lateinit var userPrefs: FakeUserPreferencesRepository
    private val testDispatcher = StandardTestDispatcher()

    class FakeUserPreferencesRepository : com.example.expense_tracker.data.UserPreferencesRepository {
        override val selectedWalletIdFlow = kotlinx.coroutines.flow.flowOf<Long?>(null)
        override val themeModeFlow = kotlinx.coroutines.flow.flowOf("System Default")
        override val currencyFlow = kotlinx.coroutines.flow.flowOf("IDR")
        override val languageFlow = kotlinx.coroutines.flow.flowOf("Indonesia")
        override val isBiometricsEnabledFlow = kotlinx.coroutines.flow.flowOf(false)
        override val userNameFlow = kotlinx.coroutines.flow.flowOf("Adam")
        override val userPhotoUriFlow = kotlinx.coroutines.flow.flowOf<String?>(null)
        
        override suspend fun saveSelectedWalletId(walletId: Long?) {}
        override suspend fun saveThemeMode(mode: String) {}
        override suspend fun saveCurrency(currency: String) {}
        override suspend fun saveLanguage(language: String) {}
        override suspend fun saveBiometricsEnabled(enabled: Boolean) {}
        override suspend fun saveUserProfile(name: String, photoUri: String?) {}
        override suspend fun clearAllPreferences() {}
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeWalletRepository()
        userPrefs = FakeUserPreferencesRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsWallets() = runTest(testDispatcher) {
        repository.insertWallet(Wallet(name = "BCA", balance = 50000L))
        
        viewModel = WalletViewModel(repository, userPrefs, testDispatcher)
        
        // Wait for coroutine to finish
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(1, state.wallets.size)
        assertEquals("BCA", state.wallets[0].name)
    }

    @Test
    fun addWallet_insertsAndRefreshes() = runTest(testDispatcher) {
        viewModel = WalletViewModel(repository, userPrefs, testDispatcher)
        testScheduler.advanceUntilIdle()
        
        viewModel.addWallet("Mandiri")
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(1, state.wallets.size)
        assertEquals("Mandiri", state.wallets[0].name)
    }

    @Test
    fun deleteWallet_removesAndRefreshes() = runTest(testDispatcher) {
        val wallet = Wallet(id = 1L, name = "Cash", balance = 100L)
        repository.insertWallet(wallet)
        
        viewModel = WalletViewModel(repository, userPrefs, testDispatcher)
        testScheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.first().wallets.size)
        
        viewModel.deleteWallet(wallet)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertTrue(state.wallets.isEmpty())
    }
}
