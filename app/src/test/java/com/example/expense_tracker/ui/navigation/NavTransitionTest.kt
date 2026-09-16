package com.example.expense_tracker.ui.navigation

import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.unit.IntOffset
import com.example.expense_tracker.ui.theme.ExpressiveMotionTokens
import com.example.expense_tracker.ui.theme.MotionScheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavTransitionTest {

    @Test
    fun `isTopLevelSwitch returns true between all bottom navigation tabs`() {
        val tabs = listOf(
            NavRoutes.HOME,
            NavRoutes.WALLET,
            NavRoutes.SUMMARY,
            NavRoutes.PROFILE
        )

        for (from in tabs) {
            for (to in tabs) {
                assertTrue(
                    "Expected isTopLevelSwitch to be true from $from to $to",
                    NavMotion.isTopLevelSwitch(from, to)
                )
            }
        }
    }

    @Test
    fun `isTopLevelSwitch handles routes with query arguments correctly`() {
        assertTrue(
            NavMotion.isTopLevelSwitch("summary?walletId=1", NavRoutes.HOME)
        )
        assertTrue(
            NavMotion.isTopLevelSwitch(NavRoutes.PROFILE, "wallet?filter=all")
        )
    }

    @Test
    fun `isTopLevelSwitch returns false when navigating to or from hierarchical screens`() {
        // Navigating forward from Home to detail/form screens
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, NavRoutes.INPUT))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, NavRoutes.CHAT))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, NavRoutes.AI_INPUT))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, NavRoutes.RECEIPT_PICKER))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, NavRoutes.REMINDER_LIST))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.SUMMARY, NavRoutes.CATEGORY_DETAIL))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.PROFILE, NavRoutes.HELP_FAQ))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.PROFILE, NavRoutes.PRIVACY_POLICY))

        // Navigating backward (pop) from detail/form screens to Home
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.INPUT, NavRoutes.HOME))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.CHAT, NavRoutes.HOME))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.CATEGORY_DETAIL, NavRoutes.SUMMARY))

        // Null and invalid routes
        assertFalse(NavMotion.isTopLevelSwitch(null, NavRoutes.HOME))
        assertFalse(NavMotion.isTopLevelSwitch(NavRoutes.HOME, null))
        assertFalse(NavMotion.isTopLevelSwitch(null, null))
        assertFalse(NavMotion.isTopLevelSwitch("unknown_screen", NavRoutes.HOME))
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
}

