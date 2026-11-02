package com.joseluiscruz.capturainventario.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * Glassmorphism - Efecto Cristal Desenfocado Premium
 */
@Composable
fun Modifier.glassmorphism(
    backgroundColor: Color = Color.White.copy(alpha = 0.15f),
    blurRadius: Dp = 20.dp,
    borderColor: Color = Color.White.copy(alpha = 0.2f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 16.dp
): Modifier = this
    .blur(blurRadius)
    .drawBehind {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            alpha = 0.7f
        )
    }
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = 0.5f),
                Color.Transparent,
                borderColor.copy(alpha = 0.3f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Glassmorphism Corporativo PLANTA DE MANUFACTURA
 */
@Composable
fun Modifier.glassmorphismDemo(
    blurRadius: Dp = 24.dp,
    cornerRadius: Dp = 20.dp,
    tintRed: Float = 0.08f
): Modifier = this
    .blur(blurRadius)
    .drawBehind {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.2f),
                    BrandColors.BrandRed.copy(alpha = tintRed),
                    Color.White.copy(alpha = 0.15f)
                )
            ),
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
    }
    .border(
        width = 1.5.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.4f),
                BrandColors.BrandRed.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.3f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Glassmorphism Dark
 */
@Composable
fun Modifier.glassmorphismDark(
    backgroundColor: Color = Color.Black.copy(alpha = 0.4f),
    blurRadius: Dp = 30.dp,
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 24.dp
): Modifier = this
    .blur(blurRadius)
    .drawBehind {
        drawRoundRect(
            color = backgroundColor,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            alpha = 0.8f
        )
    }
    .border(
        width = borderWidth,
        brush = Brush.linearGradient(
            colors = listOf(
                borderColor,
                borderColor.copy(alpha = 0.3f),
                Color.Transparent
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )
    .clip(RoundedCornerShape(cornerRadius))

/**
 * Glass Card
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.15f),
    blurRadius: Dp = 20.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .glassmorphism(
                backgroundColor = backgroundColor,
                blurRadius = blurRadius,
                cornerRadius = cornerRadius
            )
    ) {
        content()
    }
}

/**
 * Glass Dialog Container
 */
@Composable
fun GlassDialogContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .blur(24.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            BrandColors.BrandRed.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.2f)
                        )
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        BrandColors.BrandRed.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
    ) {
        content()
    }
}

/**
 * Glass Bottom Sheet
 */
@Composable
fun GlassBottomSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .blur(20.dp)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        content()
    }
}

/**
 * Glass AppBar
 */
@Composable
fun GlassAppBar(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .blur(16.dp)
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    alpha = 0.9f
                )
            }
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

/**
 * Glass Floating Button
 */
@Composable
fun GlassFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .blur(12.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            BrandColors.BrandRed.copy(alpha = 0.3f),
                            BrandColors.BrandRed.copy(alpha = 0.1f)
                        )
                    )
                )
            }
            .border(
                width = 2.dp,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.5f),
                        BrandColors.BrandRed.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp))
            .clickable { onClick() }
    ) {
        content()
    }
}

/**
 * Blur Overlay
 */
@Composable
fun BlurOverlay(
    modifier: Modifier = Modifier,
    blurRadius: Dp = 8.dp,
    overlayColor: Color = Color.Black.copy(alpha = 0.3f)
) {
    Box(
        modifier = modifier
            .blur(blurRadius)
            .background(overlayColor)
    )
}

/**
 * Gradient Border
 */
fun Modifier.gradientBorder(
    width: Dp = 2.dp,
    cornerRadius: Dp = 12.dp,
    colors: List<Color> = listOf(
        BrandColors.BrandRed,
        BrandColors.BrandDarkRed,
        BrandColors.BrandRed
    )
): Modifier = this.border(
    width = width,
    brush = Brush.linearGradient(colors = colors),
    shape = RoundedCornerShape(cornerRadius)
)
