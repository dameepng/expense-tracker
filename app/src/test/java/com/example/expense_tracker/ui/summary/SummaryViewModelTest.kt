package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.CategoryBreakdown
import com.example.expense_tracker.data.FilterPeriod
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
class SummaryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakeSummaryRepository : SummaryRepository {
        var breakdown = listOf<CategoryBreakdown>()
        override fun getBreakdownByCategory(startTime: Long, endTime: Long): List<CategoryBreakdown> =
            breakdown.toList()
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel(repo: FakeSummaryRepository): SummaryViewModel {
        val vm = SummaryViewModel(repo, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `initial filter is TODAY`() {
        val repo = FakeSummaryRepository()
        val vm = initViewModel(repo)
        assertEquals(FilterPeriod.TODAY, vm.uiState.value.filter)
    }

    @Test
    fun `default state has empty list and zero total`() {
        val repo = FakeSummaryRepository()
        val vm = initViewModel(repo)
        val state = vm.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(0L, state.totalAmount)
    }

    @Test
    fun `breakdown items include category name amount and percentage`() {
        val repo = FakeSummaryRepository()
        repo.breakdown = listOf(
            CategoryBreakdown(1, "Makanan", 60_000L),
            CategoryBreakdown(2, "Transport", 40_000L),
        )
        val vm = initViewModel(repo)

        val state = vm.uiState.value
        assertEquals(2, state.items.size)
        assertEquals(100_000L, state.totalAmount)

        assertEquals("Makanan", state.items[0].categoryName)
        assertEquals(60_000L, state.items[0].amount)
        assertEquals(0.60f, state.items[0].percentage)

        assertEquals("Transport", state.items[1].categoryName)
        assertEquals(40_000L, state.items[1].amount)
        assertEquals(0.40f, state.items[1].percentage)
    }

    @Test
    fun `single category shows 100 percent`() {
        val repo = FakeSummaryRepository()
        repo.breakdown = listOf(CategoryBreakdown(1, "Belanja", 25_000L))
        val vm = initViewModel(repo)

        val state = vm.uiState.value
        assertEquals(1, state.items.size)
        assertEquals(1.0f, state.items[0].percentage)
    }

    @Test
    fun `onFilterSelected updates filter label`() {
        val repo = FakeSummaryRepository()
        val vm = initViewModel(repo)

        vm.onFilterSelected(FilterPeriod.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(FilterPeriod.WEEK, vm.uiState.value.filter)
    }

    @Test
    fun `empty state when no breakdown data`() {
        val repo = FakeSummaryRepository()
        repo.breakdown = emptyList()
        val vm = initViewModel(repo)

        val state = vm.uiState.value
        assertTrue(state.items.isEmpty())
        assertEquals(0L, state.totalAmount)
    }
}
