package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.ui.components.ScanLineEffect
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

/**
 * Card con controles manuales para activar/desactivar la comparación en tiempo real
 */
@Composable
fun ComparisonControlCard(
    isActive: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    faltantes: Int,
    sobrantes: Int,
    diferencias: Int
) {
    // Activa: superficie elevada (surfaceVariant = Surface2 en oscuro) con borde de acento
    // (verde si está sincronizado, ámbar si hay desviación). Antes usaba colors.secondary
    // (#B3B3B3) con texto blanco: contraste ~2:1, ilegible en planta.
    val accent = if (diferencias == 0) PremiumTheme.colors.success else PremiumTheme.colors.warning
    val neutral = PremiumTheme.colors.textSecondary.copy(alpha = 0.6f)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceVariant else PremiumTheme.colors.cardBackground
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
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
                        tint = if (isActive) accent else PremiumTheme.colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "COMPARACIÓN TIEMPO REAL",
                        color = PremiumTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }

                Button(
                    onClick = { if (isActive) onStop() else onStart() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) PremiumTheme.colors.primary else PremiumTheme.colors.success
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isActive) "DETENER" else "INICIAR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (isActive) {
                Spacer(Modifier.height(16.dp))

                Surface(
                    color = if (diferencias == 0) PremiumTheme.colors.success else PremiumTheme.colors.error,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (diferencias == 0) "✓ SINCRONIZADO" else "⚠ DESVIACIÓN DETECTADA",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ComparisonStatPremium(
                        "FALTAN",
                        faltantes.toString(),
                        if (faltantes > 0) PremiumTheme.colors.error else neutral
                    )
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    ComparisonStatPremium(
                        "SOBRAN",
                        sobrantes.toString(),
                        if (sobrantes > 0) BrandColors.SuccessGreen else neutral
                    )
                    VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    ComparisonStatPremium(
                        "DIFF SKU",
                        diferencias.toString(),
                        if (diferencias > 0) PremiumTheme.colors.warning else neutral
                    )
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Presiona INICIAR para comparar inventario escaneado vs sistema",
                    color = PremiumTheme.colors.textSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun MainScannerCard(
    onScan: () -> Unit,
    onModeChange: (ScanMode) -> Unit,
    currentMode: ScanMode,
    /** Estado del toggle "Escaneo continuo"; `null` lo oculta. */
    escaneoContinuo: Boolean? = null,
    onEscaneoContinuoChange: (Boolean) -> Unit = {}
) {
    Column {
        Text(
            "MÉTODOS DE CAPTURA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTheme.colors.textSecondary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.cardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box {
                ScanLineEffect(
                    modifier = Modifier.matchParentSize(),
                    lineColor = PremiumTheme.colors.primary.copy(alpha = 0.15f)
                )

                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = onScan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumTheme.colors.primary)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(28.dp), tint = PremiumTheme.colors.onPrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("INICIAR ESCANEO", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = PremiumTheme.colors.onPrimary)
                    }

                    Spacer(Modifier.height(16.dp))

                    // Control segmentado: un solo modo activo, expuesto como grupo de radio
                    // para TalkBack ("RUMBA, botón de opción, seleccionado, 1 de 3").
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModeButton(
                            "RUMBA",
                            currentMode == ScanMode.RUMBA,
                            Modifier.weight(1f)
                        ) { onModeChange(ScanMode.RUMBA) }
                        ModeButton(
                            "PALLET",
                            currentMode == ScanMode.PALLET,
                            Modifier.weight(1f)
                        ) { onModeChange(ScanMode.PALLET) }
                        ModeButton(
                            "MANUAL",
                            currentMode == ScanMode.MANUAL,
                            Modifier.weight(1f)
                        ) { onModeChange(ScanMode.MANUAL) }
                    }

                    // "Escaneo continuo" vive junto a los modos (antes ocupaba la barra inferior).
                    if (escaneoContinuo != null) {
                        Spacer(Modifier.height(8.dp))
                        EscaneoContinuoRow(
                            checked = escaneoContinuo,
                            scanMode = currentMode,
                            onCheckedChange = onEscaneoContinuoChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) PremiumTheme.colors.primary else PremiumTheme.colors.surface
    val contentColor = if (isSelected) PremiumTheme.colors.onPrimary else PremiumTheme.colors.textSecondary
    val borderColor = if (!isSelected) PremiumTheme.colors.divider else Color.Transparent

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
private fun EscaneoContinuoRow(
    checked: Boolean,
    scanMode: com.joseluiscruz.capturainventario.domain.model.ScanMode,
    onCheckedChange: (Boolean) -> Unit
) {
    val aplica = com.joseluiscruz.capturainventario.util.ContinuousScanPolicy.toggleAplica(scanMode)
    val subtitulo = when (scanMode) {
        com.joseluiscruz.capturainventario.domain.model.ScanMode.PALLET ->
            "En PALLET el escáner siempre se relanza"
        com.joseluiscruz.capturainventario.domain.model.ScanMode.MANUAL ->
            "No aplica en captura MANUAL"
        else -> "Relanza el escáner tras guardar"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(
                value = checked,
                enabled = aplica,
                role = androidx.compose.ui.semantics.Role.Switch,
                onValueChange = onCheckedChange
            )
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "Escaneo continuo",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (aplica) PremiumTheme.colors.textPrimary else PremiumTheme.colors.textSecondary
            )
            Text(subtitulo, fontSize = 12.sp, color = PremiumTheme.colors.textSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = null, // el Row es el objetivo táctil (≥48dp) y expone la semántica
            enabled = aplica
        )
    }
}
