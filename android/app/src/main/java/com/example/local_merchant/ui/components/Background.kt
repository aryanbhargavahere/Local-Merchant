package com.example.local_merchant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.local_merchant.ui.theme.*

@Composable
fun Background(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow")

    val animProgress1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )

    val animProgress2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = ObsidianBg)

            // Dynamic Orb 1 (Cyan / Purple drift)
            val center1 = Offset(
                x = size.width * (0.2f + 0.6f * animProgress1),
                y = size.height * (0.15f + 0.3f * (1f - animProgress1))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyberPurple.copy(alpha = 0.28f), Color.Transparent) ,
                    center = center1,
                    radius = size.width * 0.75f
                ),
                center = center1,
                radius = size.width * 0.75f
            )

            // Dynamic Orb 2 (Electric Blue / Emerald drift)
            val center2 = Offset(
                x = size.width * (0.8f - 0.5f * animProgress2),
                y = size.height * (0.65f + 0.2f * animProgress2)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ElectricBlue.copy(alpha = 0.22f), Color.Transparent),
                    center = center2,
                    radius = size.width * 0.85f
                ),
                center = center2,
                radius = size.width * 0.85f
            )
        }

        content()
    }
}