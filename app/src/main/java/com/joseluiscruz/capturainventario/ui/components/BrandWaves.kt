package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun BrandWaves(
    modifier: Modifier = Modifier,
    waveHeight: Float = 0.48f,
    waveAmplitude: Float = 38f,
    waveSpeed: Int = 2800,
    opacityFront: Float = 0.88f,
    opacityBack: Float = 0.35f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(waveSpeed, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val colors = listOf(
        BrandColors.BrandRed,
        BrandColors.BrandDarkRed,
        BrandColors.BrandRed.copy(alpha = 0.5f),
        Color.Transparent
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // CAPA TRASERA
        val pathBack = Path().apply {
            moveTo(0f, h)
            var x = 0f
            while (x <= w + 5f) {
                val relX = x / w
                val y =
                    (sin(relX * 1.8 * PI + phase + PI) * (waveAmplitude * 0.7) + (h * (1f - waveHeight * 0.95))).toFloat()
                lineTo(x, y.toFloat())
                x += 4f
            }
            lineTo(w, h)
            close()
        }
        drawPath(
            path = pathBack,
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(w, 0f)
            ),
            alpha = opacityBack
        )

        // CAPA FRONTAL
        val pathFront = Path().apply {
            moveTo(0f, h)
            var x = 0f
            while (x <= w + 5f) {
                val relX = x / w
                val y = (sin(relX * 1.5 * PI + phase) * waveAmplitude + (h * (1f - waveHeight))).toFloat()
                lineTo(x, y.toFloat())
                x += 4f
            }
            lineTo(w, h)
            close()
        }
        drawPath(
            path = pathFront,
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(0f, 0f),
                end = Offset(w, 0f)
            ),
            alpha = opacityFront
        )
    }
}
