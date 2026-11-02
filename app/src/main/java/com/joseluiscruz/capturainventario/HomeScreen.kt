package com.joseluiscruz.capturainventario

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.R
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun HomeScreen(
    onTipoEscaneo: () -> Unit,
    onCompararInventarios: () -> Unit,
    onAjustesInventario: () -> Unit,
    onMasOpciones: () -> Unit,
    onCapturaInventario: () -> Unit,
    onComparar: () -> Unit,
    onResumen: () -> Unit,
    onImportar: () -> Unit,
    onExportar: () -> Unit,
    onConfiguracion: () -> Unit,
    onDashboard: () -> Unit,
    onGestionAlmacenes: () -> Unit,
    onGestionReinicio: () -> Unit
) {
    Scaffold(
        containerColor = BrandColors.Surface0
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CorporateHeader(
                    operador = "Operador PLANTA DE MANUFACTURA", // TODO: Get real user name
                    totalPallets = 142, // TODO: Get real data
                    almacenesActivos = 3 // TODO: Get real data
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        "ACCIONES RÁPIDAS",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandColors.TextHint,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    GlassActionCard(
                        title = "Escanear Material",
                        subtitle = "Captura QR / código de barras",
                        icon = Icons.Default.QrCodeScanner,
                        accentColor = BrandColors.BrandRed,
                        onClick = onTipoEscaneo
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassActionCard(
                        title = "Traspasos",
                        subtitle = "Mover entre almacenes",
                        icon = Icons.Default.SwapHoriz,
                        accentColor = BrandColors.GoldAccent,
                        onClick = onCompararInventarios // Using this for now as placeholder
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassActionCard(
                        title = "Resumen de Inventario",
                        subtitle = "KPIs y ocupación",
                        icon = Icons.Default.BarChart,
                        accentColor = BrandColors.SuccessGreen,
                        onClick = onResumen
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        "UTILIDADES",
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandColors.TextHint,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Primera fila
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UtilityCard(
                            title = "Ajustes",
                            icon = Icons.Default.Settings,
                            modifier = Modifier.weight(1f),
                            onClick = onConfiguracion
                        )

                        UtilityCard(
                            title = "Sync",
                            icon = Icons.Default.CloudSync,
                            modifier = Modifier.weight(1f),
                            onClick = onImportar
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segunda fila - Dashboard y Almacenes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UtilityCard(
                            title = "Dashboard",
                            icon = Icons.Default.BarChart,
                            modifier = Modifier.weight(1f),
                            onClick = onDashboard
                        )

                        UtilityCard(
                            title = "Almacenes",
                            icon = Icons.Default.Warehouse,
                            modifier = Modifier.weight(1f),
                            onClick = onGestionAlmacenes
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tercera fila - Reinicio y Exportar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UtilityCard(
                            title = "Reinicio",
                            icon = Icons.Default.Refresh,
                            modifier = Modifier.weight(1f),
                            onClick = onGestionReinicio
                        )

                        UtilityCard(
                            title = "Exportar",
                            icon = Icons.Default.Share,
                            modifier = Modifier.weight(1f),
                            onClick = onExportar
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun CorporateHeader(
    operador: String,
    totalPallets: Int,
    almacenesActivos: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BrandColors.BrandDarkRed,
                        BrandColors.Surface0
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            // Logo + bienvenida
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Logo estático
                Image(
                    painter = painterResource(id = R.drawable.ic_inventory),
                    contentDescription = "Logo PLANTA DE MANUFACTURA Inventario",
                    modifier = Modifier.size(48.dp)
                )

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "INVENTORY PRO",
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 24.sp),
                        color = BrandColors.BrandWhite
                    )
                    Text(
                        "Hola, $operador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // KPI Row — 3 métricas en tiempo real
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                KpiChip(
                    label = "PALLETS HOY",
                    value = "$totalPallets",
                    icon = Icons.Default.Inventory2,
                    valueColor = BrandColors.GoldAccent
                )
                KpiChip(
                    label = "ALMACENES",
                    value = "$almacenesActivos",
                    icon = Icons.Default.Warehouse,
                    valueColor = BrandColors.SuccessGreen
                )
                KpiChip(
                    label = "SYNC",
                    value = "LIVE",
                    icon = Icons.Default.CloudDone,
                    valueColor = BrandColors.SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun KpiChip(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = BrandColors.Surface2
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = valueColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = valueColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = BrandColors.TextHint,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GlassActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color = BrandColors.BrandRed,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandColors.Surface1
        ),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.6f),
                    accentColor.copy(alpha = 0.1f)
                )
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo de acento
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandColors.TextPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = BrandColors.TextSecondary
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = BrandColors.TextHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UtilityCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandColors.Surface1
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = BrandColors.TextSecondary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelSmall,
                color = BrandColors.TextSecondary
            )
        }
    }
}
