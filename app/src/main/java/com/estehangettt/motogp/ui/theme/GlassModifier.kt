package com.estehangettt.motogp.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

// Efek Kaca (Frosted Glass)
fun Modifier.glassmorphism(
    cornerRadius: Int = 16,
    blurRadius: Int = 16,
    backgroundColor: androidx.compose.ui.graphics.Color = GlassWhite
) = this.then(
    Modifier
        .blur(blurRadius.dp) // Efek blur khas iOS (Butuh Android 12+)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.05f))
            ),
            shape = RoundedCornerShape(cornerRadius.dp)
        )
        .border(
            width = 1.dp,
            color = GlassBorder,
            shape = RoundedCornerShape(cornerRadius.dp)
        )
)

// Efek Animasi Bouncy (iOS Style) pas diklik
@Composable
fun Modifier.bouncyClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "bouncy_scale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Hilangkan ripple bawaan Android, biar ala iOS
            onClick = onClick
        )
}
