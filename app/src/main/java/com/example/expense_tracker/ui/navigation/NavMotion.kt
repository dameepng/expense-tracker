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
 * Implements official Material 3 transition patterns:
 * 1. Top-Level Navigation (Bottom Nav tabs) -> Fade Through (no spatial horizontal slide)
 * 2. Forward Navigation (Deeper hierarchy) -> Shared Axis X Push (slide with parallax + fade)
 * 3. Backward Navigation (Pop back stack) -> Shared Axis X Pop (slide right + parallax return)
 *
 * All transitions respect system accessibility settings via [isReduceMotion].
 */
object NavMotion {

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
     * Standard M3 Enter Transition for forward navigation and top-level switches.
     */
    fun enterTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): EnterTransition {
        if (isReduceMotion) return EnterTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        return if (isTopLevelSwitch(fromRoute, toRoute)) {
            // M3 Fade Through pattern for top-level tabs
            fadeIn(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1, // 250ms
                    delayMillis = MaterialMotionTokens.DurationShort2, // 100ms
                    easing = LinearEasing
                )
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1, // 250ms
                    delayMillis = MaterialMotionTokens.DurationShort2, // 100ms
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            )
        } else {
            // M3 Shared Axis X: Forward push (incoming child enters from right)
            scope.slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationLong1, // 450ms
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1, // 250ms
                    easing = LinearEasing
                )
            )
        }
    }

    /**
     * Standard M3 Exit Transition for forward navigation and top-level switches.
     */
    fun exitTransition(
        scope: AnimatedContentTransitionScope<NavBackStackEntry>,
        isReduceMotion: Boolean
    ): ExitTransition {
        if (isReduceMotion) return ExitTransition.None

        val fromRoute = scope.initialState.destination.route
        val toRoute = scope.targetState.destination.route

        return if (isTopLevelSwitch(fromRoute, toRoute)) {
            // M3 Fade Through: Outgoing top-level tab fades out quickly
            fadeOut(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationShort3, // 150ms
                    easing = LinearEasing
                )
            )
        } else {
            // M3 Shared Axis X: Forward push (outgoing parent shifts slightly left with parallax)
            scope.slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                targetOffset = { fullWidth -> (fullWidth * 0.30f).toInt() },
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium4, // 400ms
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationShort4, // 200ms
                    easing = LinearEasing
                )
            )
        }
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

        return if (isTopLevelSwitch(fromRoute, toRoute)) {
            fadeIn(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1,
                    delayMillis = MaterialMotionTokens.DurationShort2,
                    easing = LinearEasing
                )
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1,
                    delayMillis = MaterialMotionTokens.DurationShort2,
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            )
        } else {
            // M3 Shared Axis X: Backward pop (parent returns from left parallax offset)
            scope.slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                initialOffset = { fullWidth -> (fullWidth * 0.30f).toInt() },
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationLong1, // 450ms
                    easing = MaterialMotionTokens.EmphasizedDecelerate
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationMedium1, // 250ms
                    easing = LinearEasing
                )
            )
        }
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

        return if (isTopLevelSwitch(fromRoute, toRoute)) {
            fadeOut(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationShort3,
                    easing = LinearEasing
                )
            )
        } else {
            // M3 Shared Axis X: Backward pop (child slides completely out to the right)
            scope.slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                targetOffset = { fullWidth -> fullWidth },
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationShort4, // 200ms
                    easing = MaterialMotionTokens.EmphasizedAccelerate
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = MaterialMotionTokens.DurationShort4, // 200ms
                    easing = LinearEasing
                )
            )
        }
    }
}
