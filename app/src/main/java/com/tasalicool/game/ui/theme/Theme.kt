package com.tasalicool.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkRedPrimary = Color(0xFF8B0000)
private val GoldSecondary = Color(0xFFFFB800)
private val PurpleTertiary = Color(0xFF6A1B9A)

private val darkColorScheme = darkColorScheme(
    primary = DarkRedPrimary,
    secondary = GoldSecondary,
    tertiary = PurpleTertiary,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E)
)

private val lightColorScheme = lightColorScheme(
    primary = DarkRedPrimary,
    secondary = GoldSecondary,
    tertiary = PurpleTertiary,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF)
)

@Composable
fun TasalicoolTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (useDarkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography(), content = content)
}
