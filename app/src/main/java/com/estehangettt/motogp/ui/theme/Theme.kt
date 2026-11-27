package com.estehangettt.motogp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MotoGPBlack = Color(0xFF0A0A0A)
val MotoGPRed = Color(0xFFE10600)
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)
val TextGray = Color(0xFFAAAAAA)

private val DarkColorScheme = darkColorScheme(
    primary = MotoGPRed,
    background = MotoGPBlack,
    surface = Color(0xFF151515),
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun MotoGPReplayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
