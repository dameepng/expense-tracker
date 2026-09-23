package com.example.expense_tracker.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavBackStackEntry
import com.example.expense_tracker.ui.theme.MaterialMotionTokens

/**
 * CompositionLocal providing access to [SharedTransitionScope] across the NavHost tree.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal providing access to the current destination's [AnimatedVisibilityScope].
 */
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Material 3 Expressive Navigation Motion System.
 *
 * Implements refined Material 3 transition patterns:
 * 1. Top-Level Navigation (Bottom Nav tabs) -> Instant, crisp Crossfade (150ms, 0ms delay, no scale bounce)
 * 2. Modal Form Navigation (Input Screen) -> Vertical Slide-Up (200ms) / Slide-Down (180ms) with Predictive Back
 * 3. Hierarchical Forward Navigation (List -> Detail) -> Shared Axis X Push (220ms, subtle parallax)
 * 4. Hierarchical Backward Navigation (Detail -> Parent) -> Shared Axis X Pop (200ms, gesture-friendly)
 *
 * All transitions respect system accessibility settings via [isReduceMotion].
 */
object NavMotion {
    private const val PARALLAX_OFFSET_FACTOR = 0.15f

    /**
     * Determines whether a transition between two routes is a top-level switch
     * between bottom navigation tabs (Home, Wallet, Summary, Profile).
     */
    fun isTopLevelSwitch(fromRoute: String?, toRoute: String?): Boolean {
        if (fromRoute == null || toRoute == null) return false
        val from = fromRoute.substringBefore('?').substringBefore('/')
        val to = toRoute.substringBefore('?').substringBefore('/')
        return from in NavRoutes.topLevelTabs && to in NavRoutes.topLevelTabs
    }

    /**
     * Determines whether a route is the InputScreen (transaction input form).
     */
    fun isInputRoute(route: String?): Boolean {
        if (route == null) return false
        val base = route.substringBefore('?').substringBefore('/')
        return base == NavRoutes.INPUT_BASE
    }

    /**
     * Standard M3 Enter Transition for forward navigation, top-level switches, and modal entry.
     */
    fun enterTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): EnterTransition {
        if (isReduceMotion) return EnterTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        // Modal input screen slides up from bottom
        if (isInputRoute(toRoute)) {
            return scope.slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Top-level peer tab switch: instant crossfade without artificial delay or whole-screen scaling
        if (isTopLevelSwitch(fromRoute, toRoute)) {
            return fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Standard hierarchical forward navigation (List -> Detail)
        return scope.slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            animationSpec = tween(
                durationMillis = 220,
                easing = MaterialMotionTokens.EmphasizedDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 180,
                easing = LinearEasing
            )
        )
    }

    /**
     * Standard M3 Exit Transition for forward navigation, top-level switches, and modal exit.
     */
    fun exitTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): ExitTransition {
        if (isReduceMotion) return ExitTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        // Screen beneath modal input fades subtly
        if (isInputRoute(toRoute)) {
            return fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Top-level peer tab switch: instant crossfade
        if (isTopLevelSwitch(fromRoute, toRoute)) {
            return fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Standard hierarchical forward push (outgoing parent shifts slightly left with parallax)
        return scope.slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Start,
            targetOffset = { fullWidth -> (fullWidth * PARALLAX_OFFSET_FACTOR).toInt() },
            animationSpec = tween(
                durationMillis = 220,
                easing = MaterialMotionTokens.EmphasizedDecelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 150,
                easing = LinearEasing
            )
        )
    }

    /**
     * Standard M3 Pop Enter Transition for backward navigation (returning to parent).
     */
    fun popEnterTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): EnterTransition {
        if (isReduceMotion || scope.initialState.destination.route == null) return EnterTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        // Returning from modal input: parent fades back in immediately
        if (isInputRoute(fromRoute)) {
            return fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Top-level peer tab switch
        if (isTopLevelSwitch(fromRoute, toRoute)) {
            return fadeIn(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Backward pop: parent returns from left parallax offset with gesture-friendly curve
        return scope.slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            initialOffset = { fullWidth -> (fullWidth * PARALLAX_OFFSET_FACTOR).toInt() },
            animationSpec = tween(
                durationMillis = 200,
                easing = MaterialMotionTokens.EmphasizedDecelerate
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = 180,
                easing = LinearEasing
            )
        )
    }

    /**
     * Standard M3 Pop Exit Transition for backward navigation (popping child out).
     */
    fun popExitTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): ExitTransition {
        if (isReduceMotion || scope.initialState.destination.route == null) return ExitTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        // Modal input dismiss: slides down smoothly to reveal parent underneath (Predictive Back compatible)
        if (isInputRoute(fromRoute)) {
            return scope.slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(
                    durationMillis = 180,
                    easing = MaterialMotionTokens.EmphasizedAccelerate
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Top-level peer tab switch
        if (isTopLevelSwitch(fromRoute, toRoute)) {
            return fadeOut(
                animationSpec = tween(
                    durationMillis = 150,
                    easing = LinearEasing
                )
            )
        }

        // Backward pop: child slides completely out to the right with synchronized fade
        return scope.slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.End,
            targetOffset = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = 200,
                easing = MaterialMotionTokens.EmphasizedAccelerate
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = 180,
                easing = LinearEasing
            )
        )
    }
}
