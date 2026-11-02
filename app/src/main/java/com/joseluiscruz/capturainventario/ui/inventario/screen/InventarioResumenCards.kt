package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

data class ResumenInventarioStats(
    val totalPallets: Int,
    val totalCajas: Int,
    val totalTarimasPlasticas: Int,
    val skusConRestos: Int,
    val skusEnRiesgoTpm: Int,
    val skusSinFechaTpm: Int,
    val proximaCaducidadTexto: String
)

@Composable
fun OcupacionChart(labelsPipe: String, valuesPipe: String) {
    val labels = labelsPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    val values = valuesPipe.split("|").mapNotNull { it.trim().toIntOrNull() }
    val data = labels.zip(values).toMap()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (data.isEmpty() || data.values.all { it == 0 }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Sin datos para mostrar", color = BrandColors.TextSecondary)
                }
            } else {
                val barColor = BrandColors.BrandRed
                val labelColor = BrandColors.TextSecondary
                val valueColor = BrandColors.TextPrimary
                val maxValue = data.values.maxOrNull()?.toFloat() ?: 1f
                val entries = data.entries.toList()

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barCount = entries.size
                    val spacing = 12.dp.toPx()
                    val barWidth = ((canvasWidth - spacing * (barCount + 1)) / barCount)
                        .coerceAtLeast(20f)
                    val maxBarHeight = canvasHeight * 0.65f
                    val bottomReserved = 32.dp.toPx()

                    entries.forEachIndexed { index, (almacen, pallets) ->
                        val left = spacing + index * (barWidth + spacing)
                        val barHeight = if (maxValue > 0) (pallets / maxValue) * maxBarHeight else 0f
                        val top = canvasHeight - bottomReserved - barHeight

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6.dp.toPx())
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "$pallets",
                            left + barWidth / 2f,
                            top - 6.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = valueColor.toArgb()
                                textSize = 11.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )

                        val label = if (almacen.length > 6) almacen.take(6) + "..." else almacen
                        drawContext.canvas.nativeCanvas.drawText(
                            label,
                            left + barWidth / 2f,
                            canvasHeight - 6.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = labelColor.toArgb()
                                textSize = 10.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        placeholder = { Text("Buscar por SKU o Ubicación...", fontSize = 14.sp, color = BrandColors.TextSecondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandColors.TextSecondary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Borrar", tint = BrandColors.TextSecondary)
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = BrandColors.Surface2,
            unfocusedContainerColor = BrandColors.Surface2,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = BrandColors.TextPrimary,
            unfocusedTextColor = BrandColors.TextPrimary,
            cursorColor = BrandColors.BrandRed
        ),
        singleLine = true
    )
}

@Composable
fun GeneralStatsCard(
    totalPallets: Int,
    totalCajas: Int,
    totalTarimasPlasticas: Int,
    skusConRestos: Int,
    skusEnRiesgoTpm: Int,
    skusSinFechaTpm: Int,
    proximaCaducidadTexto: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatBigItem("Total Pallets", totalPallets.toString(), BrandColors.BrandRed, Icons.Default.Layers)
                StatBigItem("Total Cajas", totalCajas.toString(), BrandColors.GoldAccent, Icons.Default.Inventory2)
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = BrandColors.Surface3)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatSmallItem("Tarimas Plásticas", "$totalTarimasPlasticas pzs", Icons.Default.Widgets)
                StatSmallItem("SKUs con Restos", "$skusConRestos", Icons.Default.PieChart)
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BrandColors.Surface3)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatSmallItem("En Riesgo TPM", "$skusEnRiesgoTpm", Icons.Default.Warning)
                StatSmallItem("Sin Fecha TPM", "$skusSinFechaTpm", Icons.Default.Schedule)
            }
            Spacer(Modifier.height(12.dp))
            DetailRow("Próxima caducidad", proximaCaducidadTexto)
        }
    }
}
