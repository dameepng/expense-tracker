package com.example.expense_tracker.ui.theme

import androidx.compose.ui.graphics.Color

// ── UX Design Tokens: Material 3 (Fallback) ───────────────────────

val PrimaryLight = Color(0xFF1B6EF3)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFD6E3FF)
val OnPrimaryContainerLight = Color(0xFF001B3E)

val PrimaryDark = Color(0xFF8DB8FF)
val OnPrimaryDark = Color(0xFF00317A)
val PrimaryContainerDark = Color(0xFF0049B5)
val OnPrimaryContainerDark = Color(0xFFD6E3FF)

val SecondaryLight = Color(0xFF555F71)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD9E3F8)
val OnSecondaryContainerLight = Color(0xFF121C2B)

val SecondaryDark = Color(0xFFBBC7DB)
val OnSecondaryDark = Color(0xFF283141)
val SecondaryContainerDark = Color(0xFF3E475B)
val OnSecondaryContainerDark = Color(0xFFD9E3F8)

val TertiaryLight = Color(0xFF705575)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFAD8FF)
val OnTertiaryContainerLight = Color(0xFF28132E)

val TertiaryDark = Color(0xFFD7BDDD)
val OnTertiaryDark = Color(0xFF3B2640)
val TertiaryContainerDark = Color(0xFF553B5B)
val OnTertiaryContainerDark = Color(0xFFFAD8FF)

// Surface & Background
val SurfaceLight = Color(0xFFFEFBFF)
val SurfaceDark = Color(0xFF1B1B1F)
val OnSurfaceLight = Color(0xFF1B1B1F)
val OnSurfaceDark = Color(0xFFE4E2E6)
val SurfaceVariantLight = Color(0xFFE1E2EC)
val SurfaceVariantDark = Color(0xFF44474F)
val OnSurfaceVariantLight = Color(0xFF44474F)
val OnSurfaceVariantDark = Color(0xFFC4C6D0)
val OutlineLight = Color(0xFF74777F)
val OutlineDark = Color(0xFF8E9099)

// Error
val ErrorLight = Color(0xFFBA1A1A)
val ErrorDark = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerLight = Color(0xFFFFDAD6)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerLight = Color(0xFF410002)
val OnErrorContainerDark = Color(0xFFFFDAD6)

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
