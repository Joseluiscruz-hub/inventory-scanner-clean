package com.joseluiscruz.capturainventario.ui.comparacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlinx.coroutines.delay

/**
 * Overlay de alerta para discrepancias críticas detectadas durante el escaneo.
 * 
 * Este componente muestra una alerta visual inmediata cuando se detecta un EXCESO o FALTANTE
 * significativo, permitiendo al operador corregir el error sin cambiar de pantalla.
 * 
 * Características:
 * - Animación de entrada/salida suave
 * - Colores diferenciados por tipo de alerta (EXCESO = Rojo, FALTANTE = Naranja)
 * - Muestra la diferencia numérica destacada
 * - Se auto-oculta después de un timeout configurable
 * 
 * @param ultimaDiferencia La última diferencia detectada (null si no hay alerta)
 * @param onDismiss Callback cuando el usuario cierra la alerta
 * @param autoDismissTime Tiempo en milisegundos para auto-ocultar (default 5000ms)
 */
@Composable
fun AlertaDiscrepanciaOverlay(
    ultimaDiferencia: DiferenciaInventario?,
    onDismiss: () -> Unit,
    autoDismissTime: Long = 5000L
) {
    val esDiscrepancia = ultimaDiferencia?.estado == EstadoDiferencia.EXCESO ||
        ultimaDiferencia?.estado == EstadoDiferencia.FALTANTE

    // Auto-dismiss timer
    LaunchedEffect(ultimaDiferencia) {
        if (esDiscrepancia) {
            delay(autoDismissTime)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = esDiscrepancia,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        ultimaDiferencia?.let { item ->
            val (bgColor, titulo, icono, subtitulo) = when (item.estado) {
                EstadoDiferencia.EXCESO -> Quadra(
                    BrandColors.BrandDarkRed,
                    "¡ALTO! EXCESO DETECTADO",
                    Icons.Default.Warning,
                    "Has escaneado más de lo permitido en SAP"
                )
                EstadoDiferencia.FALTANTE -> Quadra(
                    Color(0xFF8A5A00),
                    "FALTANTE REGISTRADO",
                    Icons.Default.TrendingDown,
                    "Cantidad escaneada es menor a la esperada"
                )
                EstadoDiferencia.COINCIDE -> return@AnimatedVisibility
                EstadoDiferencia.SIN_REFERENCIA -> return@AnimatedVisibility
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        icono,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Column(Modifier.weight(1f)) {
                        Text(
                            titulo,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Text(
                            subtitulo,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                        Text(
                            "SKU: ${item.codigo}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            if (item.diferencia > 0) "+${item.diferencia}" else "${item.diferencia}",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "SAP: ${item.cantidadReferencia}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo de alerta crítica para excesos.
 * 
 * A diferencia del overlay, este diálogo bloquea la UI y requiere
 * confirmación explícita del usuario. Útil para errores críticos
 * que deben ser atendidos inmediatamente.
 * 
 * @param diferencia La diferencia detectada
 * @param onConfirm Callback cuando el usuario confirma la acción
 * @param onDismiss Callback cuando el usuario cancela
 */
@Composable
fun AlertaCriticaDialog(
    diferencia: DiferenciaInventario,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    autoDismissTime: Long = 7000L
) {
    var remainingMillis by remember(diferencia, autoDismissTime) { mutableStateOf(autoDismissTime) }

    LaunchedEffect(diferencia, autoDismissTime) {
        remainingMillis = autoDismissTime
        while (remainingMillis > 0) {
            delay(1000L)
            remainingMillis = (remainingMillis - 1000L).coerceAtLeast(0L)
        }
        onDismiss()
    }

    val progress = if (autoDismissTime > 0L) {
        remainingMillis.toFloat() / autoDismissTime.toFloat()
    } else {
        0f
    }
    val remainingSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandColors.BrandDarkRed),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "¡EXCESO DETECTADO!",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    "Has escaneado ${diferencia.cantidadActual} unidades,\npero SAP solo registra ${diferencia.cantidadReferencia}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                
                Text(
                    "Diferencia: ${diferencia.diferencia} unidades",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(16.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = BrandColors.BrandWhite,
                    trackColor = BrandColors.BrandWhite.copy(alpha = 0.22f)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Se cerrará en ${remainingSeconds}s",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Revisar")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandColors.BrandWhite,
                            contentColor = BrandColors.BrandDarkRed
                        )
                    ) {
                        Text("Confirmar", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Helper data class para valores cuádruples.
 */
data class Quadra<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
