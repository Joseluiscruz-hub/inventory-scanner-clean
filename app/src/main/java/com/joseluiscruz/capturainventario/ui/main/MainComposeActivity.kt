package com.joseluiscruz.capturainventario.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.airbnb.lottie.compose.*
import com.joseluiscruz.capturainventario.ui.components.KpiChip
import com.joseluiscruz.capturainventario.ui.components.OpsActionCard
import com.joseluiscruz.capturainventario.ui.components.OpsStatusKind
import com.joseluiscruz.capturainventario.ui.components.PrimaryCta
import com.joseluiscruz.capturainventario.ui.components.OpsSectionTitle
import com.joseluiscruz.capturainventario.ui.components.StatusBadge
import com.joseluiscruz.capturainventario.ui.components.staggeredEntry
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.BrandRadius
import com.joseluiscruz.capturainventario.ui.theme.BrandSpacing
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Operación del home. [requiresAdmin] oculta la tarjeta a usuarios sin rol ADMIN
 * (y [launchOperation] vuelve a validar el rol al pulsar, como defensa en profundidad).
 */
private data class Operation(
    val icon: ImageVector,
    val label: String,
    val subtitle: String,
    val destination: Class<*>,
    val requiresAdmin: Boolean = false
)

/** Ancho por debajo del cual las grids del home pasan a una sola columna. */
private val CompactWidthThreshold = 360.dp

@AndroidEntryPoint
class MainComposeActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ensureAuthenticated()) return
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val kpis by homeViewModel.uiState.collectAsState()
            PremiumTheme(darkTheme = true) { OpsHomeScreen(kpis = kpis) }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isFinishing) ensureAuthenticated()
    }

    private fun ensureAuthenticated(): Boolean {
        val hasFirebaseSession = try {
            com.google.firebase.FirebaseApp.getApps(this).isNotEmpty() &&
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
        } catch (_: Exception) {
            false
        }
        val hasLocalSession = try {
            com.joseluiscruz.capturainventario.manager.AuthManager.currentUser.value != null
        } catch (_: Exception) {
            false
        }
        if (hasFirebaseSession || hasLocalSession) return true

        startActivity(Intent(this, com.joseluiscruz.capturainventario.ui.login.LoginActivity::class.java))
        finish()
        return false
    }
}

@Composable
fun OpsHomeScreen(kpis: HomeKpisUiState = HomeKpisUiState()) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showExitDialog by remember { mutableStateOf(false) }
    val user by com.joseluiscruz.capturainventario.manager.AuthManager.currentUser.collectAsState()

    androidx.activity.compose.BackHandler(enabled = !showExitDialog) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.ExitToApp, null, tint = BrandColors.BrandRed) },
            title = {
                Text("¿Cerrar Aplicación?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Text("¿Deseas salir de Inventario Pro?", style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = { (context as? android.app.Activity)?.finish() },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed),
                    shape = RoundedCornerShape(BrandRadius.sm)
                ) { Text("SALIR", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CANCELAR", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(BrandRadius.lg)
        )
    }

    val isAdmin = user?.role == "ADMIN"
    val operations = remember {
        listOf(
            Operation(
                Icons.Default.BarChart, "Dashboard", "Métricas en vivo",
                com.joseluiscruz.capturainventario.ui.dashboard.DashboardComposeActivity::class.java
            ),
            Operation(
                Icons.Default.Description, "Resumen", "Reporte de sesión",
                com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioResumenComposeActivity::class.java
            ),
            Operation(
                Icons.AutoMirrored.Filled.CompareArrows, "Comparar", "Tiempo real vs SAP",
                com.joseluiscruz.capturainventario.ui.comparacion.ComparacionTiempoRealActivity::class.java
            ),
            Operation(
                Icons.Default.SyncAlt, "Traspasos", "Entre almacenes",
                com.joseluiscruz.capturainventario.TraspasoActivity::class.java,
                requiresAdmin = true
            ),
            Operation(
                Icons.Default.UploadFile, "Importar", "Cargar datos CSV",
                com.joseluiscruz.capturainventario.ui.importar.ImportarActivity::class.java
            ),
            Operation(
                Icons.Default.Download, "Exportar", "Guardar inventario",
                com.joseluiscruz.capturainventario.ui.exportar.ExportarActivity::class.java
            ),
            Operation(
                Icons.Default.Warehouse, "Almacenes", "Capacidad y operación",
                com.joseluiscruz.capturainventario.ui.almacenes.GestionAlmacenesActivity::class.java,
                requiresAdmin = true
            )
        )
    }
    // Las operaciones restringidas no se muestran a no-admin (sin callejones sin salida).
    val visibleOperations = operations.filter { !it.requiresAdmin || isAdmin }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = BrandSpacing.screenHorizontal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OpsHomeHeader(
                username = user?.username ?: "Operador",
                role = user?.role ?: "OPERADOR",
                online = kpis.online,
                onMore = {
                    context.startActivity(
                        Intent(context, com.joseluiscruz.capturainventario.ui.opciones.MasOpcionesActivity::class.java)
                    )
                },
                onSettings = {
                    context.startActivity(
                        Intent(context, com.joseluiscruz.capturainventario.ui.config.ConfiguracionActivity::class.java)
                    )
                }
            )

            Spacer(Modifier.height(BrandSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(BrandSpacing.cardGap)
            ) {
                KpiChip(
                    value = kpis.escaneados.asKpiValue(),
                    label = "Escaneados",
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = kpis.escaneados
                                ?.let { "$it SKUs escaneados en la toma activa" }
                                ?: "SKUs escaneados: sin dato"
                        },
                    accent = BrandColors.BrandRed
                )
                KpiChip(
                    value = kpis.pendientesSync.asKpiValue(),
                    label = "Pend. sync",
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = kpis.pendientesSync
                                ?.let { pendientes ->
                                    val errores = kpis.fallidosSync
                                        ?.let { "$it con error" } ?: "errores sin dato"
                                    "$pendientes registros pendientes de sincronizar, $errores"
                                }
                                ?: "Pendientes de sincronizar: sin dato"
                        },
                    // Rojo si hay fallidos, azul si no hay, neutro si no se pudo leer el conteo de fallidos.
                    accent = when {
                        kpis.fallidosSync == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        kpis.fallidosSync > 0 -> BrandColors.BrandRed
                        else -> BrandColors.SyncBlue
                    }
                )
                KpiChip(
                    value = kpis.tpmEnRiesgo.asKpiValue(),
                    label = "TPM",
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = kpis.tpmEnRiesgo
                                ?.let { "$it SKUs con caducidad a menos de ${com.joseluiscruz.capturainventario.util.TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS} días en la toma activa" }
                                ?: "SKUs en riesgo TPM: sin dato"
                        },
                    accent = BrandColors.WarningAmber
                )
            }

            Spacer(Modifier.height(BrandSpacing.sectionGap))

            PrimaryCta(
                text = "Capturar Inventario",
                subtitle = "Escanea pallets y materiales",
                icon = Icons.Default.QrCodeScanner,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    context.startActivity(
                        Intent(
                            context,
                            com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioComposeActivity::class.java
                        )
                    )
                },
                modifier = Modifier.staggeredEntry(0)
            )

            Spacer(Modifier.height(BrandSpacing.sectionGap))

            OpsSectionTitle("Operaciones")
            Spacer(Modifier.height(BrandSpacing.sm))

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val columns = if (maxWidth < CompactWidthThreshold) 1 else 2
                Column {
                    visibleOperations.chunked(columns).forEachIndexed { rowIdx, rowItems ->
                        Row(
                            // Misma altura para ambas tarjetas de la fila aunque una tenga badge o texto escalado.
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(BrandSpacing.cardGap)
                        ) {
                            rowItems.forEachIndexed { colIdx, item ->
                                OpsActionCard(
                                    icon = item.icon,
                                    label = item.label,
                                    subtitle = item.subtitle,
                                    badge = if (item.requiresAdmin) "ADMIN" else null,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        launchOperation(context, item)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .semantics {
                                            contentDescription = buildString {
                                                append(item.label).append(". ").append(item.subtitle)
                                                if (item.requiresAdmin) append(". Requiere rol administrador")
                                            }
                                        }
                                        .staggeredEntry(rowIdx * columns + colIdx + 1)
                                )
                            }
                            repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                        }
                        Spacer(Modifier.height(BrandSpacing.cardGap))
                    }
                }
            }

            Spacer(Modifier.height(BrandSpacing.md))

            if (isAdmin) {
                OpsSectionTitle("Gestión avanzada")
                Spacer(Modifier.height(BrandSpacing.sm))
                QuietAdminSection(context = context, haptic = haptic)
                Spacer(Modifier.height(BrandSpacing.lg))
            }

            Text(
                text = "Inventario Pro · PLANTA DE MANUFACTURA · v${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(BrandSpacing.md))
        }
    }
}

@Composable
fun App() = OpsHomeScreen()

/** "—" mientras no hay dato; nunca se muestra un número inventado. */
private fun Int?.asKpiValue(): String = this?.toString() ?: "—"

@Composable
private fun OpsHomeHeader(
    username: String,
    role: String,
    online: Boolean?,
    onMore: () -> Unit,
    onSettings: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                val logo by rememberLottieComposition(LottieCompositionSpec.Asset("logo_animation.json"))
                val progress by animateLottieCompositionAsState(
                    logo,
                    iterations = 1,
                    speed = 1f
                )
                LottieAnimation(logo, { progress }, Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(BrandSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Inventario Pro",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "PLANTA DE MANUFACTURA · Operaciones",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    letterSpacing = 0.4.sp
                )
            }
            IconButton(onClick = onMore) {
                Icon(
                    Icons.Default.Tune,
                    "Más opciones",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    Icons.Default.Settings,
                    "Configuración",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(BrandSpacing.sm))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BrandSpacing.xs)
        ) {
            Text(
                text = "$username · $role",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            when (online) {
                true -> StatusBadge(label = "En línea", kind = OpsStatusKind.Sync)
                false -> StatusBadge(label = "Sin conexión", kind = OpsStatusKind.Warning)
                null -> Unit // Aún sin lectura de red: no afirmar nada.
            }
        }
    }
}

@Composable
private fun QuietAdminSection(
    context: android.content.Context,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    data class AdminItem(val label: String, val icon: ImageVector, val action: () -> Unit)

    val items = listOf(
        AdminItem("Ajuste", Icons.Default.Build) {
            context.startActivity(Intent(context, com.joseluiscruz.capturainventario.AjusteInventarioActivity::class.java))
        },
        AdminItem("Reinicio", Icons.Default.Refresh) {
            context.startActivity(
                Intent(context, com.joseluiscruz.capturainventario.ui.reinicio.GestionReinicioActivity::class.java)
            )
        },
        AdminItem("Ayuda", Icons.AutoMirrored.Filled.Help) {
            context.startActivity(
                Intent(context, com.joseluiscruz.capturainventario.ui.opciones.HelpActivity::class.java)
            )
        },
        AdminItem("Feedback", Icons.Default.BugReport) {
            context.startActivity(
                Intent(context, com.joseluiscruz.capturainventario.ui.opciones.FeedbackActivity::class.java)
            )
        },
        AdminItem("Soporte", Icons.Default.Email) {
            context.startActivity(
                Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:soporte@example.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "Soporte - Inventario Pro")
                }
            )
        }
    )

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val columns = if (maxWidth < CompactWidthThreshold) 1 else 2
        val rows = items.chunked(columns)
        Column {
            rows.forEachIndexed { rowIdx, rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BrandSpacing.cardGap)
                ) {
                    rowItems.forEach { item ->
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                item.action()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp),
                            shape = RoundedCornerShape(BrandRadius.sm),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.dp,
                                brush = SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            )
                        ) {
                            Icon(item.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item.label.uppercase(),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp,
                                maxLines = 1
                            )
                        }
                    }
                    repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
                }
                if (rowIdx != rows.lastIndex) {
                    Spacer(Modifier.height(BrandSpacing.xs))
                }
            }
        }
    }
}

/**
 * Punto único de navegación del home: valida el rol ADMIN antes de abrir operaciones restringidas.
 */
private fun launchOperation(context: android.content.Context, operation: Operation) {
    if (operation.requiresAdmin && !com.joseluiscruz.capturainventario.manager.AuthManager.isAdmin()) {
        android.widget.Toast.makeText(
            context,
            "\"${operation.label}\" requiere el rol ADMIN",
            android.widget.Toast.LENGTH_LONG
        ).show()
        return
    }
    context.startActivity(Intent(context, operation.destination))
}
