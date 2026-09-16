package com.example.expense_tracker.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Material 3 Expressive Motion Physics System
 *
 * Implements physics-based spring motions as defined in:
 * - https://m3.material.io/styles/motion/overview/how-it-works#fef83d57-b139-4c40-b538-9f1e9872df1b
 * - https://developer.android.com/reference/kotlin/androidx/compose/material3/MotionScheme
 *
 * Provides spring configurations for spatial transitions (movement, size, expansion)
 * and effects transitions (fade, alpha, color) across the application.
 */
@Immutable
interface MotionScheme {
    /**
     * Default spatial motion spec for layout, bounds, and position changes.
     */
    fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * Fast spatial motion spec for responsive interactions (e.g. icon flips, toggles).
     */
    fun <T> fastSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * Slow spatial motion spec for prominent hero transitions and full-screen elements.
     */
    fun <T> slowSpatialSpec(): FiniteAnimationSpec<T>

    /**
     * Default effects motion spec for non-spatial animations (e.g. opacity, elevation).
     */
    fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T>

    /**
     * Fast effects motion spec for quick color and alpha transitions.
     */
    fun <T> fastEffectsSpec(): FiniteAnimationSpec<T>

    /**
     * Slow effects motion spec for gradual background and scrim fades.
     */
    fun <T> slowEffectsSpec(): FiniteAnimationSpec<T>

    companion object {
        /**
         * Expressive motion scheme: Uses playful, lively spring physics with natural overshoots.
         */
        fun expressive(): MotionScheme = ExpressiveMotionSchemeImpl

        /**
         * Standard motion scheme: Uses utilitarian, minimal-bounce spring physics.
         */
        fun standard(): MotionScheme = StandardMotionSchemeImpl
    }
}

/**
 * Official Material 3 Expressive Motion Spring Tokens.
 */
object ExpressiveMotionTokens {
    const val SpringDefaultSpatialDamping = 0.8f
    const val SpringDefaultSpatialStiffness = 380.0f

    const val SpringFastSpatialDamping = 0.6f
    const val SpringFastSpatialStiffness = 800.0f

    const val SpringSlowSpatialDamping = 0.8f
    const val SpringSlowSpatialStiffness = 200.0f

    const val SpringDefaultEffectsDamping = 1.0f
    const val SpringDefaultEffectsStiffness = 1600.0f

    const val SpringFastEffectsDamping = 1.0f
    const val SpringFastEffectsStiffness = 3800.0f

    const val SpringSlowEffectsDamping = 1.0f
    const val SpringSlowEffectsStiffness = 800.0f
}

/**
 * Standard Motion Spring Tokens for utilitarian interfaces.
 */
object StandardMotionTokens {
    const val SpringDefaultSpatialDamping = 0.9f
    const val SpringDefaultSpatialStiffness = 700.0f

    const val SpringFastSpatialDamping = 0.9f
    const val SpringFastSpatialStiffness = 1400.0f

    const val SpringSlowSpatialDamping = 0.9f
    const val SpringSlowSpatialStiffness = 300.0f

    const val SpringDefaultEffectsDamping = 1.0f
    const val SpringDefaultEffectsStiffness = 1600.0f

    const val SpringFastEffectsDamping = 1.0f
    const val SpringFastEffectsStiffness = 3800.0f

    const val SpringSlowEffectsDamping = 1.0f
    const val SpringSlowEffectsStiffness = 800.0f
}

@Immutable
private object ExpressiveMotionSchemeImpl : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringDefaultSpatialDamping,
        stiffness = ExpressiveMotionTokens.SpringDefaultSpatialStiffness
    )

    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringFastSpatialDamping,
        stiffness = ExpressiveMotionTokens.SpringFastSpatialStiffness
    )

    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringSlowSpatialDamping,
        stiffness = ExpressiveMotionTokens.SpringSlowSpatialStiffness
    )

    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringDefaultEffectsDamping,
        stiffness = ExpressiveMotionTokens.SpringDefaultEffectsStiffness
    )

    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringFastEffectsDamping,
        stiffness = ExpressiveMotionTokens.SpringFastEffectsStiffness
    )

    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = ExpressiveMotionTokens.SpringSlowEffectsDamping,
        stiffness = ExpressiveMotionTokens.SpringSlowEffectsStiffness
    )
}

@Immutable
private object StandardMotionSchemeImpl : MotionScheme {
    override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringDefaultSpatialDamping,
        stiffness = StandardMotionTokens.SpringDefaultSpatialStiffness
    )

    override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringFastSpatialDamping,
        stiffness = StandardMotionTokens.SpringFastSpatialStiffness
    )

    override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringSlowSpatialDamping,
        stiffness = StandardMotionTokens.SpringSlowSpatialStiffness
    )

    override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringDefaultEffectsDamping,
        stiffness = StandardMotionTokens.SpringDefaultEffectsStiffness
    )

    override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringFastEffectsDamping,
        stiffness = StandardMotionTokens.SpringFastEffectsStiffness
    )

    override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = StandardMotionTokens.SpringSlowEffectsDamping,
        stiffness = StandardMotionTokens.SpringSlowEffectsStiffness
    )
}

/**
 * CompositionLocal providing the current [MotionScheme].
 */
val LocalMotionScheme = staticCompositionLocalOf<MotionScheme> {
    MotionScheme.expressive()
}

/**
 * Extension property to access the current [MotionScheme] via [MaterialTheme].
 */
val MaterialTheme.motionScheme: MotionScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalMotionScheme.current

/**
 * Official Material 3 Easing & Duration Motion Tokens.
 *
 * Suggested easing and duration pairs:
 * - Enter the screen: Emphasized decelerate (400ms - 500ms)
 * - Exit the screen: Emphasized accelerate (200ms)
 * - Begin and end on screen: Emphasized (500ms)
 *
 * Reference: https://m3.material.io/styles/motion/easing-and-duration/applying-easing-and-duration
 */
object MaterialMotionTokens {
    // Easing curves
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val StandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

    // Duration tokens (milliseconds)
    const val DurationShort1 = 50
    const val DurationShort2 = 100
    const val DurationShort3 = 150
    const val DurationShort4 = 200
    const val DurationMedium1 = 250
    const val DurationMedium2 = 300
    const val DurationMedium3 = 350
    const val DurationMedium4 = 400
    const val DurationLong1 = 450
    const val DurationLong2 = 500
    const val DurationLong3 = 550
    const val DurationLong4 = 600
}

