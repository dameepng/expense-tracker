package com.example.expense_tracker.ui.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WalletCardFormatTest {

    @Test
    fun getGradient_returnsCorrectGradient_forKnownIds() {
        assertEquals(CardGradients.SunsetRose, CardGradients.getGradient("sunset_rose"))
        assertEquals(CardGradients.OceanBlue, CardGradients.getGradient("ocean_blue"))
        assertEquals(CardGradients.MidnightPurple, CardGradients.getGradient("midnight_purple"))
        assertEquals(CardGradients.EmeraldGreen, CardGradients.getGradient("emerald_green"))
        assertEquals(CardGradients.ObsidianDark, CardGradients.getGradient("obsidian_dark"))
    }

    @Test
    fun getGradient_isCaseInsensitive() {
        assertEquals(CardGradients.OceanBlue, CardGradients.getGradient("OCEAN_BLUE"))
        assertEquals(CardGradients.EmeraldGreen, CardGradients.getGradient("Emerald_Green"))
        assertEquals(CardGradients.ObsidianDark, CardGradients.getGradient("OBSIDIAN_DARK"))
    }

    @Test
    fun getGradient_fallsBackToSunsetRose_forUnknownOrEmptyId() {
        assertEquals(CardGradients.SunsetRose, CardGradients.getGradient(null))
        assertEquals(CardGradients.SunsetRose, CardGradients.getGradient(""))
        assertEquals(CardGradients.SunsetRose, CardGradients.getGradient("unknown_preset"))
    }

    @Test
    fun options_containsAllExpectedPresets() {
        assertEquals(5, CardGradients.options.size)
        assertNotNull(CardGradients.options.firstOrNull { it.id == "sunset_rose" })
        assertNotNull(CardGradients.options.firstOrNull { it.id == "obsidian_dark" })
    }

    @Test
    fun formatMaskedCardNumber_emptyOrNonDigit_returnsDefaultMask() {
        assertEquals("•••• •••• •••• 5052", formatMaskedCardNumber(""))
        assertEquals("•••• •••• •••• 5052", formatMaskedCardNumber("abc-xyz"))
    }

    @Test
    fun formatMaskedCardNumber_valid16Digits_masksAllExceptLastFour() {
        val result = formatMaskedCardNumber("1234567812345678")
        assertEquals("•••• •••• •••• 5678", result)
    }

    @Test
    fun formatMaskedCardNumber_withHyphensAndSpaces_extractsDigitsAndMasks() {
        val result = formatMaskedCardNumber("4111 2222 3333 4444")
        assertEquals("•••• •••• •••• 4444", result)
    }

    @Test
    fun formatMaskedCardNumber_shortDigits_padsCorrectly() {
        val result = formatMaskedCardNumber("123")
        // digits length < 4 -> digits = "123", padded with • up to 16 chars -> "123•••••••••••••" -> chunked into groups of 4
        assertEquals("123• •••• •••• ••••", result)
    }
}
