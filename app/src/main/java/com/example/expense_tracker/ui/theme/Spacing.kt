package com.example.expense_tracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 (M3) Spacing System berbasis skala 8dp.
 * Sumber resmi: https://m3.material.io/styles/spacing/overview
 *
 * Unit dasar: space100 = 8.dp (1x baseline)
 * Menggunakan token terukur, alias semantik, serta token adaptif kontekstual
 * yang dapat beradaptasi berdasarkan tipe perangkat (Compact, Medium, Expanded).
 */
@Immutable
data class Spacing(
    // ── Skala 8dp M3 (Measurement Tokens) ───────────────────────────
    val space0: Dp = 0.dp,
    val space25: Dp = 2.dp,     // 0.25x baseline
    val space50: Dp = 4.dp,     // 0.5x baseline
    val space75: Dp = 6.dp,     // 0.75x baseline
    val space100: Dp = 8.dp,   // 1x baseline
    val space125: Dp = 10.dp,   // 1.25x baseline
    val space150: Dp = 12.dp,   // 1.5x baseline
    val space200: Dp = 16.dp,   // 2x baseline
    val space250: Dp = 20.dp,   // 2.5x baseline
    val space300: Dp = 24.dp,   // 3x baseline
    val space350: Dp = 28.dp,   // 3.5x baseline
    val space400: Dp = 32.dp,   // 4x baseline
    val space500: Dp = 40.dp,   // 5x baseline
    val space600: Dp = 48.dp,   // 6x baseline
    val space700: Dp = 56.dp,   // 7x baseline
    val space800: Dp = 64.dp,   // 8x baseline
    val space900: Dp = 72.dp,   // 9x baseline
    val space1000: Dp = 80.dp,  // 10x baseline

    // ── Alias Semantik ──────────────────────────────────────────────
    val none: Dp = space0,
    val extraSmall: Dp = space50,        // 4.dp
    val small: Dp = space100,            // 8.dp
    val medium: Dp = space200,           // 16.dp
    val large: Dp = space300,            // 24.dp
    val extraLarge: Dp = space400,       // 32.dp
    val extraExtraLarge: Dp = space600,  // 48.dp

    // ── Token Adaptif / Kontekstual (Contextual & Responsive) ────────
    val screenMargin: Dp = space200,     // Margin tepi layar: 16dp (phone), 24dp (medium), 32dp (expanded)
    val sectionGap: Dp = space200,       // Jarak antar kartu/bagian utama: 16dp / 24dp / 32dp
    val cardPadding: Dp = space200,      // Padding dalam kontainer kartu: 16dp / 20dp / 24dp
    val itemGap: Dp = space150,          // Jarak antar elemen daftar: 12dp / 16dp / 20dp
    val inlineGap: Dp = space100         // Jarak horizontal icon-to-text / row item: 8dp
)

/**
 * CompositionLocal untuk menyediakan Spacing ke seluruh hirarki Composable.
 */
val LocalSpacing = staticCompositionLocalOf { Spacing() }

/**
 * Extension property pada MaterialTheme agar token spacing dapat diakses secara ringkas:
 * contoh: `MaterialTheme.spacing.space100` atau `MaterialTheme.spacing.screenMargin`.
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current

/**
 * Menghasilkan konfigurasi spacing adaptif berdasarkan lebar layar saat ini:
 * - Compact (<600dp)
 * - Medium (600dp - 839dp)
 * - Expanded (≥840dp)
 */
@Composable
fun rememberAdaptiveSpacingConfig(): Spacing {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return when {
        screenWidth < 600 -> Spacing(
            screenMargin = 16.dp,
            sectionGap = 16.dp,
            cardPadding = 16.dp,
            itemGap = 12.dp,
            inlineGap = 8.dp
        )
        screenWidth < 840 -> Spacing(
            screenMargin = 24.dp,
            sectionGap = 24.dp,
            cardPadding = 20.dp,
            itemGap = 16.dp,
            inlineGap = 8.dp
        )
        else -> Spacing(
            screenMargin = 32.dp,
            sectionGap = 32.dp,
            cardPadding = 24.dp,
            itemGap = 16.dp,
            inlineGap = 12.dp
        )
    }
}
