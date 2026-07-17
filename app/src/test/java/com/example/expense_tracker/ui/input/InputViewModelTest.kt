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

@OptIn(ExperimentalCoroutinesApi::class)
class InputViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeInputRepository : InputRepository {
        var savedExpenses = mutableListOf<SavedExpense>()
        var presetCategories = listOf(
            Category(1, "Makanan"),
            Category(2, "Transport"),
            Category(3, "Belanja"),
            Category(4, "Hiburan"),
            Category(5, "Tagihan"),
            Category(6, "Kesehatan"),
            Category(7, "Lainnya"),
        )

        override fun getCategories(): List<Category> = presetCategories
        override fun getExpenseById(id: Long): com.example.expense_tracker.data.Expense? = null
        override fun insertExpense(amount: Long, categoryId: Long, description: String, timestamp: Long, type: String, id: Long) {
            savedExpenses.add(SavedExpense(amount, categoryId, description, timestamp, type))
        }
    }

    private data class SavedExpense(val amount: Long, val categoryId: Long, val description: String, val timestamp: Long, val type: String)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel(repo: FakeInputRepository): InputViewModel {
        val vm = InputViewModel(repo, testDispatcher)
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
    fun `initial state loads 7 preset categories`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)
        val state = vm.uiState.value

        assertEquals(7, state.categories.size)
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
    fun `amount and category both set enables save button`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onAmountChange("25000")
        vm.onCategorySelected(1L)

        assertTrue(vm.uiState.value.isSaveEnabled)
    }

    @Test
    fun `clearing amount disables save even with category selected`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)

        vm.onAmountChange("10000")
        vm.onCategorySelected(1L)
        assertTrue(vm.uiState.value.isSaveEnabled)

        vm.onAmountChange("")
        assertFalse(vm.uiState.value.isSaveEnabled)
    }

    @Test
    fun `amount zero or non-numeric treated as invalid`() {
        val repo = FakeInputRepository()
        val vm = initViewModel(repo)
        vm.onCategorySelected(1L)

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
        val vm = initViewModel(repo)

        vm.onAmountChange("50000")
        vm.onCategorySelected(1L)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.saved)
        assertEquals(1, repo.savedExpenses.size)
        assertEquals(50_000L, repo.savedExpenses[0].amount)
        assertEquals(1L, repo.savedExpenses[0].categoryId)
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
        val vm = initViewModel(repo)

        vm.onAmountChange("75000")
        vm.onCategorySelected(2L)
        vm.onSave()
        testDispatcher.scheduler.advanceUntilIdle()

        // After save, reset for next expense
        assertEquals("", vm.uiState.value.amountText)
        assertNull(vm.uiState.value.selectedCategoryId)
        assertFalse(vm.uiState.value.isSaveEnabled)
        assertTrue(vm.uiState.value.saved)
    }
}
