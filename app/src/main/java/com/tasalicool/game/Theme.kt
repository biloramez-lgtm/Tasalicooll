package com.tarneeb.game.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define colors
private val DarkRedPrimary = Color(0xFFB71C1C)
private val DarkRedSecondary = Color(0xFFF57C00)
private val DarkRedTertiary = Color(0xFF6A1B9A)

private val LightRedPrimary = Color(0xFFD32F2F)
private val LightRedSecondary = Color(0xFFFF7043)
private val LightRedTertiary = Color(0xFF7B1FA2)

private val DarkBackground = Color(0xFF121212)
private val DarkSurface = Color(0xFF1E1E1E)
private val LightBackground = Color(0xFFFAFAFA)
private val LightSurface = Color(0xFFFFFFFF)

private val darkColorScheme = darkColorScheme(
    primary = DarkRedPrimary,
    secondary = DarkRedSecondary,
    tertiary = DarkRedTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    error = Color(0xFFF44336),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFFE0E0E0),
    onSurface = Color(0xFFBDBDBD)
)

private val lightColorScheme = lightColorScheme(
    primary = LightRedPrimary,
    secondary = LightRedSecondary,
    tertiary = LightRedTertiary,
    background = LightBackground,
    surface = LightSurface,
    error = Color(0xFFB3261E),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF49454F)
)

@Composable
fun TarneebTheme(
    useDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TarneebTypography,
        content = content
    )
}
