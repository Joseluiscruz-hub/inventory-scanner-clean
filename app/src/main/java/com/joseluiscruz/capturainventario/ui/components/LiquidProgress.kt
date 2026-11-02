package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlin.math.sin

/**
 * Un indicador de progreso "líquido" que simula el llenado de un envase
 * con ondas animadas. Ideal para KPIs de inventario en PLANTA DE MANUFACTURA.
 *
 * @param progress Progreso actual (0.0 a 1.0)
 * @param label Etiqueta descriptiva
 */
@Composable
fun LiquidProgress(
    progress: Float,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "animatedProgress"
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 140.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val fillHeight = height * animatedProgress
                
                val path = Path().apply {
                    moveTo(0f, height - fillHeight)
                    
                    // Dibujamos la onda
                    val waveAmplitude = 10f
                    val waveCount = 1
                    for (x in 0..width.toInt()) {
                        val y = (height - fillHeight) + 
                                waveAmplitude * sin((x / width) * 2 * Math.PI + waveOffset)
                        lineTo(x.toFloat(), y.toFloat())
                    }
                    
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(color.copy(alpha = 0.8f), color),
                        startY = height - fillHeight - 20f,
                        endY = height
                    )
                )
                
                // Brillo del envase
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent, Color.White.copy(alpha = 0.05f))
                    )
                )
            }
            
            Text(
                "${(progress * 100).toInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
        
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
