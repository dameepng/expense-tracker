package com.example.expense_tracker.ui.theme

import androidx.compose.ui.graphics.Color

// ── UX Design Tokens: Light Theme ──────────────────────────────────

val Blue600 = Color(0xFF1B6EF3)
val Blue200 = Color(0xFF8DB8FF)
val OnBlueLight = Color(0xFFFFFFFF)
val OnBlueDark = Color(0xFF00317A)
val BlueContainerLight = Color(0xFFD6E3FF)
val BlueContainerDark = Color(0xFF0049B5)
val OnBlueContainerLight = Color(0xFF001B3E)
val OnBlueContainerDark = Color(0xFFD6E3FF)

// Surface
val SurfaceLight = Color(0xFFFEFBFF)
val SurfaceDark = Color(0xFF1B1B1F)
val OnSurfaceLight = Color(0xFF1B1B1F)
val OnSurfaceDark = Color(0xFFE4E2E6)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val SurfaceVariantDark = Color(0xFF44474F)
val OnSurfaceVariantLight = Color(0xFF44474F)
val OnSurfaceVariantDark = Color(0xFFC4C6D0)

// Error
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Category reference colors (for bar charts / icons)
val CategoryColors = listOf(
    Color(0xFFFF8C00), // Makanan - Orange
    Color(0xFF1E90FF), // Transport - Blue
    Color(0xFF8A2BE2), // Belanja - Purple
    Color(0xFFFF1493), // Hiburan - Pink
    Color(0xFFDC143C), // Tagihan - Red
    Color(0xFF2E8B57), // Kesehatan - Green
    Color(0xFF708090), // Lainnya - Gray
)

fun categoryColor(index: Int): Color =
    CategoryColors.getOrElse(index % CategoryColors.size) { Color.Gray }
