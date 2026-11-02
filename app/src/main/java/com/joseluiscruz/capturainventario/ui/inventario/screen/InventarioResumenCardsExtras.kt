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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun ResumenAccionesRapidas(
    totalEventos: Int,
    skusEnRiesgoTpm: Int,
    onExportCsv: () -> Unit,
    onSendEmail: () -> Unit,
    onLimpiarFiltro: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ACCIONES RÁPIDAS",
                color = BrandColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatSmallItem("Eventos", "$totalEventos", Icons.Default.List)
                StatSmallItem("En Riesgo TPM", "$skusEnRiesgoTpm", Icons.Default.Warning)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilledTonalButton(onClick = onExportCsv) { Text("EXPORTAR CSV") }
                FilledTonalButton(onClick = onSendEmail) { Text("ENVIAR CORREO") }
                OutlinedButton(onClick = onLimpiarFiltro) { Text("LIMPIAR") }
            }
        }
    }
}

fun calcularResumenInventario(materiales: List<MaterialItem>): ResumenInventarioStats {
    val items = materiales
    val totalPallets = items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
    val totalCajas = items.sumOf {
        val pallets = it.totalPallets?.toIntOrNull() ?: 0
        val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(it.sku) ?: 0
        pallets * cajasXPallet
    }
    val totalTarimasPlasticas = totalPallets
    val skusConRestos = items.count { (it.restos?.toIntOrNull() ?: 0) > 0 }
    val tpmEvaluations = items.map { material ->
        material to TpmRiskPolicy.evaluar(
            material.fechaCaducidadEpochDay
                ?: TpmRiskPolicy.calcularFechaCaducidadEpochDay(material.fProd, material.diasV)
        )
    }
    val skusEnRiesgoTpm = tpmEvaluations.count { (_, riesgo) -> riesgo.level == TpmRiskLevel.RIESGO_ALTO }
    val skusSinFechaTpm = tpmEvaluations.count { (_, riesgo) -> riesgo.level == TpmRiskLevel.SIN_FECHA }
    val proximaCaducidadTexto = tpmEvaluations
        .mapNotNull { (_, riesgo) -> riesgo.diasRestantes }
        .minOrNull()
        ?.let { "$it d" }
        ?: "N/A"

    return ResumenInventarioStats(
        totalPallets = totalPallets,
        totalCajas = totalCajas,
        totalTarimasPlasticas = totalTarimasPlasticas,
        skusConRestos = skusConRestos,
        skusEnRiesgoTpm = skusEnRiesgoTpm,
        skusSinFechaTpm = skusSinFechaTpm,
        proximaCaducidadTexto = proximaCaducidadTexto
    )
}

@Composable
fun StatBigItem(label: String, value: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 11.sp, color = BrandColors.TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StatSmallItem(label: String, value: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = BrandColors.TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
            Text(label, fontSize = 10.sp, color = BrandColors.TextSecondary)
        }
    }
}

@Composable
fun AlmacenFilterSelector(almacenesPipe: String, selected: String, onSelect: (String) -> Unit) {
    val almacenes = almacenesPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
        modifier = Modifier.fillMaxWidth().clickable { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Filtrar por Almacén", fontSize = 10.sp, color = BrandColors.TextSecondary)
                Text(selected, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = BrandColors.BrandRed)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(BrandColors.Surface2)
        ) {
            almacenes.forEach { almacen ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = almacen,
                            fontWeight = if (almacen == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (almacen == selected) BrandColors.BrandRed else BrandColors.TextPrimary
                        )
                    },
                    onClick = {
                        onSelect(almacen)
                        expanded = false
                    }
                )
            }
        }
    }
}
