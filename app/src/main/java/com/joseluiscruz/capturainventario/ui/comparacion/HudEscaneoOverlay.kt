package com.joseluiscruz.capturainventario.ui.comparacion

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.AlertaTPM
import com.joseluiscruz.capturainventario.model.EstadoColor
import com.joseluiscruz.capturainventario.model.HudEscaneoState
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import kotlinx.coroutines.delay

@Composable
fun HudEscaneoOverlay(
    state: HudEscaneoState,
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 3500
) {
    val context = LocalContext.current
    val activeState = state as? HudEscaneoState.Activo
    var visible by remember(state) { mutableStateOf(activeState != null) }

    LaunchedEffect(state) {
        if (activeState == null) {
            visible = false
            return@LaunchedEffect
        }

        visible = true
        ejecutarHaptica(context, activeState.color)
        delay(autoHideMillis)
        visible = false
    }

    AnimatedVisibility(
        visible = visible && activeState != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring()
        ) + fadeOut()
    ) {
        activeState?.let { item ->
            val colors = colorsFor(item.color)
            val sinReferencia = item.color == EstadoColor.NEUTRO
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.background),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(colors.background, colors.background.copy(alpha = 0.90f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = colors.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = colors.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.sku,
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = item.descripcion,
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (sinReferencia) item.actual.toString() else formatoDelta(item.delta),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp
                                )
                                Text(
                                    text = if (sinReferencia) {
                                        "Referencia SAP no cargada"
                                    } else {
                                        "SAP ${item.esperado} · Esc ${item.actual}"
                                    },
                                    color = Color.White.copy(alpha = 0.80f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (sinReferencia) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricChip("Conteo ciego")
                                MetricChip("Capturado: ${item.actual}")
                            }
                        } else {
                            LinearProgressIndicator(
                                progress = { progressFor(item.delta) },
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.18f),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricChip("Esperado: ${item.esperado}")
                                MetricChip("Actual: ${item.actual}")
                                MetricChip("Delta: ${formatoDelta(item.delta)}")
                            }
                        }

                        item.alertaTPM?.let { alerta ->
                            TpmBadge(alerta)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChip(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TpmBadge(alerta: AlertaTPM) {
    val color = when {
        alerta.diasRestantes != null && alerta.diasRestantes < TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS -> BrandColors.BrandDarkRed
        else -> Color(0xFF8A5A00)
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = alerta.mensaje ?: "TPM en observación",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private data class HudPalette(
    val background: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String
)

private fun colorsFor(color: EstadoColor): HudPalette = when (color) {
    EstadoColor.NEUTRO -> HudPalette(
        background = Color(0xFF1565C0),
        icon = Icons.Default.Info,
        title = "CAPTURA SIN REFERENCIA SAP"
    )
    EstadoColor.VERDE -> HudPalette(
        background = Color(0xFF2E7D32),
        icon = Icons.Default.CheckCircle,
        title = "COINCIDENCIA PERFECTA"
    )
    EstadoColor.AMARILLO -> HudPalette(
        background = Color(0xFF8A5A00),
        icon = Icons.Default.Warning,
        title = "ALERTA DE DESVIACIÓN"
    )
    EstadoColor.ROJO -> HudPalette(
        background = Color(0xFFC62828),
        icon = Icons.Default.Error,
        title = "CRÍTICO"
    )
}

private fun formatoDelta(delta: Int): String = when {
    delta > 0 -> "+$delta"
    delta < 0 -> delta.toString()
    else -> "✓"
}

private fun progressFor(delta: Int): Float {
    val normalized = kotlin.math.abs(delta).coerceAtMost(10)
    return normalized / 10f
}

private fun ejecutarHaptica(context: android.content.Context, color: EstadoColor) {
    val vibrator = context.getSystemService(Vibrator::class.java)
    val duration = when (color) {
        EstadoColor.NEUTRO -> 35L
        EstadoColor.VERDE -> 35L
        EstadoColor.AMARILLO -> 75L
        EstadoColor.ROJO -> 140L
    }

    if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    } else {
        // Fallback visual/háptico si el dispositivo no expone Vibrator.
        @Suppress("DEPRECATION")
        try {
            val view = (context as? android.app.Activity)?.window?.decorView
            view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        } catch (_: Exception) {
            // Sin-op.
        }
    }
}



