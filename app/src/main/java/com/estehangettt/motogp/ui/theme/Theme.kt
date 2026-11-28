package com.estehangettt.motogp.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Warna Dasar
val DarkBg = Color(0xFF09090B)
val TextMain = Color(0xFFFAFAFA)
val TextMuted = Color(0xFF9CA3AF)
val GlassWhite = Color(0x08FFFFFF)
val GlassBorder = Color(0x14FFFFFF)

// Warna Aksen Dinamis
data class AccentColors(
    val motogp: Color = Color(0xFFE10600),
    val moto2: Color = Color(0xFF0072BC),
    val moto3: Color = Color(0xFFFF8C00)
)
val LocalAccent = staticCompositionLocalOf { AccentColors() }

private val DarkColorScheme = darkColorScheme(
    background = DarkBg,
    surface = Color(0xFF131317),
    onBackground = TextMain,
    onSurface = TextMain,
)

@Composable
fun MotoGPReplayTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAccent provides AccentColors()) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content
        )
    }
}

// Modifier Efek Kaca (Glassmorphism)
fun Modifier.glassmorphism(
    cornerRadius: Int = 24,
    backgroundColor: Color = GlassWhite
) = this.then(
    Modifier
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(backgroundColor.copy(alpha = 0.08f), backgroundColor.copy(alpha = 0.02f))
            ),
            shape = RoundedCornerShape(cornerRadius.dp)
        )
        .border(
            width = 1.dp,
            color = GlassBorder,
            shape = RoundedCornerShape(cornerRadius.dp)
        )
)
