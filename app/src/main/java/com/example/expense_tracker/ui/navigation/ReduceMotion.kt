package com.example.expense_tracker.ui.navigation

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Observes and returns whether system reduce motion or animation scales are set to 0.
 */
@Composable
fun rememberIsReduceMotion(): Boolean {
    val context = LocalContext.current
    var isReduceMotion by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        isReduceMotion = checkReduceMotion(context)
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                isReduceMotion = checkReduceMotion(context)
            }
        }
        val uri1 = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
        val uri2 = Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE)
        context.contentResolver.registerContentObserver(uri1, false, observer)
        context.contentResolver.registerContentObserver(uri2, false, observer)
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }
    return isReduceMotion
}

private fun checkReduceMotion(context: Context): Boolean {
    return try {
        val animatorScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transitionScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        animatorScale == 0f || transitionScale == 0f
    } catch (_: Exception) {
        false
    }
}
