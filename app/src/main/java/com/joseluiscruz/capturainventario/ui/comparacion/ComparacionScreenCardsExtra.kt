package com.joseluiscruz.capturainventario.ui.comparacion

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun ComparacionItemCardPremium(item: DiferenciaInventario) {
    val statusColor = when (item.estado) {
        EstadoDiferencia.EXCESO -> BrandColors.SuccessGreen
        EstadoDiferencia.FALTANTE -> BrandColors.BrandRed
        EstadoDiferencia.COINCIDE -> Color(0xFF2E7D32)
        EstadoDiferencia.SIN_REFERENCIA -> Color(0xFF1565C0)
    }

    val icon = when (item.estado) {
        EstadoDiferencia.EXCESO -> Icons.Default.AddCircle
        EstadoDiferencia.FALTANTE -> Icons.Default.RemoveCircle
        EstadoDiferencia.COINCIDE -> Icons.Default.CheckCircle
        EstadoDiferencia.SIN_REFERENCIA -> Icons.Default.Info
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(statusColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = statusColor, modifier = Modifier.size(24.dp))
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.codigo,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = BrandColors.TextPrimary
                    )
                    Text(
                        text = item.descripcion,
                        fontSize = 12.sp,
                        color = BrandColors.TextSecondary,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (item.estado == EstadoDiferencia.SIN_REFERENCIA) {
                            "SIN REFERENCIA"
                        } else {
                            item.estado.name
                        },
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                    val diffText = if (item.diferencia > 0) "+${item.diferencia}" else "${item.diferencia}"
                    Text(
                        text = if (item.estado == EstadoDiferencia.SIN_REFERENCIA) {
                            "Capturado"
                        } else {
                            "Diff: $diffText"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.diferencia != 0) statusColor else BrandColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BrandColors.Surface3)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DataLabelValue(
                    "Sistema",
                    if (item.estado == EstadoDiferencia.SIN_REFERENCIA) "Sin SAP" else "${item.cantidadReferencia}"
                )
                DataLabelValue("Escaneado", "${item.cantidadActual}")
                DataLabelValue(
                    "Estado",
                    if (item.estado == EstadoDiferencia.SIN_REFERENCIA) "CAPTURADO" else item.estado.name
                )
            }
        }
    }
}

@Composable
fun DataLabelValue(label: String, value: String) {
    Column {
        Text(label, fontSize = 9.sp, color = BrandColors.TextSecondary, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 13.sp, color = BrandColors.TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    color: Color = BrandColors.TextPrimary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
        }
    }
}

@Composable
fun EmptyComparativaState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(BrandColors.Surface2, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = BrandColors.BrandRed,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Todo listo para comenzar",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BrandColors.TextPrimary,
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Cargue el inventario del sistema para iniciar\nla comparativa en tiempo real.",
            fontSize = 14.sp,
            color = BrandColors.TextHint,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 48.dp)
        )

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InstructionChip("1", "Menu")
            InstructionChip("2", "Importar CSV")
            InstructionChip("3", "Comparar")
        }
    }
}

@Composable
fun InstructionChip(step: String, label: String) {
    Surface(
        color = BrandColors.Surface2,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(BrandColors.BrandRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    step,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                label,
                color = BrandColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
