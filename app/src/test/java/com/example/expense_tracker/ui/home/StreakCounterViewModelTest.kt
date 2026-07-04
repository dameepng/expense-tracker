package com.example.expense_tracker.ui.home

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

/**
 * TDD tests for StreakCounterViewModel.
 *
 * US5 AC:
 * - Streak bertambah kalau expense setiap hari
 * - Streak reset ke 0 kalau gap 1 hari
 * - Streak 0: tampilkan state berbeda
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StreakCounterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun initViewModel(streak: Int): StreakCounterViewModel {
        val vm = StreakCounterViewModel(FakeStreakRepository(streak))
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `default state has streak 0`() {
        val viewModel = initViewModel(streak = 0)
        val state = viewModel.uiState.value
        assertEquals(0, state.streak)
        assertFalse(state.hasStreak)
    }

    @Test
    fun `streak 3 shows streak active with correct count`() {
        val viewModel = initViewModel(streak = 3)
        val state = viewModel.uiState.value
        assertEquals(3, state.streak)
        assertTrue(state.hasStreak)
    }

    @Test
    fun `streak 1 shows streak active`() {
        val viewModel = initViewModel(streak = 1)
        val state = viewModel.uiState.value
        assertEquals(1, state.streak)
        assertTrue(state.hasStreak)
    }

    @Test
    fun `streak 0 has motivational empty text`() {
        val viewModel = initViewModel(streak = 0)
        val state = viewModel.uiState.value
        assertEquals(0, state.streak)
        assertEquals("Mulai streak kamu hari ini!", state.encouragementText)
    }

    @Test
    fun `streak greater than 0 shows count text`() {
        val viewModel = initViewModel(streak = 5)
        val state = viewModel.uiState.value
        assertEquals(5, state.streak)
        assertEquals("5 hari berturut-turut!", state.encouragementText)
    }

    @Test
    fun `streak 1 shows singular text`() {
        val viewModel = initViewModel(streak = 1)
        val state = viewModel.uiState.value
        assertEquals("1 hari berturut-turut!", state.encouragementText)
    }
}

private class FakeStreakRepository(private val streak: Int) : StreakRepository {
    override fun calculateStreak(): Int = streak
}
