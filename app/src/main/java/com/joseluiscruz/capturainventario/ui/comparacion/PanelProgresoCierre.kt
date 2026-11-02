package com.joseluiscruz.capturainventario.ui.comparacion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.ProgresoCierre
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun PanelProgresoCierre(
    progreso: ProgresoCierre,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "PROGRESO DE CIERRE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BrandColors.TextSecondary,
                letterSpacing = 1.sp
            )

            LinearProgressIndicator(
                progress = { progreso.porcentajePallets.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = BrandColors.BrandRed,
                trackColor = BrandColors.Surface3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ChipMetrica("Avance", "${(progreso.porcentajePallets * 100).toInt()}%")
                ChipMetrica("Restantes", progreso.palletsRestantes.toString())
                ChipMetrica("SKU críticos", progreso.skusCriticos.toString())
                ChipMetrica("TPM riesgo", progreso.skusTPMRiesgo.toString())
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (progreso.skusCriticos > 0) Icons.Default.Warning else Icons.Default.Inventory,
                        contentDescription = null,
                        tint = if (progreso.skusCriticos > 0) BrandColors.BrandRed else BrandColors.TextSecondary
                    )
                    Text(
                        text = if (progreso.skusCriticos > 0) {
                            "Copiloto: atiende ${progreso.skusCriticos} SKU críticos antes de cerrar sesión"
                        } else {
                            "Copiloto: avance estable, continúa escaneo para cerrar faltantes"
                        },
                        color = BrandColors.TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipMetrica(label: String, value: String) {
    val icon = when {
        label.contains("Avance", ignoreCase = true) -> Icons.Default.Inventory
        label.contains("Restantes", ignoreCase = true) -> Icons.Default.Inventory
        label.contains("críticos", ignoreCase = true) -> Icons.Default.Warning
        label.contains("TPM", ignoreCase = true) -> Icons.Default.Warning
        else -> Icons.Default.Inventory
    }
    val iconTint = when {
        label.contains("críticos", ignoreCase = true) -> BrandColors.BrandRed
        label.contains("TPM", ignoreCase = true) -> Color(0xFFFFAB00)
        else -> BrandColors.TextSecondary
    }
    Box(
        modifier = Modifier
            .background(BrandColors.Surface2, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = BrandColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = BrandColors.TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

