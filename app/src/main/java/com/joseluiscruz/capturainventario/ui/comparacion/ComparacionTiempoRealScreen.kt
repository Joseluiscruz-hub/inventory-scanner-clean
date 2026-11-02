package com.joseluiscruz.capturainventario.ui.comparacion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparacionTiempoRealScreen(
    viewModel: InventarioSharedViewModel,
    onBack: () -> Unit,
    onImportarSistema: () -> Unit,
    onImportarAnterior: () -> Unit,
    onIniciarEscaneo: () -> Unit,
    onDescargarNube: () -> Unit = {},
    onExportarSAP: () -> Unit = {}
) {
    LocalContext.current
    val comparacion by viewModel.comparacion.collectAsState()
    val estadisticas by viewModel.estadisticas.collectAsState()
    val progresoCierre by viewModel.progresoCierre.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hudUltimoEscaneo by viewModel.hudUltimoEscaneo.collectAsState()
    viewModel.getInventarioReferencia()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showClearConfirmation by remember { mutableStateOf(false) }

    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Borrar todas las capturas?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Se eliminaran las capturas de todas las tomas guardadas en este " +
                        "dispositivo. La referencia SAP permanecera disponible."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmation = false
                        viewModel.limpiarInventarioEscaneado()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                ) {
                    Text("BORRAR CAPTURAS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BrandColors.Surface1
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        "OPCIONES DE COMPARACION",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BrandColors.TextPrimary
                    )
                    Spacer(Modifier.height(32.dp))

                    DrawerItem(
                        label = "Importar Sistema (.csv)",
                        icon = Icons.Default.FileOpen,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onImportarSistema()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    DrawerItem(
                        label = "Descargar de la Nube",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onDescargarNube()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    DrawerItem(
                        label = "Importar Anterior",
                        icon = Icons.Default.UploadFile,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onImportarAnterior()
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    HorizontalDivider(color = BrandColors.Surface3)

                    Spacer(Modifier.height(16.dp))

                    DrawerItem(
                        label = "Limpiar Datos",
                        icon = Icons.Default.CloudDownload,
                        color = BrandColors.BrandRed,
                        onClick = {
                            scope.launch { drawerState.close() }
                            showClearConfirmation = true
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Comparativa Real",
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandColors.TextPrimary,
                            letterSpacing = 0.5.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = BrandColors.TextPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = BrandColors.TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BrandColors.Surface1,
                        titleContentColor = BrandColors.TextPrimary,
                        navigationIconContentColor = BrandColors.TextPrimary,
                        actionIconContentColor = BrandColors.TextPrimary
                    )
                )
            },
            floatingActionButton = {
                val hayDiferencias = comparacion.any { it.diferencia != 0 }
                val textoFab = if (hayDiferencias) "EXPORTAR SAP" else "INICIAR ESCANEO"
                val iconoFab = if (hayDiferencias) Icons.Default.FileUpload else Icons.Default.QrCodeScanner
                val colorFab = if (hayDiferencias) BrandColors.GoldAccent else BrandColors.BrandRed

                ExtendedFloatingActionButton(
                    text = { Text(textoFab, fontWeight = FontWeight.Bold) },
                    icon = { Icon(iconoFab, contentDescription = null) },
                    onClick = {
                        if (hayDiferencias) onExportarSAP() else onIniciarEscaneo()
                    },
                    containerColor = colorFab,
                    contentColor = if (hayDiferencias) BrandColors.BrandBlack else BrandColors.BrandWhite,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            },
            containerColor = BrandColors.Surface0
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HudEscaneoOverlay(
                    state = hudUltimoEscaneo,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(20f)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .graphicsLayer(
                            alpha = alphaAnim.value,
                            translationY = contentOffsetAnim.value
                        )
                ) {
                    Spacer(Modifier.height(16.dp))

                    ResumenComparativaCard(
                        totalSistema = estadisticas.totalSistema,
                        totalEscaneado = estadisticas.itemsEscaneados,
                        itemsEscaneados = estadisticas.itemsEscaneados,
                        comparacionList = comparacion.toTypedArray()
                    )

                    Spacer(Modifier.height(12.dp))

                    PanelProgresoCierre(progreso = progresoCierre)

                    Spacer(Modifier.height(20.dp))

                    Text(
                        "DETALLE DE DISCREPANCIAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.TextSecondary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = BrandColors.BrandRed)
                        }
                    } else if (comparacion.isEmpty()) {
                        EmptyComparativaState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            items(comparacion) { item ->
                                ComparacionItemCardPremium(item)
                            }
                        }
                    }
                }
            }
        }
    }
}
