package com.joseluiscruz.capturainventario.ui.comparacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun ResumenComparativaCard(
    totalSistema: Int,
    totalEscaneado: Int,
    itemsEscaneados: Int,
    comparacionList: Array<DiferenciaInventario>
) {
    val rows = comparacionList.toList()
    val progress = if (totalSistema > 0) itemsEscaneados.toFloat() / totalSistema else 0f
    val accuracy = if (totalSistema > 0) {
        val matches = rows.count { it.estado == EstadoDiferencia.COINCIDE }
        (matches.toFloat() / totalSistema * 100).toInt()
    } else 0

    Card(
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(BrandColors.Surface2, BrandColors.Surface1)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "EFECTIVIDAD DE CARGA",
                            color = BrandColors.GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "$itemsEscaneados / $totalSistema SKU",
                            color = BrandColors.BrandWhite,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = BrandColors.SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Precision: $accuracy%",
                                color = BrandColors.BrandWhite.copy(alpha = 0.9f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            color = BrandColors.BrandWhite.copy(alpha = 0.1f),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(70.dp)
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            color = BrandColors.BrandRed,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(70.dp)
                        )
                        Text(
                            "${(progress * 100).toInt()}%",
                            color = BrandColors.BrandWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = BrandColors.BrandWhite.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStatItem("PALLETS", totalEscaneado.toString(), Icons.Default.Inventory)
                    val faltantes = rows.sumOf {
                        if (it.estado == EstadoDiferencia.FALTANTE) Math.abs(it.diferencia) else 0
                    }
                    SummaryStatItem("FALTANTES", faltantes.toString(), Icons.Default.TrendingDown, BrandColors.BrandRed)
                    val sobrantes = rows.sumOf {
                        if (it.estado == EstadoDiferencia.EXCESO) it.diferencia else 0
                    }
                    SummaryStatItem("SOBRANTES", sobrantes.toString(), Icons.Default.TrendingUp, BrandColors.SuccessGreen)
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, value: String, icon: ImageVector, color: Color = BrandColors.BrandWhite) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = BrandColors.BrandWhite.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
