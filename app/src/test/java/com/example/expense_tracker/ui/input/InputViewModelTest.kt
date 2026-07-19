package com.example.expense_tracker.ui.input

import com.example.expense_tracker.data.Category
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
import com.example.expense_tracker.data.FakeWalletRepository
import com.example.expense_tracker.data.Wallet

@OptIn(ExperimentalCoroutinesApi::class)
class InputViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeInputRepository : InputRepository {
        var savedExpenses = mutableListOf<SavedExpense>()
        var presetCategories = listOf(
            Category(1, "Makanan", "EXPENSE"),
            Category(2, "Transport", "EXPENSE"),
            Category(3, "Belanja", "EXPENSE"),
            Category(4, "Hiburan", "EXPENSE"),
            Category(5, "Tagihan", "EXPENSE"),
            Category(6, "Kesehatan", "EXPENSE"),
            Category(7, "Lainnya", "BOTH"),
            Category(8, "Gaji", "INCOME"),
            Category(9, "Freelance", "INCOME"),
            Category(10, "Bonus", "INCOME"),
            Category(11, "Transfer Masuk", "INCOME")
        )

        override fun getCategories(): List<Category> = presetCategories
        override fun getCategoriesByType(type: String): List<Category> =
            presetCategories.filter { it.type == type || it.type == "BOTH" }
        override fun getExpenseById(id: Long): com.example.expense_tracker.data.Expense? = null
        override fun insertExpense(amount: Long, categoryId: Long, description: String, timestamp: Long, type: String, walletId: Long, id: Long) {
            savedExpenses.add(SavedExpense(amount, categoryId, description, timestamp, type, walletId))
        }
    }

    private data class SavedExpense(val amount: Long, val categoryId: Long, val description: String, val timestamp: Long, val type: String, val walletId: Long)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel(
        repo: FakeInputRepository,
        walletRepo: FakeWalletRepository = FakeWalletRepository(),
        billReminderRepo: com.example.expense_tracker.data.FakeBillReminderRepository = com.example.expense_tracker.data.FakeBillReminderRepository()
    ): InputViewModel {
        val vm = InputViewModel(repo, walletRepo, billReminderRepo, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `initial state has empty amount and no category selected`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)
        val state = vm.uiState.value

        assertEquals("", state.amountText)
        assertNull(state.selectedCategoryId)
        assertFalse(state.isSaveEnabled)
        assertFalse(state.saved)
    }

    @Test
    fun `initial state loads expense categories by default`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)
        val state = vm.uiState.value

        // 6 EXPENSE + 1 BOTH (Lainnya) = 7
        assertEquals(7, state.categories.size)
        assertTrue(state.categories.all { it.type == "EXPENSE" || it.type == "BOTH" })
    }

    @Test
    fun `entering amount enables save only when category also selected`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onAmountChange("50000")
        assertFalse(vm.uiState.value.isSaveEnabled)
        assertEquals("50000", vm.uiState.value.amountText)
    }

    @Test
    fun `amount, category, and wallet set enables save button`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)

        vm.onAmountChange("25000")
        vm.onCategorySelected(1L)
        vm.onWalletSelected(1L)

        assertTrue(vm.uiState.value.isSaveEnabled)
    }

    @Test
    fun `clearing amount disables save even with category selected`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)

        vm.onAmountChange("10000")
        vm.onCategorySelected(1L)
        vm.onWalletSelected(1L)
        assertTrue(vm.uiState.value.isSaveEnabled)

        vm.onAmountChange("")
        assertFalse(vm.uiState.value.isSaveEnabled)
    }

    @Test
    fun `amount zero or non-numeric treated as invalid`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)
        vm.onCategorySelected(1L)
        vm.onWalletSelected(1L)

        vm.onAmountChange("0")
        assertFalse(vm.uiState.value.isSaveEnabled)

        vm.onAmountChange("abc")
        assertFalse(vm.uiState.value.isSaveEnabled)
    }

    @Test
    fun `selecting category sets selectedCategoryId`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onCategorySelected(2L)

        assertEquals(2L, vm.uiState.value.selectedCategoryId)
    }

    @Test
    fun `selecting different category replaces previous selection`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onCategorySelected(1L)
        assertEquals(1L, vm.uiState.value.selectedCategoryId)

        vm.onCategorySelected(3L)
        assertEquals(3L, vm.uiState.value.selectedCategoryId)
    }

    @Test
    fun `save inserts expense and sets saved flag`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)

        vm.onAmountChange("50000")
        vm.onCategorySelected(1L)
        vm.onWalletSelected(1L)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
        assertEquals(1, repo.savedExpenses.size)
        assertEquals(50_000L, repo.savedExpenses[0].amount)
        assertEquals(1L, repo.savedExpenses[0].categoryId)
        assertEquals(1L, repo.savedExpenses[0].walletId)
    }

    @Test
    fun `save does nothing when form invalid`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.saved)
        assertEquals(0, repo.savedExpenses.size)
    }

    @Test
    fun `save resets form state for next input`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)

        vm.onAmountChange("75000")
        vm.onCategorySelected(2L)
        vm.onWalletSelected(1L)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        // After save, reset for next expense
        assertEquals("", vm.uiState.value.amountText)
        assertNull(vm.uiState.value.selectedCategoryId)
        // selectedWalletId might also be null or auto-selected, but saved is true
        assertTrue(vm.uiState.value.saved)
    }

    @Test
    fun `switching to income type loads income categories`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onTransactionTypeChange(com.example.expense_tracker.data.TransactionType.INCOME)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        // 4 INCOME + 1 BOTH (Lainnya) = 5
        assertEquals(5, state.categories.size)
        assertTrue(state.categories.all { it.type == "INCOME" || it.type == "BOTH" })
        assertNull(state.selectedCategoryId) // selection reset
    }

    @Test
    fun `switching type resets category selection and disables save`() {
        val repo = FakeInputRepository()
        val walletRepo = FakeWalletRepository().apply {
            insertWallet(Wallet(1L, "Cash", 0L))
        }
        val vm = initViewModel(repo, walletRepo)

        vm.onAmountChange("50000")
        vm.onCategorySelected(1L)
        vm.onWalletSelected(1L)
        assertTrue(vm.uiState.value.isSaveEnabled)

        vm.onTransactionTypeChange(com.example.expense_tracker.data.TransactionType.INCOME)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.selectedCategoryId)
        assertFalse(vm.uiState.value.isSaveEnabled)
    }
}
