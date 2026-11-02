package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScannedDataCard(data: com.joseluiscruz.capturainventario.ui.inventario.state.ScannedData) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.primary.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoGraph, null, tint = PremiumTheme.colors.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "DATOS EXTRAÍDOS DEL QR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PremiumTheme.colors.primary
                )
            }
            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScannedItem("SKU", data.sku)
                ScannedItem("DP", data.dp)
                ScannedItem("Centro", data.centro)
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                ScannedItem("Línea", data.linea)
                ScannedItem("OP", data.op)
                ScannedItem("F.Prod", data.fProd)
            }
        }
    }
}

@Composable
fun ScannedItem(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = PremiumTheme.colors.textSecondary)
        Text(
            value.ifBlank { "-" },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTheme.colors.textPrimary
        )
    }
}

@Composable
fun UtilitiesGrid(onResumen: () -> Unit, onComparar: () -> Unit, onRestos: () -> Unit, onExportar: () -> Unit) {
    Column {
        Text(
            "HERRAMIENTAS ADICIONALES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTheme.colors.textSecondary,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UtilityButton("RESUMEN", Icons.Default.Description, Modifier.weight(1f), onResumen)
            UtilityButton("COMPARAR", Icons.Default.CompareArrows, Modifier.weight(1f), onComparar)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UtilityButton("RESTOS", Icons.Default.Inventory, Modifier.weight(1f), onRestos)
            UtilityButton("EXPORTAR", Icons.Default.FileDownload, Modifier.weight(1f), onExportar)
        }
    }
}

@Composable
fun UtilityButton(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = PremiumTheme.colors.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, PremiumTheme.colors.divider)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = PremiumTheme.colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PremiumTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Diálogo de selección personalizado con diseño Premium
 */
@Composable
fun PremiumSelectionDialog(
    title: String,
    icon: ImageVector,
    optionsPipeSeparated: String,
    onSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = optionsPipeSeparated.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.cardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Encabezado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PremiumTheme.colors.primary)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = PremiumTheme.colors.onPrimary)
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = title.uppercase(),
                            color = PremiumTheme.colors.onPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Lista de opciones
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(options) { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(option) }
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = PremiumTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = option,
                                fontSize = 16.sp,
                                color = PremiumTheme.colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 24.dp),
                            color = PremiumTheme.colors.divider
                        )
                    }
                }

                // Botón Cancelar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCELAR", color = PremiumTheme.colors.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
