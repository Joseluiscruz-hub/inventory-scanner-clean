package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.inventario.component.ScannerHUDPremium
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioEvent
import com.joseluiscruz.capturainventario.ui.inventario.state.SnackbarType
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InventarioScreen(
    viewModel: InventarioComposeViewModel,
    onBack: () -> Unit,
    onOpenCalculator: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val ultimoItemHUD by viewModel.ultimoItemHUD.collectAsState()
    val ultimoEscaneo by viewModel.ultimoEscaneo.collectAsState()
    val isKeyboardVisible = WindowInsets.isImeVisible
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showExitDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    androidx.activity.compose.BackHandler(enabled = !showExitDialog) {
        showExitDialog = true
    }

    androidx.activity.compose.BackHandler(enabled = showExitDialog) {
        showExitDialog = false
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = PremiumTheme.colors.primary) },
            title = { Text("¿Abandonar Captura?", fontWeight = FontWeight.Bold) },
            text = { Text("Aún tienes la sesión de escaneo activa. ¿Seguro que deseas volver al menú principal?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PremiumTheme.colors.primary)
                ) {
                    Text("ABANDONAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("CONTINUAR ESCANEANDO")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = PremiumTheme.colors.cardBackground
        )
    }

    LaunchedEffect(ultimoItemHUD) {
        ultimoItemHUD?.let {
            delay(3500)
            viewModel.limpiarHUD()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            val action = uiState.snackbarAction
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = action?.label,
                    withDismissAction = action != null,
                    duration = if (action != null) SnackbarDuration.Long else SnackbarDuration.Short
                )
                viewModel.handleEvent(InventarioEvent.OnSnackbarShown)
                if (result == SnackbarResult.ActionPerformed && action != null) {
                    viewModel.handleEvent(InventarioEvent.OnSnackbarAction(action))
                }
            }
        }
    }

    if (uiState.showNuevaTomaDialog) {
        NuevaTomaDialog(
            cargando = uiState.nuevaTomaCargando,
            totalRegistros = uiState.nuevaTomaTotalRegistros,
            sinSincronizar = uiState.nuevaTomaSinSincronizar,
            onExportar = { viewModel.handleEvent(InventarioEvent.OnNuevaTomaExportarClick) },
            onConfirmar = { viewModel.handleEvent(InventarioEvent.OnResetSession) },
            onDismiss = { viewModel.handleEvent(InventarioEvent.OnNuevaTomaDialogDismissed) }
        )
    }

    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        viewModel.handleEvent(InventarioEvent.OnScreenLoaded)
        launch { alphaAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    // Una sola línea: con el menú ⋮ el título se partía en dos. Se autoajusta
                    // (18→13sp) y, si aún no cabe, recorta con "…".
                    com.joseluiscruz.capturainventario.ui.components.AutoSizeText(
                        text = "REGISTRO DE INVENTARIO",
                        maxFontSize = 18.sp,
                        minFontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumTheme.colors.onSurface,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = PremiumTheme.colors.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.handleEvent(InventarioEvent.OnAlmacenClick) }) {
                        Icon(Icons.Default.Warehouse, "Almacén", tint = BrandColors.BrandRed)
                    }
                    // Acciones destructivas fuera de la tarjeta principal: menú secundario.
                    Box {
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Más opciones", tint = PremiumTheme.colors.onSurface)
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nueva toma · borrar capturas") },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = PremiumTheme.colors.error)
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.handleEvent(InventarioEvent.OnNuevaTomaClick)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PremiumTheme.colors.surface,
                    titleContentColor = PremiumTheme.colors.onSurface,
                    navigationIconContentColor = PremiumTheme.colors.onSurface,
                    actionIconContentColor = PremiumTheme.colors.onSurface
                )
            )
        },
        bottomBar = {
            CapturaBottomBar(
                ultimoEscaneo = ultimoEscaneo,
                comparacionActiva = uiState.isComparisonActive,
                isSaving = uiState.isSaving,
                canSave = uiState.isGuardarButtonEnabled,
                onGuardar = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.handleEvent(InventarioEvent.OnGuardarClick)
                },
                onSiguiente = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    viewModel.handleEvent(InventarioEvent.OnSiguienteClick)
                }
            )
        },
        floatingActionButton = {
            // FAB normal de 56dp (como en la rama de diseño; cómodo con guantes). Scaffold lo
            // coloca por encima de la bottomBar, así que no tapa GUARDAR/SIGUIENTE, y la lista
            // reserva espacio al final para que ningún campo quede debajo de él.
            if (onOpenCalculator != null && !isKeyboardVisible) {
                FloatingActionButton(
                    onClick = onOpenCalculator,
                    containerColor = BrandColors.Surface2,
                    contentColor = BrandColors.BrandRed
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = "Calculadora")
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                val containerColor = when (uiState.snackbarType) {
                    SnackbarType.SUCCESS -> PremiumTheme.colors.success
                    SnackbarType.ERROR -> PremiumTheme.colors.error
                    SnackbarType.INFO -> PremiumTheme.colors.secondary
                }
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = containerColor,
                    contentColor = Color.White,
                    actionColor = Color.White,
                    dismissActionContentColor = Color.White
                )
            }
        },
        containerColor = PremiumTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            ) {
                // HUD flotante solo con comparación activa (como en la rama de diseño); el último
                // escaneo siempre queda visible en la barra inferior.
                if (uiState.isComparisonActive) {
                    ScannerHUDPremium(diferencia = ultimoItemHUD)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alphaAnim.value, translationY = contentOffsetAnim.value),
                contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    OperativoStatusCard(
                        almacen = uiState.selectedAlmacen,
                        modo = uiState.scanMode.id.uppercase(),
                        tarima = uiState.tipoTarimaSeleccionada,
                        referenciaCargada = uiState.isReferenciaSistemaCargada,
                        cargandoReferenciaSap = uiState.isLoadingReferenciaSap,
                        onTarimaClick = { viewModel.handleEvent(InventarioEvent.OnTipoTarimaClick) },
                        onLoadSapClick = { viewModel.handleEvent(InventarioEvent.OnLoadSystemInventory) },
                        // Mismo diálogo seguro que el menú ⋮ (no borra directamente).
                        onNuevaTomaClick = { viewModel.handleEvent(InventarioEvent.OnNuevaTomaClick) }
                    )
                }

                item {
                    MainScannerCard(
                        onScan = { viewModel.handleEvent(InventarioEvent.OnScanClick) },
                        onModeChange = { viewModel.handleEvent(InventarioEvent.OnScanModeSelected(it)) },
                        currentMode = uiState.scanMode,
                        escaneoContinuo = uiState.escaneoContinuo,
                        onEscaneoContinuoChange = {
                            viewModel.handleEvent(InventarioEvent.OnEscaneoContinuoChanged(it))
                        }
                    )
                }

                item {
                    InputDataCard(
                        skuManual = uiState.skuManual,
                        onSkuChange = { viewModel.handleEvent(InventarioEvent.OnSkuManualChanged(it)) },
                        ubicacion = uiState.ubicacion,
                        onUbicacionChange = { viewModel.handleEvent(InventarioEvent.OnUbicacionChanged(it)) },
                        totalPallets = uiState.totalPallets,
                        onPalletsChange = { viewModel.handleEvent(InventarioEvent.OnTotalPalletsChanged(it)) },
                        restos = uiState.restos,
                        onRestosChange = { viewModel.handleEvent(InventarioEvent.OnRestosChanged(it)) },
                        fpcManual = uiState.fpcManual,
                        onFpcManualChange = { viewModel.handleEvent(InventarioEvent.OnFpcManualChanged(it)) },
                        conManual = uiState.conManual,
                        onConManualChange = { viewModel.handleEvent(InventarioEvent.OnConManualChanged(it)) },
                        isSkuEnabled = uiState.isSkuManualEnabled,
                        esProductoTerminado = uiState.esProductoTerminado,
                        esMaterial = uiState.esMaterial,
                        cajasXPalletPT = uiState.cajasXPalletPT,
                        totalCajas = uiState.totalCajas,
                        tarimaPlasticaSku = uiState.tarimaPlasticaSku,
                        isUbicacionEnabled = uiState.isUbicacionEnabled,
                        isPalletsEnabled = uiState.isTotalPalletsEnabled,
                        isRestosEnabled = uiState.isRestosEnabled
                    )
                }

                if (uiState.showScannedFields) if (uiState.scannedData.sku.isNotEmpty()) {
                    item {
                        ScannedDataCard(uiState.scannedData)
                    }
                }

                item {
                    ComparisonControlCard(
                        isActive = uiState.isComparisonActive,
                        onStart = { viewModel.handleEvent(InventarioEvent.OnComparisonStart) },
                        onStop = { viewModel.handleEvent(InventarioEvent.OnComparisonStop) },
                        faltantes = uiState.totalFaltantes,
                        sobrantes = uiState.totalSobrantes,
                        diferencias = uiState.itemsConDiferencia
                    )
                }

                item {
                    UtilitiesGrid(
                        onResumen = { viewModel.handleEvent(InventarioEvent.OnResumenClick) },
                        onComparar = { viewModel.handleEvent(InventarioEvent.OnCompararEnviarClick) },
                        onRestos = { viewModel.handleEvent(InventarioEvent.OnRestosButtonClick) },
                        onExportar = { viewModel.handleEvent(InventarioEvent.OnExportarClick) }
                    )
                }

                // Espacio para que el FAB de calculadora (56dp + márgenes) no tape el último elemento.
                item { Spacer(Modifier.height(88.dp)) }
            }

            if (uiState.showAlmacenDialog) {
                PremiumSelectionDialog(
                    title = "Seleccionar Almacén",
                    icon = Icons.Default.Warehouse,
                    optionsPipeSeparated = uiState.almacenesDisponibles.joinToString("|"),
                    onSelected = { viewModel.handleEvent(InventarioEvent.OnAlmacenSelected(it)) },
                    onDismiss = { viewModel.handleEvent(InventarioEvent.OnAlmacenDialogDismissed) }
                )
            }

            if (uiState.showTipoTarimaDialog) {
                PremiumSelectionDialog(
                    title = "Tipo de Tarima",
                    icon = Icons.Default.Layers,
                    optionsPipeSeparated = com.joseluiscruz.capturainventario.domain.model.TipoTarima
                        .etiquetas.joinToString("|"),
                    onSelected = { viewModel.handleEvent(InventarioEvent.OnTipoTarimaSelected(it)) },
                    onDismiss = { viewModel.handleEvent(InventarioEvent.OnTipoTarimaDialogDismissed) }
                )
            }
        }
    }
}

/**
 * Confirmación de "Nueva toma": muestra cuántos registros se borran y cuántos siguen sin
 * sincronizar, y ofrece exportar el CSV antes de borrar.
 */
@Composable
private fun NuevaTomaDialog(
    cargando: Boolean,
    totalRegistros: Int?,
    sinSincronizar: Int?,
    onExportar: () -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    val hayPendientes = (sinSincronizar ?: 0) > 0 || (!cargando && sinSincronizar == null)
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = PremiumTheme.colors.error) },
        title = { Text("¿Iniciar una nueva toma?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (cargando) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Revisando capturas sin sincronizar…")
                    }
                } else {
                    Text(
                        com.joseluiscruz.capturainventario.util.MensajesUsuario
                            .confirmacionNuevaToma(totalRegistros, sinSincronizar)
                    )
                }
                if (!cargando && (totalRegistros ?: 1) > 0) {
                    OutlinedButton(
                        onClick = onExportar,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("EXPORTAR CSV PRIMERO")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !cargando,
                colors = ButtonDefaults.buttonColors(containerColor = PremiumTheme.colors.error)
            ) {
                Text(if (hayPendientes) "BORRAR DE TODOS MODOS" else "SÍ, INICIAR DE CERO")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = PremiumTheme.colors.cardBackground
    )
}

/**
 * Barra inferior fija y compacta (~100dp): una línea de último escaneo + GUARDAR/SIGUIENTE.
 * El toggle "Escaneo continuo" vive en la tarjeta de métodos (MainScannerCard).
 * Respeta la barra de navegación y sube con el teclado (imePadding) para que GUARDAR
 * siempre sea visible mientras se capturan pallets/ubicación.
 */
@Composable
private fun CapturaBottomBar(
    ultimoEscaneo: com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario?,
    comparacionActiva: Boolean,
    isSaving: Boolean,
    canSave: Boolean,
    onGuardar: () -> Unit,
    onSiguiente: () -> Unit
) {
    Surface(
        color = PremiumTheme.colors.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            UltimoEscaneoStrip(ultimoEscaneo, comparacionActiva)
            InventarioActionButtons(
                isSaving = isSaving,
                canSave = canSave,
                onGuardar = onGuardar,
                onSiguiente = onSiguiente
            )
        }
    }
}

@Composable
private fun UltimoEscaneoStrip(
    item: com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario?,
    comparacionActiva: Boolean
) {
    val texto = if (item == null) {
        "Último escaneo: —"
    } else buildString {
        append("Último: ").append(item.codigo)
        append(" · total ").append(item.cantidadActual)
        if (comparacionActiva && item.estado !=
            com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia.SIN_REFERENCIA
        ) {
            append(" / SAP ").append(item.cantidadReferencia)
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = null,
            tint = PremiumTheme.colors.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = PremiumTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (item != null) {
            Text(
                text = item.descripcion,
                fontSize = 12.sp,
                color = PremiumTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f)
            )
        }
    }
}


@Composable
private fun InventarioActionButtons(
    isSaving: Boolean,
    canSave: Boolean,
    onGuardar: () -> Unit,
    onSiguiente: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onGuardar,
            modifier = Modifier
                .weight(1.3f)
                .height(56.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PremiumTheme.colors.primary,
                disabledContainerColor = PremiumTheme.colors.primary.copy(alpha = 0.4f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 2.dp
            ),
            enabled = canSave
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = PremiumTheme.colors.onPrimary,
                    strokeWidth = 2.5.dp
                )
            } else {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    tint = PremiumTheme.colors.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "GUARDAR",
                    fontWeight = FontWeight.ExtraBold,
                    color = PremiumTheme.colors.onPrimary,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }
        }

        OutlinedButton(
            onClick = onSiguiente,
            modifier = Modifier
                .weight(0.9f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, PremiumTheme.colors.divider),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PremiumTheme.colors.textSecondary
            )
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = PremiumTheme.colors.textSecondary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "SIGUIENTE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = PremiumTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
