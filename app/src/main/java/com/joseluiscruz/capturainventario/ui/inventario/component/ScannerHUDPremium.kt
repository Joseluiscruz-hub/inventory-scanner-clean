package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * ScannerHUDPremium - Dynamic Island para validación de escaneos.
 * 
 * Componente HUD (Heads-Up Display) que muestra información crítica
 * inmediatamente después de escanear un código, con colores diferenciados
 * según el estado de la validación contra SAP.
 * 
 * Características:
 * - Animación de entrada/salida suave tipo "Dynamic Island"
 * - Colores corporativos PLANTA DE MANUFACTURA
 * - Información clara y concisa para el operador
 * - Auto-ocultado configurable
 * 
 * @param diferencia La diferencia detectada (null para ocultar)
 * @param modifier Modificador para personalizar layout
 */
@Composable
fun ScannerHUDPremium(
    diferencia: DiferenciaInventario?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = diferencia != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = spring()),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring()
        ) + fadeOut()
    ) {
        diferencia?.let { item ->
            val (bgColor, icono, titulo, borderColor) = when (item.estado) {
                EstadoDiferencia.EXCESO -> Quadra(
                    Color(0xFFB71C1C).copy(alpha = 0.95f),
                    Icons.Default.Error,
                    "¡EXCESO DETECTADO!",
                    BrandColors.BrandRed
                )
                EstadoDiferencia.FALTANTE -> Quadra(
                    Color(0xFF8A5A00).copy(alpha = 0.95f),
                    Icons.Default.Warning,
                    "FALTANTE REGISTRADO",
                    BrandColors.WarningAmber
                )
                EstadoDiferencia.COINCIDE -> Quadra(
                    Color(0xFF2E7D32).copy(alpha = 0.95f),
                    Icons.Default.CheckCircle,
                    "COINCIDENCIA PERFECTA",
                    BrandColors.SuccessGreen
                )
                EstadoDiferencia.SIN_REFERENCIA -> Quadra(
                    Color(0xFF1565C0).copy(alpha = 0.95f),
                    Icons.Default.Info,
                    "CAPTURA REGISTRADA",
                    Color(0xFF64B5F6)
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono de estado
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    // Información principal
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = titulo,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        
                        Text(
                            text = item.codigo,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        
                        Text(
                            text = item.descripcion,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    
                    // Diferencia numérica destacada
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = when {
                                item.estado == EstadoDiferencia.SIN_REFERENCIA ->
                                    item.cantidadActual.toString()
                                item.diferencia > 0 -> "+${item.diferencia}"
                                item.diferencia < 0 -> "${item.diferencia}"
                                else -> "✓"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 28.sp
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (item.estado == EstadoDiferencia.SIN_REFERENCIA) {
                                    "SIN SAP"
                                } else {
                                    "SAP:"
                                },
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                            Text(
                                text = if (item.estado == EstadoDiferencia.SIN_REFERENCIA) {
                                    "CAPTURADO"
                                } else {
                                    "${item.cantidadReferencia}"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                            
                            if (
                                item.estado != EstadoDiferencia.SIN_REFERENCIA &&
                                item.cantidadActual != item.cantidadReferencia
                            ) {
                                Text(
                                    text = "→",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "${item.cantidadActual}",
                                    fontWeight = FontWeight.Bold,
                                    color = BrandColors.GoldAccent,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Variante compacta del HUD para pantallas pequeñas.
 */
@Composable
fun ScannerHUDCompact(
    diferencia: DiferenciaInventario?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = diferencia != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        diferencia?.let { item ->
            val (bgColor, icono) = when (item.estado) {
                EstadoDiferencia.EXCESO -> Pair(Color(0xFFB71C1C), Icons.Default.Error)
                EstadoDiferencia.FALTANTE -> Pair(Color(0xFFF57F17), Icons.Default.Warning)
                EstadoDiferencia.COINCIDE -> Pair(Color(0xFF2E7D32), Icons.Default.CheckCircle)
                EstadoDiferencia.SIN_REFERENCIA -> Pair(Color(0xFF1565C0), Icons.Default.Info)
            }
            
            Surface(
                color = bgColor,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 12.dp,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.codigo,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = when (item.estado) {
                                EstadoDiferencia.EXCESO -> "Exceso de ${item.diferencia} unidades"
                                EstadoDiferencia.FALTANTE -> "Faltan ${kotlin.math.abs(item.diferencia)} unidades"
                                EstadoDiferencia.COINCIDE -> "Correcto"
                                EstadoDiferencia.SIN_REFERENCIA -> "Capturado sin referencia SAP"
                            },
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                    
                    Text(
                        text = if (item.estado == EstadoDiferencia.SIN_REFERENCIA) {
                            "${item.cantidadActual} capt."
                        } else {
                            "${item.cantidadActual}/${item.cantidadReferencia}"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Helper data class para valores cuádruples.
 */
private data class Quadra<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
