package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════
//  SHIMMER EFFECT — Brillo que recorre el botón horizontalmente
// ═══════════════════════════════════════════════════════════════

/**
 * Efecto shimmer que recorre un composable de izquierda a derecha.
 *
 * Se usa sobre el botón de login para darle un "brillo" premium
 * que indica que es el CTA principal. El efecto es sutil pero
 * perceptible y se repite cada ~2.5 segundos.
 */
@Composable
fun ShimmerOverlay(
    modifier: Modifier = Modifier,
    shimmerColor: Color = Color.White.copy(alpha = 0.15f),
    durationMs: Int = 2500,
    shimmerWidth: Float = 0.3f // Ancho del brillo como fracción del total
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -shimmerWidth,
        targetValue = 1f + shimmerWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val shimmerStart = shimmerProgress * size.width
        val shimmerEnd = (shimmerProgress + shimmerWidth) * size.width

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    shimmerColor,
                    shimmerColor.copy(alpha = shimmerColor.alpha * 0.6f),
                    Color.Transparent
                ),
                startX = shimmerStart,
                endX = shimmerEnd
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  GLASSMORPHISM CARD — Borde degradado + fondo semitransparente
// ═══════════════════════════════════════════════════════════════

/**
 * Modificador para aplicar efecto Glassmorphism a un Card o Box.
 *
 * El efecto consiste en:
 * 1. Fondo semitransparente oscuro
 * 2. Borde con gradiente sutil (blanco → rojo → transparente)
 * 3. Esquinas redondeadas amplias
 *
 * Inspirado en interfaces de iOS, Apple Vision Pro y dashboards premium.
 */
@Composable
fun Modifier.glassCard(
    cornerRadius: Dp = 28.dp,
    borderAlpha: Float = 0.12f,
    backgroundAlpha: Float = 0.6f
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .clip(shape)
        .background(BrandColors.Surface1.copy(alpha = backgroundAlpha))
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = borderAlpha),
                    BrandColors.BrandRed.copy(alpha = borderAlpha * 0.5f),
                    Color.White.copy(alpha = borderAlpha * 0.3f),
                    Color.Transparent
                )
            ),
            shape = shape
        )
}

// ═══════════════════════════════════════════════════════════════
//  AURORA BACKGROUND — Gradientes orgánicos animados
// ═══════════════════════════════════════════════════════════════

/**
 * Fondo animado tipo "Aurora Boreal" con colores corporativos.
 *
 * Genera gradientes radiales que se mueven lentamente, creando
 * un efecto orgánico y premium. Mucho más sofisticado que partículas.
 *
 * Los colores usan la paleta PLANTA DE MANUFACTURA: Rojo → Dorado → Rojo oscuro
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    primaryColor: Color = BrandColors.BrandRed,
    secondaryColor: Color = BrandColors.GoldAccent,
    tertiaryColor: Color = BrandColors.BrandDarkRed,
    alphaIntensity: Float = 0.08f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Mancha 1 — Rojo principal (se mueve en patrón circular)
        val cx1 = w * (0.3f + 0.2f * sin(phase1))
        val cy1 = h * (0.2f + 0.15f * cos(phase1 * 0.7f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = alphaIntensity * 1.2f),
                    primaryColor.copy(alpha = alphaIntensity * 0.4f),
                    Color.Transparent
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.45f
            ),
            center = Offset(cx1, cy1),
            radius = w * 0.45f
        )

        // Mancha 2 — Dorada (se mueve más lento, en la zona media)
        val cx2 = w * (0.7f + 0.15f * cos(phase2))
        val cy2 = h * (0.5f + 0.2f * sin(phase2 * 0.6f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    secondaryColor.copy(alpha = alphaIntensity * 0.8f),
                    secondaryColor.copy(alpha = alphaIntensity * 0.2f),
                    Color.Transparent
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.35f
            ),
            center = Offset(cx2, cy2),
            radius = w * 0.35f
        )

        // Mancha 3 — Rojo oscuro (zona inferior, movimiento suave)
        val cx3 = w * (0.5f + 0.25f * sin(phase3 * 0.8f))
        val cy3 = h * (0.75f + 0.1f * cos(phase3))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    tertiaryColor.copy(alpha = alphaIntensity),
                    tertiaryColor.copy(alpha = alphaIntensity * 0.3f),
                    Color.Transparent
                ),
                center = Offset(cx3, cy3),
                radius = w * 0.4f
            ),
            center = Offset(cx3, cy3),
            radius = w * 0.4f
        )
    }
}

// ═══════════════════════════════════════════════════════════════
//  SCAN LINE EFFECT — Línea horizontal que recorre una superficie
// ═══════════════════════════════════════════════════════════════

/**
 * Efecto de "línea de escaneo" que recorre verticalmente un composable.
 * Da la sensación de un escáner futurista pasando por el card.
 */
@Composable
fun ScanLineEffect(
    modifier: Modifier = Modifier,
    lineColor: Color = BrandColors.BrandRed.copy(alpha = 0.06f),
    durationMs: Int = 4000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val progress by infiniteTransition.animateFloat(
        initialValue = -0.1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val y = progress * size.height
        val lineHeight = size.height * 0.08f

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    lineColor,
                    lineColor.copy(alpha = lineColor.alpha * 1.5f),
                    lineColor,
                    Color.Transparent
                ),
                startY = y - lineHeight / 2,
                endY = y + lineHeight / 2
            )
        )
    }
}
