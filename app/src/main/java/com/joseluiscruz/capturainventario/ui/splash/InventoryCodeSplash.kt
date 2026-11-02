package com.joseluiscruz.capturainventario.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.R
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

internal object SplashTimeline {
    const val DURATION_MILLIS = 3_500

    fun segment(value: Float, start: Float, end: Float): Float {
        if (end <= start) return if (value >= end) 1f else 0f
        return ((value - start) / (end - start)).coerceIn(0f, 1f)
    }
}

private val DeepNavy = Color(0xFF06111F)
private val Navy = Color(0xFF0A1B31)
private val SteelBlue = Color(0xFF174568)
private val Cyan = Color(0xFF22D3EE)
private val IceBlue = Color(0xFFBAF3FF)
private val SuccessGreen = Color(0xFF22C77A)
private val MutedText = Color(0xFF8FB6CA)

private data class ParticleSeed(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float
)

private val particleSeeds = List(24) { index ->
    ParticleSeed(
        x = ((index * 37) % 101) / 100f,
        y = ((index * 61 + 13) % 103) / 102f,
        radius = 0.7f + (index % 4) * 0.45f,
        speed = 0.025f + (index % 5) * 0.008f,
        phase = (index * 0.73f) % (2f * PI.toFloat())
    )
}

/**
 * Intro de producto dibujada completamente con Jetpack Compose.
 * No utiliza video, Lottie ni recursos rasterizados.
 */
@Composable
fun InventoryCodeSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeline = remember { Animatable(0f) }
    val latestOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        timeline.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = SplashTimeline.DURATION_MILLIS,
                easing = LinearEasing
            )
        )
        latestOnFinished()
    }

    val progress = timeline.value
    val bracketProgress = easedSegment(progress, 0.03f, 0.18f)
    val palletProgress = easedSegment(progress, 0.10f, 0.32f)
    val scanProgress = SplashTimeline.segment(progress, 0.31f, 0.58f)
    val validationProgress = easedSegment(progress, 0.56f, 0.69f)
    val morphProgress = easedSegment(progress, 0.67f, 0.80f)
    val textProgress = easedSegment(progress, 0.76f, 0.90f)
    val accessibilityLabel = stringResource(R.string.splash_accessibility_label)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DeepNavy, Navy, DeepNavy)
                )
            )
            .semantics { contentDescription = accessibilityLabel }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameWidth = min(size.width * 0.72f, size.height * 0.39f)
            val frameHeight = frameWidth * 0.86f
            val center = Offset(size.width / 2f, size.height * 0.40f)
            val frameScale = 1f - (morphProgress * 0.16f)

            drawIndustrialBackground(
                timelineProgress = progress,
                finalProgress = morphProgress
            )

            drawScannerBrackets(
                center = center,
                width = frameWidth * frameScale,
                height = frameHeight * frameScale,
                progress = bracketProgress,
                alpha = 1f
            )

            if (palletProgress > 0f && morphProgress < 1f) {
                scale(
                    scale = 0.72f + (0.28f * palletProgress),
                    pivot = center
                ) {
                    drawIsometricPallet(
                        center = center,
                        width = frameWidth * 0.54f,
                        alpha = palletProgress * (1f - morphProgress),
                        scanProgress = scanProgress
                    )
                }
            }

            if (scanProgress > 0f && scanProgress < 1f && morphProgress < 0.85f) {
                drawScanBeam(
                    center = center,
                    frameWidth = frameWidth,
                    frameHeight = frameHeight,
                    progress = scanProgress,
                    alpha = 1f - morphProgress
                )
            }

            if (validationProgress > 0f && morphProgress < 1f) {
                drawValidationBadge(
                    center = Offset(
                        x = center.x + frameWidth * 0.32f,
                        y = center.y + frameHeight * 0.30f
                    ),
                    progress = validationProgress,
                    alpha = 1f - morphProgress
                )
            }

            if (morphProgress > 0f) {
                drawFinalMark(
                    center = center,
                    width = frameWidth * 0.46f,
                    progress = morphProgress
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .offset(
                    y = (maxHeight * 0.63f) + (12f * (1f - textProgress)).dp
                )
                .alpha(textProgress)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.splash_brand_name),
                color = Cyan,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_product_name),
                color = Color.White,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_tagline),
                color = MutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.15.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun easedSegment(value: Float, start: Float, end: Float): Float =
    FastOutSlowInEasing.transform(SplashTimeline.segment(value, start, end))

private fun DrawScope.drawIndustrialBackground(
    timelineProgress: Float,
    finalProgress: Float
) {
    val sceneAlpha = 1f - (finalProgress * 0.72f)
    val lineWidth = 1.dp.toPx()

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(
                Cyan.copy(alpha = 0.10f),
                SteelBlue.copy(alpha = 0.035f),
                Color.Transparent
            ),
            center = Offset(size.width / 2f, size.height * 0.42f),
            radius = size.width * 0.72f
        )
    )

    val shelfColor = IceBlue.copy(alpha = 0.045f * sceneAlpha)
    val shelfTop = size.height * 0.08f
    val shelfHeight = size.height * 0.48f
    val shelfWidth = size.width * 0.24f

    drawRect(
        color = shelfColor,
        topLeft = Offset(size.width * 0.02f, shelfTop),
        size = Size(shelfWidth, shelfHeight),
        style = Stroke(lineWidth)
    )
    drawRect(
        color = shelfColor,
        topLeft = Offset(size.width * 0.74f, shelfTop),
        size = Size(shelfWidth, shelfHeight),
        style = Stroke(lineWidth)
    )

    repeat(5) { index ->
        val y = shelfTop + shelfHeight * ((index + 1f) / 6f)
        drawLine(
            color = shelfColor,
            start = Offset(size.width * 0.02f, y),
            end = Offset(size.width * 0.26f, y),
            strokeWidth = lineWidth
        )
        drawLine(
            color = shelfColor,
            start = Offset(size.width * 0.74f, y),
            end = Offset(size.width * 0.98f, y),
            strokeWidth = lineWidth
        )
    }

    val horizon = Offset(size.width / 2f, size.height * 0.53f)
    val gridColor = Cyan.copy(alpha = 0.035f * sceneAlpha)
    for (index in -5..5) {
        val bottomX = size.width / 2f + (index * size.width * 0.15f)
        drawLine(
            color = gridColor,
            start = horizon,
            end = Offset(bottomX, size.height),
            strokeWidth = lineWidth
        )
    }
    repeat(7) { index ->
        val fraction = (index + 1f) / 8f
        val curvedFraction = fraction * fraction
        val y = horizon.y + ((size.height - horizon.y) * curvedFraction)
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = lineWidth
        )
    }

    particleSeeds.forEach { particle ->
        val normalizedY = positiveModulo(
            particle.y - timelineProgress * particle.speed,
            1f
        )
        val shimmer = 0.45f + 0.55f *
            ((sin(particle.phase + timelineProgress * PI.toFloat() * 3f) + 1f) / 2f)
        drawCircle(
            color = Cyan.copy(alpha = 0.12f * shimmer * sceneAlpha),
            radius = particle.radius.dp.toPx(),
            center = Offset(particle.x * size.width, normalizedY * size.height)
        )
    }
}

private fun DrawScope.drawScannerBrackets(
    center: Offset,
    width: Float,
    height: Float,
    progress: Float,
    alpha: Float
) {
    if (progress <= 0f || alpha <= 0f) return

    val left = center.x - width / 2f
    val right = center.x + width / 2f
    val top = center.y - height / 2f
    val bottom = center.y + height / 2f
    val cornerLength = width * 0.18f * progress
    val stroke = 2.2.dp.toPx()

    fun glowingLine(start: Offset, end: Offset) {
        drawLine(
            color = Cyan.copy(alpha = 0.12f * alpha),
            start = start,
            end = end,
            strokeWidth = stroke * 5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = IceBlue.copy(alpha = 0.95f * alpha),
            start = start,
            end = end,
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }

    glowingLine(Offset(left, top), Offset(left + cornerLength, top))
    glowingLine(Offset(left, top), Offset(left, top + cornerLength))
    glowingLine(Offset(right, top), Offset(right - cornerLength, top))
    glowingLine(Offset(right, top), Offset(right, top + cornerLength))
    glowingLine(Offset(left, bottom), Offset(left + cornerLength, bottom))
    glowingLine(Offset(left, bottom), Offset(left, bottom - cornerLength))
    glowingLine(Offset(right, bottom), Offset(right - cornerLength, bottom))
    glowingLine(Offset(right, bottom), Offset(right, bottom - cornerLength))
}

private fun DrawScope.drawIsometricPallet(
    center: Offset,
    width: Float,
    alpha: Float,
    scanProgress: Float
) {
    if (alpha <= 0f) return

    val halfWidth = width / 2f
    val depth = width * 0.30f
    val bodyHeight = width * 0.42f
    val topY = center.y - bodyHeight / 2f

    val top = Offset(center.x, topY - depth / 2f)
    val right = Offset(center.x + halfWidth, topY)
    val front = Offset(center.x, topY + depth / 2f)
    val left = Offset(center.x - halfWidth, topY)
    val bottomRight = Offset(right.x, right.y + bodyHeight)
    val bottomFront = Offset(front.x, front.y + bodyHeight)
    val bottomLeft = Offset(left.x, left.y + bodyHeight)

    val topFace = polygonPath(top, right, front, left)
    val leftFace = polygonPath(left, front, bottomFront, bottomLeft)
    val rightFace = polygonPath(right, bottomRight, bottomFront, front)
    val edgeColor = IceBlue.copy(alpha = 0.38f * alpha)
    val gridColor = IceBlue.copy(alpha = 0.20f * alpha)
    val stroke = 1.dp.toPx()

    drawPath(topFace, color = Color(0xFF2D7396).copy(alpha = 0.92f * alpha))
    drawPath(leftFace, color = Color(0xFF143F64).copy(alpha = 0.96f * alpha))
    drawPath(rightFace, color = Color(0xFF0F3151).copy(alpha = 0.98f * alpha))
    drawPath(topFace, color = edgeColor, style = Stroke(stroke))
    drawPath(leftFace, color = edgeColor, style = Stroke(stroke))
    drawPath(rightFace, color = edgeColor, style = Stroke(stroke))

    listOf(1f / 3f, 2f / 3f).forEach { fraction ->
        drawLine(
            color = gridColor,
            start = lerpOffset(top, right, fraction),
            end = lerpOffset(left, front, fraction),
            strokeWidth = stroke
        )
        drawLine(
            color = gridColor,
            start = lerpOffset(top, left, fraction),
            end = lerpOffset(right, front, fraction),
            strokeWidth = stroke
        )
        drawLine(
            color = gridColor,
            start = lerpOffset(left, front, fraction),
            end = lerpOffset(bottomLeft, bottomFront, fraction),
            strokeWidth = stroke
        )
        drawLine(
            color = gridColor,
            start = lerpOffset(right, front, fraction),
            end = lerpOffset(bottomRight, bottomFront, fraction),
            strokeWidth = stroke
        )
        drawLine(
            color = gridColor,
            start = lerpOffset(left, bottomLeft, fraction),
            end = lerpOffset(front, bottomFront, fraction),
            strokeWidth = stroke
        )
        drawLine(
            color = gridColor,
            start = lerpOffset(right, bottomRight, fraction),
            end = lerpOffset(front, bottomFront, fraction),
            strokeWidth = stroke
        )
    }

    val palletBack = Offset(center.x, bottomFront.y + width * 0.02f)
    val palletRight = Offset(center.x + width * 0.59f, bottomFront.y + width * 0.09f)
    val palletFront = Offset(center.x, bottomFront.y + width * 0.18f)
    val palletLeft = Offset(center.x - width * 0.59f, bottomFront.y + width * 0.09f)
    val palletTop = polygonPath(palletBack, palletRight, palletFront, palletLeft)
    drawPath(
        palletTop,
        color = Color(0xFF537084).copy(alpha = 0.95f * alpha)
    )
    drawPath(
        palletTop,
        color = IceBlue.copy(alpha = 0.45f * alpha),
        style = Stroke(stroke)
    )
    drawLine(
        color = DeepNavy.copy(alpha = 0.9f * alpha),
        start = lerpOffset(palletLeft, palletFront, 0.42f),
        end = lerpOffset(palletBack, palletRight, 0.42f),
        strokeWidth = 3.dp.toPx()
    )
    drawLine(
        color = DeepNavy.copy(alpha = 0.9f * alpha),
        start = lerpOffset(palletLeft, palletFront, 0.74f),
        end = lerpOffset(palletBack, palletRight, 0.74f),
        strokeWidth = 3.dp.toPx()
    )

    val markers = listOf(
        Offset(center.x - width * 0.31f, center.y - width * 0.02f) to 0.28f,
        Offset(center.x + width * 0.22f, center.y + width * 0.02f) to 0.42f,
        Offset(center.x - width * 0.17f, center.y + width * 0.18f) to 0.66f,
        Offset(center.x + width * 0.34f, center.y + width * 0.22f) to 0.78f
    )
    markers.forEach { (position, activationPoint) ->
        val active = easedSegment(scanProgress, activationPoint, activationPoint + 0.12f)
        val markerSize = 6.dp.toPx()
        drawRoundRect(
            color = IceBlue.copy(alpha = (0.22f + 0.65f * active) * alpha),
            topLeft = Offset(position.x - markerSize, position.y - markerSize * 0.65f),
            size = Size(markerSize * 2f, markerSize * 1.3f),
            cornerRadius = CornerRadius(markerSize * 0.22f)
        )
        if (active > 0f) {
            drawCircle(
                color = SuccessGreen.copy(alpha = active * alpha),
                radius = 2.dp.toPx() * active,
                center = Offset(position.x + markerSize * 0.62f, position.y)
            )
        }
    }
}

private fun DrawScope.drawScanBeam(
    center: Offset,
    frameWidth: Float,
    frameHeight: Float,
    progress: Float,
    alpha: Float
) {
    val fadeIn = SplashTimeline.segment(progress, 0f, 0.08f)
    val fadeOut = 1f - SplashTimeline.segment(progress, 0.88f, 1f)
    val beamAlpha = fadeIn * fadeOut * alpha
    val startY = center.y - frameHeight * 0.34f
    val endY = center.y + frameHeight * 0.34f
    val y = startY + ((endY - startY) * progress)
    val start = Offset(center.x - frameWidth * 0.43f, y)
    val end = Offset(center.x + frameWidth * 0.43f, y)

    drawLine(
        color = Cyan.copy(alpha = 0.10f * beamAlpha),
        start = start,
        end = end,
        strokeWidth = 18.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Cyan.copy(alpha = 0.32f * beamAlpha),
        start = start,
        end = end,
        strokeWidth = 7.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White.copy(alpha = 0.95f * beamAlpha),
        start = start,
        end = end,
        strokeWidth = 1.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    repeat(7) { index ->
        val x = start.x + frameWidth * (0.08f + index * 0.12f)
        val drift = sin(progress * 14f + index) * 4.dp.toPx()
        drawCircle(
            color = Cyan.copy(alpha = 0.35f * beamAlpha),
            radius = (1f + (index % 3) * 0.45f).dp.toPx(),
            center = Offset(x, y + drift)
        )
    }
}

private fun DrawScope.drawValidationBadge(
    center: Offset,
    progress: Float,
    alpha: Float
) {
    if (progress <= 0f || alpha <= 0f) return

    val radius = 17.dp.toPx() * progress
    drawCircle(
        color = SuccessGreen.copy(alpha = 0.13f * progress * alpha),
        radius = radius * 1.75f,
        center = center
    )
    drawCircle(
        color = SuccessGreen.copy(alpha = 0.96f * alpha),
        radius = radius,
        center = center
    )

    val checkProgress = easedSegment(progress, 0.28f, 1f)
    val first = Offset(center.x - radius * 0.46f, center.y)
    val middle = Offset(center.x - radius * 0.10f, center.y + radius * 0.34f)
    val end = Offset(center.x + radius * 0.52f, center.y - radius * 0.38f)
    val firstPart = SplashTimeline.segment(checkProgress, 0f, 0.45f)
    val secondPart = SplashTimeline.segment(checkProgress, 0.45f, 1f)

    drawLine(
        color = Color.White.copy(alpha = alpha),
        start = first,
        end = lerpOffset(first, middle, firstPart),
        strokeWidth = 2.4.dp.toPx(),
        cap = StrokeCap.Round
    )
    if (secondPart > 0f) {
        drawLine(
            color = Color.White.copy(alpha = alpha),
            start = middle,
            end = lerpOffset(middle, end, secondPart),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawFinalMark(
    center: Offset,
    width: Float,
    progress: Float
) {
    if (progress <= 0f) return

    scale(
        scale = 0.88f + 0.12f * progress,
        pivot = center
    ) {
        val halfWidth = width / 2f
        val depth = width * 0.30f
        val bodyHeight = width * 0.42f
        val topY = center.y - bodyHeight / 2f
        val top = Offset(center.x, topY - depth / 2f)
        val right = Offset(center.x + halfWidth, topY)
        val front = Offset(center.x, topY + depth / 2f)
        val left = Offset(center.x - halfWidth, topY)
        val bottomRight = Offset(right.x, right.y + bodyHeight)
        val bottomFront = Offset(front.x, front.y + bodyHeight)
        val bottomLeft = Offset(left.x, left.y + bodyHeight)
        val stroke = 2.dp.toPx()

        val topFace = polygonPath(top, right, front, left)
        val leftFace = polygonPath(left, front, bottomFront, bottomLeft)
        val rightFace = polygonPath(right, bottomRight, bottomFront, front)

        drawPath(
            topFace,
            color = Color(0xFF46D99A).copy(alpha = progress)
        )
        drawPath(
            leftFace,
            color = Color(0xFF14975F).copy(alpha = progress)
        )
        drawPath(
            rightFace,
            color = Color(0xFF0A7348).copy(alpha = progress)
        )
        drawPath(
            topFace,
            color = Color.White.copy(alpha = 0.92f * progress),
            style = Stroke(stroke)
        )
        drawPath(
            leftFace,
            color = Color.White.copy(alpha = 0.92f * progress),
            style = Stroke(stroke)
        )
        drawPath(
            rightFace,
            color = Color.White.copy(alpha = 0.92f * progress),
            style = Stroke(stroke)
        )

        drawLine(
            color = Color.White.copy(alpha = 0.82f * progress),
            start = lerpOffset(top, right, 0.52f),
            end = lerpOffset(left, front, 0.52f),
            strokeWidth = stroke
        )
        drawLine(
            color = Color.White.copy(alpha = 0.82f * progress),
            start = lerpOffset(top, left, 0.48f),
            end = lerpOffset(right, front, 0.48f),
            strokeWidth = stroke
        )

        val baseBack = Offset(center.x, bottomFront.y + width * 0.05f)
        val baseRight = Offset(center.x + width * 0.61f, bottomFront.y + width * 0.13f)
        val baseFront = Offset(center.x, bottomFront.y + width * 0.22f)
        val baseLeft = Offset(center.x - width * 0.61f, bottomFront.y + width * 0.13f)
        val basePath = polygonPath(baseBack, baseRight, baseFront, baseLeft)

        drawPath(
            basePath,
            color = SuccessGreen.copy(alpha = progress)
        )
        drawPath(
            basePath,
            color = Color.White.copy(alpha = 0.9f * progress),
            style = Stroke(stroke)
        )
    }
}

private fun polygonPath(
    first: Offset,
    second: Offset,
    third: Offset,
    fourth: Offset
): Path = Path().apply {
    moveTo(first.x, first.y)
    lineTo(second.x, second.y)
    lineTo(third.x, third.y)
    lineTo(fourth.x, fourth.y)
    close()
}

private fun lerpOffset(start: Offset, end: Offset, fraction: Float): Offset {
    val bounded = fraction.coerceIn(0f, 1f)
    return Offset(
        x = start.x + (end.x - start.x) * bounded,
        y = start.y + (end.y - start.y) * bounded
    )
}

private fun positiveModulo(value: Float, modulus: Float): Float =
    ((value % modulus) + modulus) % modulus
