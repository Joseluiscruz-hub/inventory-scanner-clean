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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputDataCard(
    skuManual: String, onSkuChange: (String) -> Unit,
    ubicacion: String, onUbicacionChange: (String) -> Unit,
    totalPallets: String, onPalletsChange: (String) -> Unit,
    restos: String, onRestosChange: (String) -> Unit,
    fpcManual: String, onFpcManualChange: (String) -> Unit,
    conManual: String, onConManualChange: (String) -> Unit,
    isSkuEnabled: Boolean, isUbicacionEnabled: Boolean,
    isPalletsEnabled: Boolean, isRestosEnabled: Boolean,
    // Información de Producto Terminado
    esProductoTerminado: Boolean = false,
    esMaterial: Boolean = false,
    cajasXPalletPT: Int = 0,
    totalCajas: Int = 0,
    tarimaPlasticaSku: String = ""
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    var showFpcDatePicker by remember { mutableStateOf(false) }

    val nextKeyboardActions = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Next) }
    )
    val doneKeyboardActions = KeyboardActions(
        onDone = {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    )

    if (showFpcDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showFpcDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            onFpcManualChange(dateFormatter.format(Date(selectedMillis)))
                        }
                        showFpcDatePicker = false
                    }
                ) {
                    Text("ACEPTAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFpcDatePicker = false }) {
                    Text("CANCELAR")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("DATOS DE ENTRADA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PremiumTheme.colors.primary)

            // Indicador de tipo de producto
            if (esProductoTerminado || esMaterial) {
                ProductTypeIndicator(
                    esProductoTerminado = esProductoTerminado,
                    esMaterial = esMaterial,
                    cajasXPalletPT = cajasXPalletPT
                )
            }

            PremiumTextField(
                value = skuManual,
                onValueChange = onSkuChange,
                label = "🏷️ SKU de Material",
                enabled = isSkuEnabled,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = nextKeyboardActions
            )

            PremiumTextField(
                value = ubicacion,
                onValueChange = onUbicacionChange,
                label = "📍 Ubicación / Posición",
                enabled = isUbicacionEnabled,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = nextKeyboardActions
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumTextField(
                    value = totalPallets,
                    onValueChange = onPalletsChange,
                    label = if (esProductoTerminado) "📦 Pallets" else "📦 Piezas",
                    enabled = isPalletsEnabled,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextKeyboardActions
                )
                PremiumTextField(
                    value = restos,
                    onValueChange = onRestosChange,
                    label = "📊 Restos",
                    enabled = isRestosEnabled,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextKeyboardActions
                )
            }

            // Campos adicionales para FPC y TPM (CON)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PremiumDateField(
                    value = fpcManual,
                    label = "📅 FPC (Fecha)",
                    enabled = isSkuEnabled, // Se habilita si el SKU es manual
                    modifier = Modifier.weight(1f),
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        showFpcDatePicker = true
                    }
                )
                PremiumTextField(
                    value = conManual,
                    onValueChange = onConManualChange,
                    label = "📋 CON (TPM)",
                    enabled = isSkuEnabled,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = doneKeyboardActions
                )
            }

            // Mostrar cálculo de cajas para Producto Terminado
            if (esProductoTerminado) if (totalCajas > 0) {
                CalculoCajasCard(
                    totalPallets = totalPallets.toIntOrNull() ?: 0,
                    cajasXPallet = cajasXPalletPT,
                    totalCajas = totalCajas,
                    tarimaPlasticaSku = tarimaPlasticaSku
                )
            }
        }
    }
}

/**
 * Indicador visual del tipo de producto
 */
@Composable
fun ProductTypeIndicator(
    esProductoTerminado: Boolean,
    esMaterial: Boolean,
    cajasXPalletPT: Int
) {
    val backgroundColor = when {
        esProductoTerminado -> PremiumTheme.colors.success.copy(alpha = 0.1f)
        esMaterial -> BrandColors.SyncBlue.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    val textColor = when {
        esProductoTerminado -> PremiumTheme.colors.success
        esMaterial -> BrandColors.SyncBlue
        else -> PremiumTheme.colors.textSecondary
    }
    val icon = when {
        esProductoTerminado -> Icons.Default.CheckCircle
        esMaterial -> Icons.Default.Build
        else -> Icons.Default.Info
    }
    val text = when {
        esProductoTerminado -> "PRODUCTO TERMINADO \u2022 $cajasXPalletPT cajas/pallet"
        esMaterial -> "MATERIAL (TARIMA) \u2022 Conteo en piezas"
        else -> ""
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Tarjeta que muestra el cálculo de cajas para productos terminados
 */
@Composable
fun CalculoCajasCard(
    totalPallets: Int,
    cajasXPallet: Int,
    totalCajas: Int,
    tarimaPlasticaSku: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.colors.warning.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📦 CÁLCULO AUTOMÁTICO",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = PremiumTheme.colors.warning
                )
            }
            Spacer(Modifier.height(8.dp))

            // Fórmula
            Text(
                "$totalPallets pallets \u00D7 $cajasXPallet cajas/pallet = $totalCajas cajas",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PremiumTheme.colors.textPrimary
            )

            Spacer(Modifier.height(4.dp))

            // Tarimas plásticas
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Widgets,
                    contentDescription = null,
                    tint = PremiumTheme.colors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Tarimas plásticas: $totalPallets pzs (SKU: $tarimaPlasticaSku)",
                    fontSize = 12.sp,
                    color = PremiumTheme.colors.textSecondary
                )
            }
        }
    }
}
