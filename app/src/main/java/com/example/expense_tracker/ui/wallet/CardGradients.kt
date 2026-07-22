package com.example.expense_tracker.ui.wallet

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CardGradient(
    val id: String,
    val name: String,
    val startColor: Color,
    val endColor: Color
) {
    val brush: Brush
        get() = Brush.linearGradient(listOf(startColor, endColor))
}

object CardGradients {
    val SunsetRose = CardGradient(
        id = "sunset_rose",
        name = "Sunset Rose",
        startColor = Color(0xFFE96196),
        endColor = Color(0xFFC084C4)
    )

    val OceanBlue = CardGradient(
        id = "ocean_blue",
        name = "Ocean Blue",
        startColor = Color(0xFF1A6DFF),
        endColor = Color(0xFF00D4FF)
    )

    val MidnightPurple = CardGradient(
        id = "midnight_purple",
        name = "Midnight Purple",
        startColor = Color(0xFF4A148C),
        endColor = Color(0xFF7C4DFF)
    )

    val EmeraldGreen = CardGradient(
        id = "emerald_green",
        name = "Emerald Green",
        startColor = Color(0xFF00695C),
        endColor = Color(0xFF26A69A)
    )

    val ObsidianDark = CardGradient(
        id = "obsidian_dark",
        name = "Obsidian Dark",
        startColor = Color(0xFF1A1A2E),
        endColor = Color(0xFF16213E)
    )

    val options: List<CardGradient> = listOf(
        SunsetRose,
        OceanBlue,
        MidnightPurple,
        EmeraldGreen,
        ObsidianDark
    )

    fun getGradient(colorId: String?): CardGradient {
        return options.find { it.id.equals(colorId, ignoreCase = true) } ?: SunsetRose
    }
}
