package com.example.expense_tracker.ui.input.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputFormatTest {

    @Test
    fun formatWithDots_shortLengths_returnsOriginal() {
        assertEquals("", formatWithDots(""))
        assertEquals("0", formatWithDots("0"))
        assertEquals("50", formatWithDots("50"))
        assertEquals("500", formatWithDots("500"))
    }

    @Test
    fun formatWithDots_thousands_formatsCorrectly() {
        assertEquals("1.000", formatWithDots("1000"))
        assertEquals("15.000", formatWithDots("15000"))
        assertEquals("150.000", formatWithDots("150000"))
    }

    @Test
    fun formatWithDots_millionsAndBillions_formatsCorrectly() {
        assertEquals("1.000.000", formatWithDots("1000000"))
        assertEquals("25.500.000", formatWithDots("25500000"))
        assertEquals("1.000.000.000", formatWithDots("1000000000"))
    }
}
