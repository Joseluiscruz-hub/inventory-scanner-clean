package com.joseluiscruz.capturainventario

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.model.InventarioItem
import com.airbnb.lottie.compose.*

@Composable
fun InventoryComparisonScreen(
    onImportarSistema: () -> Unit,
    onImportarEscaneado: () -> Unit,
    onComparar: () -> Unit,
    onReiniciarComparacion: () -> Unit,
    onLimpiarDatos: () -> Unit,
    onToggleActualizacionTiempoReal: (Boolean) -> Unit,
    actualizacionTiempoRealActivada: Boolean,
    inventarioSistema: Map<String, InventarioItem>,
    inventarioEscaneado: Map<String, InventarioItem>,
    comparacion: List<Triple<String, InventarioItem?, InventarioItem?>>, // SKU, sistema, escaneado
    diferencia: (InventarioItem?, InventarioItem?) -> Int,
    mensaje: String? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isTablet = screenWidthDp > 600.dp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    // Padding responsivo
    val padding = if (isTablet) 24.dp else 16.dp
    val cardPadding = if (isTablet) 20.dp else 16.dp

    Surface(modifier = Modifier.fillMaxSize()) {
        if (isTablet && isLandscape) {
            // Layout de tablet en horizontal - dos columnas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Columna izquierda: Controles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Comparación de Inventarios",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    ImportSectionCard(
                        inventarioSistema = inventarioSistema,
                        inventarioEscaneado = inventarioEscaneado,
                        onImportarSistema = onImportarSistema,
                        onImportarEscaneado = onImportarEscaneado,
                        onComparar = onComparar,
                        onReiniciarComparacion = onReiniciarComparacion,
                        onLimpiarDatos = onLimpiarDatos,
                        onToggleActualizacionTiempoReal = onToggleActualizacionTiempoReal,
                        actualizacionTiempoRealActivada = actualizacionTiempoRealActivada,
                        cardPadding = cardPadding,
                        isTablet = isTablet,
                        comparacion = comparacion
                    )
                }

                // Columna derecha: Resultados
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    ResultsSection(
                        comparacion = comparacion,
                        mensaje = mensaje,
                        inventarioSistema = inventarioSistema,
                        inventarioEscaneado = inventarioEscaneado,
                        diferencia = diferencia,
                        cardPadding = cardPadding,
                        isTablet = isTablet
                    )
                }
            }
        } else {
            // Layout normal - una columna
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Comparación de Inventarios",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ImportSectionCard(
                    inventarioSistema = inventarioSistema,
                    inventarioEscaneado = inventarioEscaneado,
                    onImportarSistema = onImportarSistema,
                    onImportarEscaneado = onImportarEscaneado,
                    onComparar = onComparar,
                    onReiniciarComparacion = onReiniciarComparacion,
                    onLimpiarDatos = onLimpiarDatos,
                    onToggleActualizacionTiempoReal = onToggleActualizacionTiempoReal,
                    actualizacionTiempoRealActivada = actualizacionTiempoRealActivada,
                    cardPadding = cardPadding,
                    isTablet = isTablet,
                    comparacion = comparacion
                )

                Spacer(modifier = Modifier.height(16.dp))

                ResultsSection(
                    comparacion = comparacion,
                    mensaje = mensaje,
                    inventarioSistema = inventarioSistema,
                    inventarioEscaneado = inventarioEscaneado,
                    diferencia = diferencia,
                    cardPadding = cardPadding,
                    isTablet = isTablet
                )
            }
        }
    }
}

@Composable
private fun ImportSectionCard(
    inventarioSistema: Map<String, InventarioItem>,
    inventarioEscaneado: Map<String, InventarioItem>,
    comparacion: List<Triple<String, InventarioItem?, InventarioItem?>>,
    onImportarSistema: () -> Unit,
    onImportarEscaneado: () -> Unit,
    onComparar: () -> Unit,
    onReiniciarComparacion: () -> Unit,
    onLimpiarDatos: () -> Unit,
    onToggleActualizacionTiempoReal: (Boolean) -> Unit,
    actualizacionTiempoRealActivada: Boolean,
    cardPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(cardPadding)
        ) {
            // Sistema import section
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Inventario de Sistema",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${inventarioSistema.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (inventarioSistema.isNotEmpty()) Color(0xFF4CAF50) else Color(0xFF757575)
                        )
                    }
                    Button(
                        onClick = onImportarSistema,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Importar Sistema")
                    }
                }
            } else {
                // Versión móvil - columna
                Column {
                    Text(
                        "Inventario de Sistema",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${inventarioSistema.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (inventarioSistema.isNotEmpty()) Color(0xFF4CAF50) else Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onImportarSistema,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Importar Sistema")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Escaneado import section
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Inventario Escaneado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${inventarioEscaneado.size} items",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (inventarioEscaneado.isNotEmpty()) Color(0xFF4CAF50) else Color(0xFF757575)
                        )
                    }
                    Button(
                        onClick = onImportarEscaneado,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Importar Escaneado")
                    }
                }
            } else {
                Column {
                    Text(
                        "Inventario Escaneado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${inventarioEscaneado.size} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (inventarioEscaneado.isNotEmpty()) Color(0xFF4CAF50) else Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onImportarEscaneado,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Importar Escaneado")
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            // Comparar button
            Button(
                onClick = onComparar,
                modifier = Modifier.fillMaxWidth(),
                enabled = inventarioSistema.isNotEmpty() && inventarioEscaneado.isNotEmpty()
            ) {
                Text("Comparar Inventarios")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sección de acciones adicionales
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                "Acciones adicionales",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Switch para actualización en tiempo real
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Actualización en tiempo real")
                Switch(
                    checked = actualizacionTiempoRealActivada,
                    onCheckedChange = onToggleActualizacionTiempoReal,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones adicionales en una fila para tablet o columna para móvil
            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReiniciarComparacion,
                        modifier = Modifier.weight(1f),
                        enabled = comparacion.isNotEmpty()
                    ) {
                        Text("Reiniciar Comparación")
                    }

                    OutlinedButton(
                        onClick = onLimpiarDatos,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD32F2F)
                        )
                    ) {
                        Text("Limpiar Datos")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onReiniciarComparacion,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = comparacion.isNotEmpty()
                ) {
                    Text("Reiniciar Comparación")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onLimpiarDatos,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Limpiar Datos")
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    comparacion: List<Triple<String, InventarioItem?, InventarioItem?>>,
    mensaje: String?,
    inventarioSistema: Map<String, InventarioItem>,
    inventarioEscaneado: Map<String, InventarioItem>,
    diferencia: (InventarioItem?, InventarioItem?) -> Int,
    cardPadding: androidx.compose.ui.unit.Dp,
    isTablet: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(cardPadding)
        ) {
            Text(
                "Resultados de Comparación",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mostrar mensaje si existe
            mensaje?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Mostrar estadísticas
            if (inventarioSistema.isNotEmpty() || inventarioEscaneado.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Estadísticas",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text("Sistema: ${inventarioSistema.size} items")
                        Text("Escaneado: ${inventarioEscaneado.size} items")
                        if (comparacion.isNotEmpty()) {
                            Text("Diferencias encontradas: ${comparacion.size}")
                        }
                    }
                }
            }

            // Mostrar comparación si existe
            if (comparacion.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comparacion) { (sku, itemSistema, itemEscaneado) ->
                        ComparisonItemCard(
                            sku = sku,
                            itemSistema = itemSistema,
                            itemEscaneado = itemEscaneado,
                            diferencia = diferencia(itemSistema, itemEscaneado),
                            isTablet = isTablet
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparisonItemCard(
    sku: String,
    itemSistema: InventarioItem?,
    itemEscaneado: InventarioItem?,
    diferencia: Int,
    isTablet: Boolean
) {
    val backgroundColor = when {
        diferencia > 0 -> Color(0xFFFFEBEE) // Rojo claro - falta
        diferencia < 0 -> Color(0xFFE8F5E8) // Verde claro - sobra
        else -> Color(0xFFFFF3E0) // Naranja claro - igual
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "SKU: $sku",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isTablet) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sistema: ${itemSistema?.pallets ?: 0}")
                        Text("Escaneado: ${itemEscaneado?.pallets ?: 0}")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Diferencia: $diferencia",
                            fontWeight = FontWeight.Bold,
                            color = when {
                                diferencia > 0 -> Color(0xFFD32F2F)
                                diferencia < 0 -> Color(0xFF388E3C)
                                else -> Color(0xFFFF9800)
                            }
                        )
                    }
                }
            } else {
                Text("Sistema: ${itemSistema?.pallets ?: 0}")
                Text("Escaneado: ${itemEscaneado?.pallets ?: 0}")
                Text(
                    "Diferencia: $diferencia",
                    fontWeight = FontWeight.Bold,
                    color = when {
                        diferencia > 0 -> Color(0xFFD32F2F)
                        diferencia < 0 -> Color(0xFF388E3C)
                        else -> Color(0xFFFF9800)
                    }
                )
            }
        }
    }
}
