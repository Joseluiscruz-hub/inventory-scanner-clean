package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.components.AutoSizeText
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

@Composable
fun OperativoStatusCard(
    almacen: String,
    modo: String,
    tarima: String?,
    referenciaCargada: Boolean,
    cargandoReferenciaSap: Boolean,
    onTarimaClick: () -> Unit,
    onLoadSapClick: () -> Unit,
    /**
     * Abre la confirmación segura de "Nueva toma" (conteo sin sincronizar + exportar CSV antes
     * de borrar). La tarjeta NO borra nada por sí misma; `null` oculta el botón.
     */
    onNuevaTomaClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Estado Operativo",
                        fontSize = 14.sp,
                        color = PremiumTheme.colors.textSecondary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = almacen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumTheme.colors.textPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (referenciaCargada) {
                        Surface(
                            color = BrandColors.SuccessGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.SuccessGreen)
                        ) {
                            Text(
                                "SAP OK",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.SuccessGreen
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    } else {
                        // Objetivo táctil de 48dp (antes 36dp).
                        IconButton(
                            onClick = onLoadSapClick,
                            enabled = !cargandoReferenciaSap,
                            modifier = Modifier
                                .size(48.dp)
                                .background(BrandColors.WarningAmber.copy(alpha = 0.1f), CircleShape)
                        ) {
                            if (cargandoReferenciaSap) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = BrandColors.WarningAmber,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.CloudDownload,
                                    contentDescription = "Cargar referencia SAP",
                                    tint = BrandColors.WarningAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (cargandoReferenciaSap) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PremiumTheme.colors.primary,
                    trackColor = PremiumTheme.colors.primary.copy(alpha = 0.12f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Cargando referencia SAP...",
                    color = PremiumTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InventarioStatusChip(
                    label = "MODO",
                    value = modo,
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.weight(1f)
                )

                InventarioStatusChip(
                    label = "TARIMA",
                    value = tarima ?: "ESTÁNDAR",
                    icon = Icons.Default.Layers,
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(
                            onClickLabel = "Cambiar tipo de tarima",
                            role = Role.Button
                        ) { onTarimaClick() }
                )
            }

            if (onNuevaTomaClick != null) {
                Spacer(Modifier.height(14.dp))
                // Botón discreto (como en la rama de diseño) que abre el MISMO diálogo seguro del
                // menú ⋮: muestra registros sin sincronizar y ofrece exportar el CSV antes de borrar.
                OutlinedButton(
                    onClick = onNuevaTomaClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        PremiumTheme.colors.primary.copy(alpha = 0.45f)
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumTheme.colors.primary)
                ) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = PremiumTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "NUEVA TOMA · BORRAR CAPTURAS",
                        color = PremiumTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Chip de estado (MODO / TARIMA) translúcido como en la rama de diseño: capa de `onSurface`
 * al 10 % con borde al 15 % e ícono en círculo, derivados del tema (legible también en claro).
 * Textos ≥12sp; el valor se autoajusta de 14 a 12sp para que "DEFAULT" quepa completo.
 * Altura mínima 56dp: cumple 48dp cuando es clicable.
 */
@Composable
fun InventarioStatusChip(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val onSurface = PremiumTheme.colors.onSurface
    Surface(
        modifier = modifier.heightIn(min = 56.dp),
        shape = RoundedCornerShape(14.dp),
        color = onSurface.copy(alpha = 0.10f),
        contentColor = onSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, onSurface.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(onSurface.copy(alpha = 0.20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = onSurface, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    color = onSurface.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                AutoSizeText(
                    text = value,
                    maxFontSize = 14.sp,
                    minFontSize = 12.sp,
                    color = onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun RealTimeComparisonCard(faltantes: Int, sobrantes: Int, diferencias: Int) {
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            (if (diferencias == 0) PremiumTheme.colors.success else PremiumTheme.colors.warning).copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Compare,
                        contentDescription = null,
                        tint = PremiumTheme.colors.warning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "COMPARATIVA EN VIVO",
                        color = PremiumTheme.colors.textPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    color = if (diferencias == 0) PremiumTheme.colors.success else PremiumTheme.colors.error,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (diferencias == 0) "SINCRO OK" else "DESVIACIÓN",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonStatPremium(
                    "FALTAN",
                    faltantes.toString(),
                    if (faltantes > 0) PremiumTheme.colors.error else PremiumTheme.colors.textSecondary.copy(alpha = 0.6f)
                )
                VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ComparisonStatPremium(
                    "SOBRAN",
                    sobrantes.toString(),
                    if (sobrantes > 0) BrandColors.SuccessGreen else PremiumTheme.colors.textSecondary.copy(alpha = 0.6f)
                )
                VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                ComparisonStatPremium(
                    "DIFF SKU",
                    diferencias.toString(),
                    if (diferencias > 0) PremiumTheme.colors.warning else PremiumTheme.colors.textSecondary.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ComparisonStatPremium(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        // textSecondary: legible sobre superficies oscuras (antes blanco al 70 % sobre gris claro).
        Text(label, color = PremiumTheme.colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ComparisonStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = PremiumTheme.colors.onSecondary.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
    }
}
