package com.joseluiscruz.capturainventario.ui.comparacion

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.viewmodel.EstadisticasSesion

/**
 * GraficaDonaPremium - Dona de auditoría con efecto Glassmorphism.
 * 
 * Componente visualmente impactante que muestra el estado del inventario
 * en una dona dividida en tres segmentos:
 * - Verde: Coincidencias (inventario limpio)
 * - Naranja: Faltantes
 * - Rojo: Excesos
 * 
 * El centro tiene un efecto glassmorphism con el porcentaje de efectividad.
 * 
 * @param estadisticas Las estadísticas del inventario
 * @param modifier Modificador para personalizar layout
 */
@Composable
fun GraficaDonaPremium(
    estadisticas: EstadisticasSesion,
    modifier: Modifier = Modifier
) {
    // Calcular los ángulos para cada segmento
    val total = estadisticas.totalSKUs.coerceAtLeast(1) // Evitar división por 0
    val verde = (estadisticas.coincidencias.toFloat() / total) * 360f
    val naranja = (estadisticas.faltantes.toFloat() / total) * 360f
    val rojo = (estadisticas.excesos.toFloat() / total) * 360f
    
    // Animación de progreso
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            1f,
            tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            )
        )
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(180.dp)
    ) {
        // Capa de fondo blur para glassmorphism
        Box(
            modifier = Modifier
                .size(160.dp)
                .blur(20.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandColors.SuccessGreen.copy(alpha = 0.3f),
                            BrandColors.WarningAmber.copy(alpha = 0.2f),
                            BrandColors.BrandRed.copy(alpha = 0.2f)
                        )
                    ),
                    RoundedCornerShape(50.dp)
                )
        )
        
        // Canvas para la dona
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 28.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            
            // Centro del canvas
            val center = Offset(size.width / 2, size.height / 2)
            
            // Dibujar pista de fondo (gris)
            drawCircle(
                color = BrandColors.Surface3,
                radius = radius,
                center = center,
                style = Stroke(strokeWidth)
            )
            
            // Ángulo de inicio (-90 grados = arriba)
            val startAngle = -90f
            
            // Dibujar segmento verde (coincidencias)
            if (verde > 0) {
                drawArc(
                    color = BrandColors.SuccessGreen,
                    startAngle = startAngle,
                    sweepAngle = verde * progressAnim.value,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Dibujar segmento naranja (faltantes)
            if (naranja > 0) {
                drawArc(
                    color = BrandColors.WarningAmber,
                    startAngle = startAngle + (verde * progressAnim.value),
                    sweepAngle = naranja * progressAnim.value,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Dibujar segmento rojo (excesos)
            if (rojo > 0) {
                drawArc(
                    color = BrandColors.BrandRed,
                    startAngle = startAngle + ((verde + naranja) * progressAnim.value),
                    sweepAngle = rojo * progressAnim.value,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        // Centro con Glassmorphism
        Card(
            colors = CardDefaults.cardColors(
                containerColor = BrandColors.Surface1.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .size(100.dp)
                .border(
                    1.dp,
                    BrandColors.BrandWhite.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "${estadisticas.efectividad}%",
                    color = BrandColors.BrandWhite,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Limpio",
                    color = BrandColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * FiltrosRapidosChips - Chips de filtro rápido en píldoras.
 * 
 * Permite filtrar la lista de diferencias por estado con un solo toque.
 * Diseño en píldoras con scroll horizontal para mejor UX en pantallas pequeñas.
 * 
 * @param filtroActual El filtro actualmente seleccionado (null = todos)
 * @param onFiltroSeleccionado Callback cuando se selecciona un filtro
 * @param modifier Modificador para personalizar layout
 */
@Composable
fun FiltrosRapidosChips(
    filtroActual: EstadoDiferencia?,
    onFiltroSeleccionado: (EstadoDiferencia?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FiltroChipPremium(
            texto = "Todos",
            seleccionado = filtroActual == null,
            colorAcento = BrandColors.BrandWhite,
            onClick = { onFiltroSeleccionado(null) }
        )
        
        FiltroChipPremium(
            texto = "Excesos 🔴",
            seleccionado = filtroActual == EstadoDiferencia.EXCESO,
            colorAcento = BrandColors.BrandRed,
            onClick = { onFiltroSeleccionado(EstadoDiferencia.EXCESO) }
        )
        
        FiltroChipPremium(
            texto = "Faltantes 🟡",
            seleccionado = filtroActual == EstadoDiferencia.FALTANTE,
            colorAcento = BrandColors.WarningAmber,
            onClick = { onFiltroSeleccionado(EstadoDiferencia.FALTANTE) }
        )
        
        FiltroChipPremium(
            texto = "Completos 🟢",
            seleccionado = filtroActual == EstadoDiferencia.COINCIDE,
            colorAcento = BrandColors.SuccessGreen,
            onClick = { onFiltroSeleccionado(EstadoDiferencia.COINCIDE) }
        )
    }
}

/**
 * FiltroChipPremium - Chip individual para filtros.
 * 
 * @param texto El texto a mostrar en el chip
 * @param seleccionado Si el chip está seleccionado
 * @param colorAcento El color de acento cuando está seleccionado
 * @param onClick Callback cuando se hace click
 * @param modifier Modificador para personalizar layout
 */
@Composable
fun FiltroChipPremium(
    texto: String,
    seleccionado: Boolean,
    colorAcento: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (seleccionado) colorAcento.copy(alpha = 0.15f) else BrandColors.Surface1.copy(alpha = 0.6f),
        border = BorderStroke(
            1.5.dp,
            if (seleccionado) colorAcento else Color.Transparent
        ),
        shadowElevation = if (seleccionado) 8.dp else 4.dp,
        modifier = modifier.height(38.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Text(
                text = texto,
                color = if (seleccionado) colorAcento else BrandColors.TextSecondary,
                fontWeight = if (seleccionado) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * ResumenEstadisticasRow - Fila de estadísticas rápidas.
 * 
 * Muestra las estadísticas principales en una fila horizontal
 * con íconos y colores diferenciados.
 */
@Composable
fun ResumenEstadisticasRow(estadisticas: EstadisticasSesion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        EstadisticaItem(
            titulo = "Total",
            valor = estadisticas.totalSKUs.toString(),
            color = BrandColors.BrandWhite,
            icono = Icons.Default.Inventory
        )
        
        EstadisticaItem(
            titulo = "Coincidencias",
            valor = estadisticas.coincidencias.toString(),
            color = BrandColors.SuccessGreen,
            icono = Icons.Default.CheckCircle
        )
        
        EstadisticaItem(
            titulo = "Faltantes",
            valor = estadisticas.faltantes.toString(),
            color = BrandColors.WarningAmber,
            icono = Icons.Default.TrendingDown
        )
        
        EstadisticaItem(
            titulo = "Excesos",
            valor = estadisticas.excesos.toString(),
            color = BrandColors.BrandRed,
            icono = Icons.Default.TrendingUp
        )
    }
}

@Composable
fun EstadisticaItem(
    titulo: String,
    valor: String,
    color: Color,
    icono: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = valor,
            color = color,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = titulo,
            color = BrandColors.TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
