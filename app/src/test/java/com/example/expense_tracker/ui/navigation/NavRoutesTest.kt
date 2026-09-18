package com.example.expense_tracker.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavRoutesTest {
    @Test
    fun `bottom bar is hidden on full-screen AI destinations`() {
        assertFalse(NavRoutes.shouldShowBottomBar(NavRoutes.AI_INPUT))
        assertFalse(NavRoutes.shouldShowBottomBar(NavRoutes.CHAT))
    }

    @Test
    fun `bottom bar remains visible on primary destinations`() {
        assertTrue(NavRoutes.shouldShowBottomBar(NavRoutes.HOME))
        assertTrue(NavRoutes.shouldShowBottomBar(NavRoutes.WALLET))
        assertTrue(NavRoutes.shouldShowBottomBar(null))
    }

    @Test
    fun `inputRoute generates correct route paths`() {
        org.junit.Assert.assertEquals("input", NavRoutes.inputRoute())
        org.junit.Assert.assertEquals("input", NavRoutes.inputRoute(null))
        org.junit.Assert.assertEquals("input?expenseId=42", NavRoutes.inputRoute(42L))
    }
}
