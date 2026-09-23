package com.example.expense_tracker.ui.summary

import com.example.expense_tracker.data.FilterPeriod
import com.example.expense_tracker.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryFormatTest {

    @Test
    fun formatAxisValue_formatsZeroAndSmallNumbersDirectly() {
        assertEquals("0", formatAxisValue(0L))
        assertEquals("500", formatAxisValue(500L))
        assertEquals("999", formatAxisValue(999L))
    }

    @Test
    fun formatAxisValue_formatsThousandsWithRb() {
        assertEquals("1rb", formatAxisValue(1_000L))
        assertEquals("50rb", formatAxisValue(50_000L))
        assertEquals("999rb", formatAxisValue(999_000L))
    }

    @Test
    fun formatAxisValue_formatsMillionsWithJt() {
        assertEquals("1jt", formatAxisValue(1_000_000L))
        assertEquals("2jt", formatAxisValue(2_500_000L))
        assertEquals("15jt", formatAxisValue(15_000_000L))
    }

    @Test
    fun formatAxisValue_formatsNegativeValuesCorrectly() {
        assertEquals("-50rb", formatAxisValue(-50_000L))
        assertEquals("-2jt", formatAxisValue(-2_000_000L))
    }

    @Test
    fun defaultSummaryUiState_hasExpectedDefaults() {
        val state = SummaryUiState()
        assertEquals(FilterPeriod.TODAY, state.filter)
        assertEquals(TransactionType.EXPENSE, state.transactionType)
        assertTrue(state.items.isEmpty())
        assertEquals(0L, state.totalAmount)
        assertEquals(0L, state.totalBalance)
        assertEquals(0f, state.balancePercentageChange, 0.001f)
        assertFalse(state.isLoading)
        assertTrue(state.wallets.isEmpty())
    }

    @Test
    fun dailyCashFlow_holdsDataCorrectly() {
        val flow = DailyCashFlow(dateMillis = 1000L, income = 50_000L, expense = 20_000L)
        assertEquals(1000L, flow.dateMillis)
        assertEquals(50_000L, flow.income)
        assertEquals(20_000L, flow.expense)
    }

    @Test
    fun calculateNiceYTicks_returnsExpectedFourTicksFor200M() {
        val ticks = calculateNiceYTicks(0L, 200_148_000L)
        assertEquals(listOf(300_000_000L, 200_000_000L, 100_000_000L, 0L), ticks)
        assertEquals(listOf("300jt", "200jt", "100jt", "0"), ticks.map { formatAxisValue(it) })
    }
}

