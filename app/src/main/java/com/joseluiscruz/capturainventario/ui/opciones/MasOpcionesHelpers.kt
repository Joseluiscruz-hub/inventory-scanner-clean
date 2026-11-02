package com.joseluiscruz.capturainventario.ui.opciones

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun SmartSyncCard(viewModel: InventarioSharedViewModel) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatus by remember { mutableStateOf(SyncStatus.Idle) }
    var lastSyncMessage by remember { mutableStateOf("Ultima sincronizacion: Desconocida") }

    val infiniteTransition = rememberInfiniteTransition(label = "syncPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSyncing) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "syncScale"
    )

    val cardColor = when (syncStatus) {
        SyncStatus.Idle -> BrandColors.Surface1
        SyncStatus.Syncing -> Color(0xFF1A237E)
        SyncStatus.Success -> Color(0xFF1B5E20)
        SyncStatus.Error -> Color(0xFFB71C1C)
    }

    val iconColor = when (syncStatus) {
        SyncStatus.Idle -> BrandColors.TextSecondary
        SyncStatus.Syncing -> Color(0xFF2196F3)
        SyncStatus.Success -> BrandColors.SuccessGreen
        SyncStatus.Error -> BrandColors.ErrorRed
    }

    val icon = when (syncStatus) {
        SyncStatus.Idle -> Icons.Default.CloudQueue
        SyncStatus.Syncing -> Icons.Default.CloudSync
        SyncStatus.Success -> Icons.Default.CloudDone
        SyncStatus.Error -> Icons.Default.CloudOff
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSyncing) {
                isSyncing = true
                syncStatus = SyncStatus.Syncing
                viewModel.sincronizarDesdeNube { success, message ->
                    isSyncing = false
                    syncStatus = if (success) SyncStatus.Success else SyncStatus.Error
                    lastSyncMessage = if (success) message else "Error: $message"
                }
            }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(scale)
                    .background(BrandColors.Surface2, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isSyncing) "Sincronizando..." else "Sincronizacion Nube",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary
                )
                Text(
                    text = if (isSyncing) "Descargando datos..." else lastSyncMessage,
                    fontSize = 12.sp,
                    color = if (syncStatus == SyncStatus.Error) BrandColors.ErrorRed else BrandColors.TextSecondary,
                    maxLines = 2
                )
            }
            if (!isSyncing) {
                if (syncStatus == SyncStatus.Idle) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Descargar",
                        tint = BrandColors.BrandRed
                    )
                }
            }
        }
    }
}

enum class SyncStatus {
    Idle, Syncing, Success, Error
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = BrandColors.TextSecondary,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
fun OptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = BrandColors.TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = BrandColors.TextHint
            )
        }
    }
}

fun showAboutDialog(context: android.content.Context) {
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle("Acerca de Inventario Pro")
        .setMessage("Version: ${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME}\n\nDesarrollado para optimizar el control de materiales en plantas y bodegas de PLANTA DE MANUFACTURA.\n\n(c) 2026 PLANTA DE MANUFACTURA")
        .setPositiveButton("Cerrar", null)
        .show()
}

fun sendSupportEmail(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:soporte.it@example.com")
        putExtra(
            Intent.EXTRA_SUBJECT,
            "Soporte - Inventario Pro v${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME}"
        )
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No hay apps de correo instaladas", android.widget.Toast.LENGTH_SHORT)
            .show()
    }
}

fun sendInventoryEmail(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_SUBJECT, "Reporte de Inventario - PLANTA DE MANUFACTURA")
        putExtra(
            Intent.EXTRA_TEXT,
            "Adjunto encontrara el reporte de inventario.\n\nGenerado por Inventario Pro v${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME}"
        )
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Enviar correo..."))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "No hay apps de correo instaladas", android.widget.Toast.LENGTH_SHORT)
            .show()
    }
}
