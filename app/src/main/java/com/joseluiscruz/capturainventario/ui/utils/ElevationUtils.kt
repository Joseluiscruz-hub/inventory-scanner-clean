package com.joseluiscruz.capturainventario.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * Modifier para sombra con color personalizado
 */
fun Modifier.coloredShadow(
    shadowColor: Color = BrandColors.BrandDarkRed.copy(alpha = 0.3f),
    blurRadius: Dp = 16.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 8.dp
): Modifier = this.drawWithContent {
    drawContent()
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(16.dp.toPx()),
        alpha = 0.5f
    )
}

/**
 * Sombra Neon
 */
fun Modifier.neonShadow(
    color: Color = BrandColors.BrandRed,
    blurRadius: Dp = 20.dp,
    spread: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    repeat(3) { index ->
        val alpha = 0.3f - (index * 0.1f)
        val spreadPx = spread.toPx() * (index + 1)
        drawRoundRect(
            color = color.copy(alpha = alpha),
            topLeft = Offset(-spreadPx / 2, -spreadPx / 2),
            size = Size(size.width + spreadPx, size.height + spreadPx),
            cornerRadius = CornerRadius(12.dp.toPx()),
            alpha = alpha
        )
    }
}

/**
 * Sombra Corporativa PLANTA DE MANUFACTURA
 */
fun Modifier.demoShadow(
    elevation: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    val elevationPx = elevation.toPx()
    val shadowColor = BrandColors.BrandDarkRed
    
    drawRoundRect(
        color = shadowColor.copy(alpha = 0.15f),
        topLeft = Offset(0f, elevationPx * 0.3f),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(16.dp.toPx()),
        alpha = 0.4f
    )
    
    drawRoundRect(
        color = shadowColor.copy(alpha = 0.08f),
        topLeft = Offset(0f, elevationPx * 0.6f),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(16.dp.toPx()),
        alpha = 0.25f
    )
}

/**
 * Sombra con Gradiente
 */
fun Modifier.gradientShadow(
    colors: List<Color> = listOf(
        BrandColors.BrandRed.copy(alpha = 0.3f),
        BrandColors.BrandDarkRed.copy(alpha = 0.15f),
        Color.Transparent
    ),
    blurRadius: Dp = 20.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 8.dp
): Modifier = this.drawWithContent {
    drawContent()
    drawRoundRect(
        brush = Brush.verticalGradient(colors = colors),
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = Size(size.width, size.height),
        cornerRadius = CornerRadius(16.dp.toPx()),
        alpha = 0.5f
    )
}

// ==================== TARJETAS PREMIUM ====================

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    containerColor: Color = Color.White,
    contentColor: Color = BrandColors.BrandBlack,
    border: Boolean = false,
    borderColor: Color = BrandColors.BrandRed.copy(alpha = 0.1f),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.then(
            if (border) {
                Modifier.border(
                    width = 1.dp,
                    color = borderColor,
                    shape = shape
                )
            } else {
                Modifier
            }
        ).demoShadow(elevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun ElevatedPremiumCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 6.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    containerColor: Color = Color.White,
    contentColor: Color = BrandColors.BrandBlack,
    glowColor: Color = BrandColors.BrandRed,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.neonShadow(color = glowColor, blurRadius = elevation * 2),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun GlassShadowCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 8.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .demoShadow(elevation = elevation),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun OutlinedPremiumCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    gradientColors: List<Color> = listOf(
        BrandColors.BrandRed,
        BrandColors.BrandDarkRed,
        BrandColors.BrandRed
    ),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.border(
            width = 2.dp,
            brush = Brush.linearGradient(colors = gradientColors),
            shape = shape
        ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun DynamicElevationCard(
    modifier: Modifier = Modifier,
    isPressed: Boolean = false,
    isHovered: Boolean = false,
    baseElevation: Dp = 4.dp,
    pressedElevation: Dp = 2.dp,
    hoveredElevation: Dp = 8.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val currentElevation = when {
        isPressed -> pressedElevation
        isHovered -> hoveredElevation
        else -> baseElevation
    }

    val animatedElevation = androidx.compose.animation.core.animateDpAsState(
        targetValue = currentElevation,
        animationSpec = androidx.compose.animation.core.tween(150),
        label = "elevation"
    ).value

    Card(
        modifier = modifier.demoShadow(elevation = animatedElevation),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = shape
    ) {
        content()
    }
}

fun Modifier.fabShadow(
    color: Color = BrandColors.BrandDarkRed.copy(alpha = 0.4f),
    elevation: Dp = 8.dp
): Modifier = this.drawWithContent {
    drawContent()
    val elevationPx = elevation.toPx()
    
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = size.minDimension / 2 + elevationPx * 0.5f,
        center = Offset(center.x, center.y + elevationPx * 0.3f),
        alpha = 0.5f
    )
}

fun Modifier.appBarShadow(
    color: Color = BrandColors.BrandDarkRed.copy(alpha = 0.15f),
    elevation: Dp = 4.dp
): Modifier = this.drawWithContent {
    drawContent()
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                color.copy(alpha = 0.05f),
                Color.Transparent
            ),
            startY = 0f,
            endY = elevation.toPx() * 2
        ),
        alpha = 0.5f
    )
}

fun Modifier.scrollShadow(
    color: Color = BrandColors.BrandDarkRed.copy(alpha = 0.1f),
    height: Dp = 8.dp,
    position: ShadowPosition = ShadowPosition.Top
): Modifier = this.drawWithContent {
    drawContent()
    val startY = if (position == ShadowPosition.Top) 0f else size.height - height.toPx()
    
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                Color.Transparent
            ),
            startY = startY,
            endY = if (position == ShadowPosition.Top) height.toPx() else size.height
        ),
        alpha = 0.5f
    )
}

enum class ShadowPosition { Top, Bottom }
