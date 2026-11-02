package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.AjusteInventarioActivity
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.repository.toMaterialItem
import com.joseluiscruz.capturainventario.domain.model.OperationResult
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.EventoDetalleResumen
import com.joseluiscruz.capturainventario.util.InventarioResumenHelper
import com.joseluiscruz.capturainventario.util.InventarioShareHelper
import com.joseluiscruz.capturainventario.util.SkuDetalleResumen
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditMaterialDialog(
    material: MaterialItem,
    onDismiss: () -> Unit,
    onConfirm: (MaterialItem) -> Unit
) {
    var pallets by remember { mutableStateOf(material.totalPallets ?: "0") }
    var ubicacion by remember { mutableStateOf(material.ubicacion ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Registro", fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary) },
        text = {
            Column {
                Text("SKU: ${material.sku}", fontSize = 14.sp, color = BrandColors.TextSecondary)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = pallets,
                    onValueChange = { pallets = it },
                    label = { Text("Cantidad de Pallets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = BrandColors.BrandRed,
                        unfocusedIndicatorColor = BrandColors.Surface3,
                        focusedTextColor = BrandColors.TextPrimary,
                        unfocusedTextColor = BrandColors.TextPrimary,
                        cursorColor = BrandColors.BrandRed,
                        focusedLabelColor = BrandColors.BrandRed,
                        unfocusedLabelColor = BrandColors.TextSecondary
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    label = { Text("Ubicación") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = BrandColors.BrandRed,
                        unfocusedIndicatorColor = BrandColors.Surface3,
                        focusedTextColor = BrandColors.TextPrimary,
                        unfocusedTextColor = BrandColors.TextPrimary,
                        cursorColor = BrandColors.BrandRed,
                        focusedLabelColor = BrandColors.BrandRed,
                        unfocusedLabelColor = BrandColors.TextSecondary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(material.copy(totalPallets = pallets, ubicacion = ubicacion))
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Text("GUARDAR", color = BrandColors.BrandWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = BrandColors.TextSecondary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = BrandColors.Surface2
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = BrandColors.TextSecondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
    }
}

@Composable
fun BottomActionPanel(
    modifier: Modifier = Modifier,
    onExportPdf: () -> Unit,
    onExportExcel: () -> Unit,
    onSendEmail: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ResumenActionButton("PDF", Icons.Default.PictureAsPdf, Color(0xFFD32F2F), onExportPdf)
            ResumenActionButton("Excel", Icons.Default.TableChart, Color(0xFF388E3C), onExportExcel)
            ResumenActionButton("Correo", Icons.Default.Email, Color(0xFF1976D2), onSendEmail)
        }
    }
}

@Composable
fun ResumenActionButton(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
