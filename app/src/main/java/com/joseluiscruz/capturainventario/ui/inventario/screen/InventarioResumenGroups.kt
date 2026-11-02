package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.util.EventoDetalleResumen
import com.joseluiscruz.capturainventario.util.SkuDetalleResumen
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun ExpandableSkuGroupCard(
    group: SkuDetalleResumen,
    onEdit: (EventoDetalleResumen) -> Unit,
    onDelete: (EventoDetalleResumen) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(group.sku) ?: 0
    val totalCajas = if (cajasXPallet > 0) group.totalPallets * cajasXPallet else 0
    val riesgoGroup = when {
        group.eventos.any { it.riesgo == TpmRiskLevel.RIESGO_ALTO } -> TpmRiskLevel.RIESGO_ALTO
        group.eventos.all { it.riesgo == TpmRiskLevel.SIN_FECHA } -> TpmRiskLevel.SIN_FECHA
        else -> TpmRiskLevel.CUMPLE
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (group.totalPallets > 0) BrandColors.SuccessGreen else Color.Gray)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("SKU: ${group.sku}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandColors.TextPrimary)
                        Text("Eventos: ${group.totalEventos}", fontSize = 11.sp, color = BrandColors.TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        TpmRiskBadge(
                            riesgoTpm = com.joseluiscruz.capturainventario.util.TpmRiskEvaluation(
                                level = riesgoGroup,
                                diasRestantes = group.proximaCaducidadDias,
                                thresholdDays = TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS
                            )
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${group.totalPallets} PLT", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BrandColors.BrandRed)
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = BrandColors.TextSecondary
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = BrandColors.Surface3)
                    Spacer(Modifier.height(12.dp))

                    DetailRow("Total Pallets", "${group.totalPallets}")
                    if (totalCajas > 0) {
                        DetailRow("Total Cajas", "$totalCajas")
                    }
                    DetailRow("Próxima caducidad", group.proximaCaducidadDias?.let { "$it d" } ?: "N/A")

                    Spacer(Modifier.height(16.dp))
                    Text("EVENTOS", color = BrandColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))

                    group.eventos.forEach { evento ->
                        EventoDetalleCard(
                            evento = evento,
                            onEdit = { onEdit(evento) },
                            onDelete = { onDelete(evento) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventoDetalleCard(
    evento: EventoDetalleResumen,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lote: ${evento.lote}", fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
                TpmRiskBadge(
                    riesgoTpm = com.joseluiscruz.capturainventario.util.TpmRiskEvaluation(
                        level = evento.riesgo,
                        diasRestantes = evento.diasRestantes,
                        thresholdDays = TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS
                    )
                )
            }

            Spacer(Modifier.height(6.dp))
            DetailRow("Fecha etiqueta", evento.fechaEtiqueta)
            DetailRow("Cantidad", "${evento.cantidadPallets} PLT")
            DetailRow("Almacén", evento.almacen)
            DetailRow("Ubicación", evento.ubicacion)

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.ErrorRed.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandColors.ErrorRed)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("ELIMINAR", fontSize = 12.sp)
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp), tint = BrandColors.BrandWhite)
                    Spacer(Modifier.width(4.dp))
                    Text("AJUSTAR", fontSize = 12.sp, color = BrandColors.BrandWhite)
                }
            }
        }
    }
}

@Composable
fun TpmRiskBadge(riesgoTpm: com.joseluiscruz.capturainventario.util.TpmRiskEvaluation) {
    val (bg, fg) = when (riesgoTpm.level) {
        TpmRiskLevel.CUMPLE -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        TpmRiskLevel.RIESGO_ALTO -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        TpmRiskLevel.SIN_FECHA -> Color(0xFFF1F3F4) to BrandColors.TextSecondary
    }

    Surface(
        color = bg,
        contentColor = fg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = when (riesgoTpm.level) {
                TpmRiskLevel.CUMPLE -> {
                    val dias = riesgoTpm.diasRestantes ?: 0
                    "TPM OK ($dias d)"
                }

                TpmRiskLevel.RIESGO_ALTO -> {
                    val dias = riesgoTpm.diasRestantes ?: 0
                    "RIESGO TPM ($dias d)"
                }

                TpmRiskLevel.SIN_FECHA -> "TPM sin fecha"
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
