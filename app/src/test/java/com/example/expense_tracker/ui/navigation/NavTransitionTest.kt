package com.example.expense_tracker.ui.navigation

import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.unit.IntOffset
import com.example.expense_tracker.isBottomNavPeer
import com.example.expense_tracker.ui.theme.ExpressiveMotionTokens
import com.example.expense_tracker.ui.theme.MotionScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTransitionTest {

    @Test
    fun `isBottomNavPeer returns true between all bottom navigation tabs`() {
        val tabs = listOf(
            NavRoutes.HOME,
            NavRoutes.WALLET,
            NavRoutes.SUMMARY,
            NavRoutes.PROFILE
        )

        for (from in tabs) {
            for (to in tabs) {
                assertTrue(
                    "Expected isBottomNavPeer to be true from $from to $to",
                    isBottomNavPeer(from, to)
                )
            }
        }
    }

    @Test
    fun `isBottomNavPeer handles routes with query arguments correctly`() {
        assertTrue(
            isBottomNavPeer("summary?walletId=1", NavRoutes.HOME)
        )
        assertTrue(
            isBottomNavPeer(NavRoutes.PROFILE, "wallet?filter=all")
        )
    }

    @Test
    fun `isBottomNavPeer returns false when navigating to or from hierarchical screens`() {
        // Navigating forward from Home to detail/form screens
        assertFalse(isBottomNavPeer(NavRoutes.HOME, NavRoutes.INPUT))
        assertFalse(isBottomNavPeer(NavRoutes.HOME, NavRoutes.CHAT))
        assertFalse(isBottomNavPeer(NavRoutes.HOME, NavRoutes.AI_INPUT))
        assertFalse(isBottomNavPeer(NavRoutes.HOME, NavRoutes.RECEIPT_PICKER))
        assertFalse(isBottomNavPeer(NavRoutes.HOME, NavRoutes.REMINDER_LIST))
        assertFalse(isBottomNavPeer(NavRoutes.SUMMARY, NavRoutes.CATEGORY_DETAIL))
        assertFalse(isBottomNavPeer(NavRoutes.PROFILE, NavRoutes.HELP_FAQ))
        assertFalse(isBottomNavPeer(NavRoutes.PROFILE, NavRoutes.PRIVACY_POLICY))

        // Navigating backward (pop) from detail/form screens to Home
        assertFalse(isBottomNavPeer(NavRoutes.INPUT, NavRoutes.HOME))
        assertFalse(isBottomNavPeer(NavRoutes.CHAT, NavRoutes.HOME))
        assertFalse(isBottomNavPeer(NavRoutes.CATEGORY_DETAIL, NavRoutes.SUMMARY))

        // Null and invalid routes
        assertFalse(isBottomNavPeer(null, NavRoutes.HOME))
        assertFalse(isBottomNavPeer(NavRoutes.HOME, null))
        assertFalse(isBottomNavPeer(null, null))
        assertFalse(isBottomNavPeer("unknown_screen", NavRoutes.HOME))
    }

    @Test
    fun `expressive motion scheme provides valid M3 spring tokens for navigation`() {
        val motionScheme = MotionScheme.expressive()
        assertNotNull(motionScheme)

        val spatialSpec = motionScheme.defaultSpatialSpec<IntOffset>() as SpringSpec<IntOffset>
        assertEquals(ExpressiveMotionTokens.SpringDefaultSpatialDamping, spatialSpec.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringDefaultSpatialStiffness, spatialSpec.stiffness, 0.001f)

        val effectsSpec = motionScheme.defaultEffectsSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringDefaultEffectsDamping, effectsSpec.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringDefaultEffectsStiffness, effectsSpec.stiffness, 0.001f)

        val fastEffectsSpec = motionScheme.fastEffectsSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringFastEffectsDamping, fastEffectsSpec.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringFastEffectsStiffness, fastEffectsSpec.stiffness, 0.001f)
    }

    @Test
    fun `materialMotionTokens matches official M3 suggested easing and duration pairs`() {
        // Suggested Easing & Duration pairs from M3 guide:
        // - Enter the screen: Emphasized decelerate (400ms - 500ms)
        // - Exit the screen: Emphasized accelerate (200ms)
        // - Begin and end on screen: Emphasized (500ms)
        assertEquals(200, com.example.expense_tracker.ui.theme.MaterialMotionTokens.DurationShort4)
        assertEquals(250, com.example.expense_tracker.ui.theme.MaterialMotionTokens.DurationMedium1)
        assertEquals(300, com.example.expense_tracker.ui.theme.MaterialMotionTokens.DurationMedium2)
        assertEquals(400, com.example.expense_tracker.ui.theme.MaterialMotionTokens.DurationMedium4)
        assertEquals(500, com.example.expense_tracker.ui.theme.MaterialMotionTokens.DurationLong2)

        assertNotNull(com.example.expense_tracker.ui.theme.MaterialMotionTokens.Emphasized)
        assertNotNull(com.example.expense_tracker.ui.theme.MaterialMotionTokens.EmphasizedDecelerate)
        assertNotNull(com.example.expense_tracker.ui.theme.MaterialMotionTokens.EmphasizedAccelerate)
    }

    @Test
    fun `isAiRoute returns true for all AI features on both enter and exit`() {
        val aiRoutes = listOf(
            NavRoutes.AI_INPUT,
            NavRoutes.RECEIPT_PICKER,
            NavRoutes.RECEIPT_REVIEW,
            NavRoutes.CHAT
        )

        for (aiRoute in aiRoutes) {
            // Navigating from Home to AI screen (enter)
            assertTrue("Expected isAiRoute to be true from HOME to $aiRoute", com.example.expense_tracker.isAiRoute(NavRoutes.HOME, aiRoute))
            // Navigating from AI screen to Home (exit / pop)
            assertTrue("Expected isAiRoute to be true from $aiRoute to HOME", com.example.expense_tracker.isAiRoute(aiRoute, NavRoutes.HOME))
            // Between AI screens (e.g. RECEIPT_PICKER -> RECEIPT_REVIEW -> AI_INPUT)
            assertTrue("Expected isAiRoute to be true between AI screens", com.example.expense_tracker.isAiRoute(NavRoutes.RECEIPT_PICKER, NavRoutes.RECEIPT_REVIEW))
            assertTrue("Expected isAiRoute to be true between AI screens", com.example.expense_tracker.isAiRoute(NavRoutes.RECEIPT_REVIEW, NavRoutes.AI_INPUT))
        }

        // Non-AI routes should return false
        assertFalse(com.example.expense_tracker.isAiRoute(NavRoutes.HOME, NavRoutes.INPUT))
        assertFalse(com.example.expense_tracker.isAiRoute(NavRoutes.HOME, NavRoutes.SUMMARY))
        assertFalse(com.example.expense_tracker.isAiRoute(NavRoutes.SUMMARY, NavRoutes.WALLET))
        assertFalse(com.example.expense_tracker.isAiRoute(null, NavRoutes.HOME))
        assertFalse(com.example.expense_tracker.isAiRoute(null, null))
    }
}

