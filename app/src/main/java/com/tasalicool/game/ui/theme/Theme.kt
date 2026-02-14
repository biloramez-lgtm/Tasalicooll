package com.tasalicool.game.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/* ==================== GAME COLORS ==================== */

private val DarkRedPrimary = Color(0xFF8B0000)
private val GoldSecondary = Color(0xFFFFB800)
private val PurpleTertiary = Color(0xFF6A1B9A)

/* ==================== DARK SCHEME ==================== */

private val DarkColorScheme = darkColorScheme(
    primary = DarkRedPrimary,
    onPrimary = Color.White,

    secondary = GoldSecondary,
    onSecondary = Color.Black,

    tertiary = PurpleTertiary,
    onTertiary = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

/* ==================== LIGHT SCHEME ==================== */

private val LightColorScheme = lightColorScheme(
    primary = DarkRedPrimary,
    onPrimary = Color.White,

    secondary = GoldSecondary,
    onSecondary = Color.Black,

    tertiary = PurpleTertiary,
    onTertiary = Color.White,

    background = Color(0xFFFAFAFA),
    onBackground = Color.Black,

    surface = Color(0xFFFFFFFF),
    onSurface = Color.Black
)

/* ==================== THEME ==================== */

@Composable
fun TasalicoolTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,   // 👈 مهم جداً
        content = content
    )
}
