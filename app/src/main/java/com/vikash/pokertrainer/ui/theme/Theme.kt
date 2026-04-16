package com.vikash.pokertrainer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val DarkColorScheme = darkColorScheme(
    primary = PokerTableGreen,
    secondary = GoldAccent,
    tertiary = ChipBlue,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = DarkBackground,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = IncorrectRed,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PokerTableGreen,
    secondary = GoldAccent,
    tertiary = ChipBlue,
    background = LightPokerColors.background,
    surface = LightPokerColors.surface,
    onPrimary = TextPrimary,
    onSecondary = LightPokerColors.textPrimary,
    onTertiary = TextPrimary,
    onBackground = LightPokerColors.textPrimary,
    onSurface = LightPokerColors.textPrimary,
    error = IncorrectRed,
    onError = TextPrimary
)

@Composable
fun PokerTrainerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val pokerColors = if (darkTheme) DarkPokerColors else LightPokerColors

    CompositionLocalProvider(LocalPokerColors provides pokerColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography,
            content = content
        )
    }
}
