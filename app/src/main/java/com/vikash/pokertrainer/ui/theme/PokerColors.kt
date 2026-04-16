package com.vikash.pokertrainer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Theme-aware semantic colors. Colors here swap between light and dark mode.
 * Non-theme colors (card suits, action colors, accents) stay constant across themes.
 */
data class PokerColors(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val card: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val divider: Color,
    val isDark: Boolean
)

// Dark palette (original)
val DarkPokerColors = PokerColors(
    background = Color(0xFF0F1922),
    surface = Color(0xFF1B2838),
    surfaceHigh = Color(0xFF243447),
    card = Color(0xFF2D4157),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFB0BEC5),
    textMuted = Color(0xFF78909C),
    divider = Color(0xFF2D4157),
    isDark = true
)

// Light palette — warm neutral tones with good contrast
val LightPokerColors = PokerColors(
    background = Color(0xFFF5F7FA),
    surface = Color(0xFFFFFFFF),
    surfaceHigh = Color(0xFFF0F2F5),
    card = Color(0xFFE8EAF0),
    textPrimary = Color(0xFF1A1F2E),
    textSecondary = Color(0xFF4A5568),
    textMuted = Color(0xFF8A95A5),
    divider = Color(0xFFE2E8F0),
    isDark = false
)

val LocalPokerColors = compositionLocalOf { DarkPokerColors }

/**
 * Convenience accessor inside composables.
 */
val pokerColors: PokerColors
    @Composable
    @ReadOnlyComposable
    get() = LocalPokerColors.current
