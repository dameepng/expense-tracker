package com.example.expense_tracker.ui.theme

import androidx.compose.animation.core.SpringSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MotionSchemeTest {

    @Test
    fun expressiveMotionScheme_providesExpectedSpringTokens() {
        val scheme = MotionScheme.expressive()
        assertNotNull(scheme)

        val defaultSpatial = scheme.defaultSpatialSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringDefaultSpatialDamping, defaultSpatial.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringDefaultSpatialStiffness, defaultSpatial.stiffness, 0.001f)

        val fastSpatial = scheme.fastSpatialSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringFastSpatialDamping, fastSpatial.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringFastSpatialStiffness, fastSpatial.stiffness, 0.001f)

        val slowSpatial = scheme.slowSpatialSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringSlowSpatialDamping, slowSpatial.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringSlowSpatialStiffness, slowSpatial.stiffness, 0.001f)

        val defaultEffects = scheme.defaultEffectsSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringDefaultEffectsDamping, defaultEffects.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringDefaultEffectsStiffness, defaultEffects.stiffness, 0.001f)

        val fastEffects = scheme.fastEffectsSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringFastEffectsDamping, fastEffects.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringFastEffectsStiffness, fastEffects.stiffness, 0.001f)

        val slowEffects = scheme.slowEffectsSpec<Float>() as SpringSpec<Float>
        assertEquals(ExpressiveMotionTokens.SpringSlowEffectsDamping, slowEffects.dampingRatio, 0.001f)
        assertEquals(ExpressiveMotionTokens.SpringSlowEffectsStiffness, slowEffects.stiffness, 0.001f)
    }

    @Test
    fun standardMotionScheme_providesExpectedSpringTokens() {
        val scheme = MotionScheme.standard()
        assertNotNull(scheme)

        val defaultSpatial = scheme.defaultSpatialSpec<Float>() as SpringSpec<Float>
        assertEquals(StandardMotionTokens.SpringDefaultSpatialDamping, defaultSpatial.dampingRatio, 0.001f)
        assertEquals(StandardMotionTokens.SpringDefaultSpatialStiffness, defaultSpatial.stiffness, 0.001f)

        val fastSpatial = scheme.fastSpatialSpec<Float>() as SpringSpec<Float>
        assertEquals(StandardMotionTokens.SpringFastSpatialDamping, fastSpatial.dampingRatio, 0.001f)
        assertEquals(StandardMotionTokens.SpringFastSpatialStiffness, fastSpatial.stiffness, 0.001f)
    }

    @Test
    fun expressiveScheme_isMoreBouncyThanStandard() {
        val expressive = MotionScheme.expressive()
        val standard = MotionScheme.standard()

        val expressiveFast = expressive.fastSpatialSpec<Float>() as SpringSpec<Float>
        val standardFast = standard.fastSpatialSpec<Float>() as SpringSpec<Float>

        // Expressive fast spatial has damping 0.6f (bouncier) compared to Standard 0.9f
        assertTrue(expressiveFast.dampingRatio < standardFast.dampingRatio)
    }
}
