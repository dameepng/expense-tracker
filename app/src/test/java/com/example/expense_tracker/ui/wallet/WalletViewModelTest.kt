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
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeWalletRepository()
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_loadsWallets() = runTest(testDispatcher) {
        repository.insertWallet(Wallet(name = "BCA", balance = 50000L))
        
        viewModel = WalletViewModel(repository, testDispatcher)
        
        // Wait for coroutine to finish
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(1, state.wallets.size)
        assertEquals("BCA", state.wallets[0].name)
    }

    @Test
    fun addWallet_insertsAndRefreshes() = runTest(testDispatcher) {
        viewModel = WalletViewModel(repository, testDispatcher)
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
        
        viewModel = WalletViewModel(repository, testDispatcher)
        testScheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.first().wallets.size)
        
        viewModel.deleteWallet(wallet)
        testScheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertTrue(state.wallets.isEmpty())
    }
}
