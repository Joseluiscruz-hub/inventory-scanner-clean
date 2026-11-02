package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun ParticlesAnimatedBackground(
    modifier: Modifier = Modifier,
    particleColor: Color = Color(0xFFEF233C),
    particleCount: Int = 32,
    particleMinSize: Float = 6f,
    particleMaxSize: Float = 18f,
    speed: Int = 9000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(speed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (i in 0 until particleCount) {
            val angle = (i.toFloat() / particleCount) * 2f * PI.toFloat() + phase
            val radius = (w.coerceAtMost(h) / 2f) * (0.3f + 0.6f * (i % 7) / 7f)
            val x = w / 2f + radius * sin(angle)
            val y = h / 2f + radius * sin(angle * 1.2f)
            val size = particleMinSize + (particleMaxSize - particleMinSize) * ((i % 5) / 5f)
            drawCircle(
                color = particleColor.copy(alpha = 0.13f + 0.07f * (i % 3)),
                radius = size,
                center = Offset(x, y)
            )
        }
    }
}

