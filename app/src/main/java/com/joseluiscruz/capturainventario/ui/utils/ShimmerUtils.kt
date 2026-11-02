package com.joseluiscruz.capturainventario.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * Modifier Shimmer Premium para efectos de carga esqueleto
 * Inspirado en Facebook, YouTube y LinkedIn
 *
 * @param shimmerColor Color del destello (por defecto blanco semi-transparente)
 * @param durationMillis Duración de la animación en milisegundos
 * @param angle Ángulo del gradiente en grados (0 = horizontal, 90 = vertical)
 */
fun Modifier.shimmer(
    shimmerColor: Color = Color.White.copy(alpha = 0.6f),
    durationMillis: Int = 1200,
    angle: Float = 45f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    
    // Calculamos el desplazamiento basado en el ángulo
    val xOffset by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
    
    // Convertimos el ángulo a radianes
    val angleRad = Math.toRadians(angle.toDouble()).toFloat()
    val offsetX = (xOffset * kotlin.math.cos(angleRad))
    val offsetY = (xOffset * kotlin.math.sin(angleRad))
    
    this.background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE0E0E0),
                Color(0xFFE0E0E0),
                shimmerColor,
                Color(0xFFE0E0E0),
                Color(0xFFE0E0E0)
            ),
            start = Offset(x = offsetX * 100, y = offsetY * 100),
            end = Offset(x = offsetX * 100 + 500, y = offsetY * 100 + 500)
        )
    )
}

/**
 * Modifier Shimmer Corporativo con colores PLANTA DE MANUFACTURA
 * Usa el rojo corporativo de forma sutil para branding durante la carga
 */
fun Modifier.shimmerDemo(
    durationMillis: Int = 1400,
    angle: Float = 35f
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerDemo")
    
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )
    
    this.background(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE8E8E8),
                Color(0xFFF5F5F5),
                BrandColors.BrandRed.copy(alpha = 0.08f),
                Color(0xFFF5F5F5),
                Color(0xFFE8E8E8)
            ),
            startX = 0f,
            endX = 1000f * progress
        )
    )
}

// ==================== COMPONENTES ESQUELETO ====================

/**
 * Skeleton Box - Para imágenes o contenedores cuadrados/rectangulares
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
    isLoading: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (isLoading) Modifier.shimmerDemo() else Modifier.background(Color.Transparent)
            )
    )
}

/**
 * Skeleton Text - Para líneas de texto
 *
 * @param lines Número de líneas a mostrar
 * @param lineHeight Altura de cada línea
 * @param lastLineWidth Porcentaje del ancho de la última línea (0f = mismo ancho)
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    lines: Int = 1,
    lineHeight: Dp = 16.dp,
    lastLineWidth: Float = 1f,
    isLoading: Boolean = true
) {
    Column(modifier = modifier) {
        repeat(lines) { index ->
            val widthFraction = if (index == lines - 1 && lastLineWidth < 1f) lastLineWidth else 1f
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(lineHeight)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .then(
                        if (isLoading) Modifier.shimmerDemo() 
                        else Modifier.background(Color(0xFFE0E0E0))
                    )
            )
            if (index < lines - 1) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * Skeleton Card - Para tarjetas completas de productos/pallets
 * Ideal para listas de inventario
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    showImage: Boolean = true,
    showTitle: Boolean = true,
    showSubtitle: Boolean = true,
    showActions: Boolean = false,
    isLoading: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Imagen esqueleto
        if (showImage) {
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                isLoading = isLoading
            )
        }
        
        // Título esqueleto
        if (showTitle) {
            SkeletonText(
                lines = 1,
                lineHeight = 20.dp,
                lastLineWidth = 0.7f,
                isLoading = isLoading
            )
        }
        
        // Subtítulo/esqueleto de descripción
        if (showSubtitle) {
            SkeletonText(
                lines = 2,
                lineHeight = 14.dp,
                lastLineWidth = 0.5f,
                isLoading = isLoading
            )
        }
        
        // Acciones esqueleto
        if (showActions) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    isLoading = isLoading
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    isLoading = isLoading
                )
            }
        }
    }
}

/**
 * Skeleton List Item - Para items de lista de inventario
 * Diseño compacto con thumbnail
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier,
    showThumbnail: Boolean = true,
    isLoading: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        if (showThumbnail) {
            SkeletonBox(
                modifier = Modifier
                    .size(56.dp),
                isLoading = isLoading
            )
        }
        
        // Contenido de texto
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SkeletonText(
                lines = 1,
                lineHeight = 16.dp,
                lastLineWidth = 0.8f,
                isLoading = isLoading
            )
            SkeletonText(
                lines = 1,
                lineHeight = 12.dp,
                lastLineWidth = 0.5f,
                isLoading = isLoading
            )
        }
        
        // Precio/cantidad
        SkeletonBox(
            modifier = Modifier
                .width(60.dp)
                .height(24.dp),
            isLoading = isLoading
        )
    }
}

/**
 * Skeleton Dashboard - Para métricas y tarjetas de dashboard
 */
@Composable
fun SkeletonDashboardCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono/título
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SkeletonBox(
                modifier = Modifier.size(40.dp),
                isLoading = isLoading
            )
            SkeletonBox(
                modifier = Modifier.size(24.dp),
                isLoading = isLoading
            )
        }
        
        // Valor principal (número grande)
        SkeletonText(
            lines = 1,
            lineHeight = 32.dp,
            lastLineWidth = 0.6f,
            isLoading = isLoading
        )
        
        // Descripción
        SkeletonText(
            lines = 1,
            lineHeight = 12.dp,
            lastLineWidth = 0.8f,
            isLoading = isLoading
        )
    }
}

/**
 * Skeleton Table Row - Para filas de tabla de datos
 */
@Composable
fun SkeletonTableRow(
    modifier: Modifier = Modifier,
    columns: Int = 4,
    isLoading: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(columns) { index ->
            val weight = when (index) {
                0 -> 3f
                columns - 1 -> 1f
                else -> 2f
            }
            SkeletonBox(
                modifier = Modifier
                    .weight(weight)
                    .height(20.dp),
                isLoading = isLoading
            )
        }
    }
}
