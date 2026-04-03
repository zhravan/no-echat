package com.zhravan.noechat.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1B4332),
    onPrimary = Color.White,
    secondary = Color(0xFF2D6A4F),
    background = Color(0xFFF8F9FA),
    surface = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF95D5B2),
    onPrimary = Color(0xFF1B4332),
    secondary = Color(0xFF74C69D)
)

@Composable
fun NoEchatTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = NoEchatTypography,
        content = content
    )
}
