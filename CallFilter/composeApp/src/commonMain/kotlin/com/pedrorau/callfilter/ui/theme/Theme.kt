package com.pedrorau.callfilter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF137FEC)
val SuccessGreen = Color(0xFF2E7D32)
val WarningAmber = Color(0xFFF57C00)
val ErrorRed = Color(0xFFD32F2F)
val BackgroundLight = Color(0xFFF6F7F8)
val TextPrimary = Color(0xFF111418)
val TextSecondary = Color(0xFF617589)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = TextPrimary,
    surface = Color.White,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundLight,
    error = ErrorRed
)

@Composable
fun CallFilterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
