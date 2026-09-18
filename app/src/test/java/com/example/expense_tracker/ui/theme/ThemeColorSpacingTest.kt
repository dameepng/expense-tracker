package com.example.expense_tracker.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeColorSpacingTest {

    @Test
    fun categoryColor_income_returnsExpectedColorsAndWrapsCorrectly() {
        // Index 1 maps to first income color
        val firstIncome = categoryColor(1, isIncome = true)
        assertEquals(IncomeCategoryColors[0], firstIncome)

        // Index 0 coerced to 0 -> first income color
        val zeroIncome = categoryColor(0, isIncome = true)
        assertEquals(IncomeCategoryColors[0], zeroIncome)

        // Negative index coerced to 0 -> first income color
        val negativeIncome = categoryColor(-5, isIncome = true)
        assertEquals(IncomeCategoryColors[0], negativeIncome)

        // Index equal to size + 1 wraps around to 0
        val wrappedIncome = categoryColor(IncomeCategoryColors.size + 1, isIncome = true)
        assertEquals(IncomeCategoryColors[0], wrappedIncome)

        // Second color
        val secondIncome = categoryColor(2, isIncome = true)
        assertEquals(IncomeCategoryColors[1], secondIncome)
    }

    @Test
    fun categoryColor_expense_returnsExpectedColorsAndWrapsCorrectly() {
        // Index 1 maps to first expense color
        val firstExpense = categoryColor(1, isIncome = false)
        assertEquals(CategoryColors[0], firstExpense)

        // Index 0 coerced to 0 -> first expense color
        val zeroExpense = categoryColor(0, isIncome = false)
        assertEquals(CategoryColors[0], zeroExpense)

        // Negative index coerced to 0 -> first expense color
        val negativeExpense = categoryColor(-10, isIncome = false)
        assertEquals(CategoryColors[0], negativeExpense)

        // Index equal to size + 1 wraps around to 0
        val wrappedExpense = categoryColor(CategoryColors.size + 1, isIncome = false)
        assertEquals(CategoryColors[0], wrappedExpense)

        // Expense and Income colors should be distinct palettes
        assertNotEquals(firstExpense, categoryColor(1, isIncome = true))
    }

    @Test
    fun spacing_measurementTokens_areMonotonicallyIncreasing() {
        val spacing = Spacing()
        assertTrue(spacing.space0 < spacing.space25)
        assertTrue(spacing.space25 < spacing.space50)
        assertTrue(spacing.space50 < spacing.space75)
        assertTrue(spacing.space75 < spacing.space100)
        assertTrue(spacing.space100 < spacing.space150)
        assertTrue(spacing.space150 < spacing.space200)
        assertTrue(spacing.space200 < spacing.space300)
        assertTrue(spacing.space300 < spacing.space400)
        assertTrue(spacing.space400 < spacing.space600)
        assertTrue(spacing.space600 < spacing.space1000)
    }

    @Test
    fun spacing_semanticAliases_mapToExpectedTokens() {
        val spacing = Spacing()
        assertEquals(spacing.space0, spacing.none)
        assertEquals(spacing.space50, spacing.extraSmall)
        assertEquals(spacing.space100, spacing.small)
        assertEquals(spacing.space200, spacing.medium)
        assertEquals(spacing.space300, spacing.large)
        assertEquals(spacing.space400, spacing.extraLarge)
        assertEquals(spacing.space600, spacing.extraExtraLarge)
    }
}
