package com.example.expense_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.expense_tracker.ui.theme.spacing

private val DEFAULT_ADAPTIVE_MAX_WIDTH: Dp = 840.dp

/**
 * Nilai spacing dan padding adaptif sesuai prinsip Material 3 Expressive Layout.
 * - Compact (<600dp): Smartphone portrait
 * - Medium (600dp - 839dp): Foldable unfolded, tablet portrait, phone landscape
 * - Expanded (≥840dp): Tablet landscape, Chromebook, Desktop
 */
@Immutable
data class AdaptiveSpacing(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cardSpacing: Dp
)

/**
 * Menghitung spacing adaptif berdasarkan lebar layar saat ini melalui MaterialTheme.spacing.
 */
@Composable
fun rememberAdaptiveSpacing(): AdaptiveSpacing {
    val spacing = MaterialTheme.spacing
    return remember(spacing) {
        AdaptiveSpacing(
            horizontalPadding = spacing.screenMargin,
            verticalPadding = spacing.screenMargin,
            cardSpacing = spacing.itemGap
        )
    }
}

/**
 * Container pembungkus konten utama yang membatasi lebar maksimal (max-width bounding)
 * dan menempatkannya di tengah layar (centered) pada layar tablet/lebar,
 * mencegah form atau kartu membentang terlalu lebar.
 */
@Composable
fun AdaptiveContentContainer(
    maxWidth: Dp = DEFAULT_ADAPTIVE_MAX_WIDTH,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}
