package com.joseluiscruz.capturainventario.ui.inventario.screen
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.joseluiscruz.capturainventario.AjusteInventarioActivity
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.repository.toMaterialItem
import com.joseluiscruz.capturainventario.domain.model.OperationResult
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.InventarioResumenHelper
import com.joseluiscruz.capturainventario.util.InventarioShareHelper
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioResumenScreen(
    csvService: CsvInventarioService,
    repository: InventarioRepository
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // Room es la fuente de verdad. El CSV solo se conserva como proyeccion/exportacion.
    val inventoryFlow = remember(repository) { repository.observarEscaneados() }
    val eventosConsolidados by inventoryFlow.collectAsState(initial = emptyList())
    val materiales = remember(eventosConsolidados) {
        eventosConsolidados.map { it.toMaterialItem() }
    }
    // Filtro de Almacen
    val almacenes = remember(materiales) {
        listOf("Todos") + materiales.mapNotNull { it.almacen }.filter { it.isNotBlank() }.distinct()
    }
    var selectedAlmacen by remember { mutableStateOf("Todos") }
    // Estado de Busqueda
    var searchQuery by remember { mutableStateOf("") }
    // Filtrar materiales (Almacen + Busqueda)
    val materialesFiltrados = remember(materiales, selectedAlmacen, searchQuery) {
        var lista = if (selectedAlmacen == "Todos") materiales else materiales.filter { it.almacen == selectedAlmacen }
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            lista = lista.filter {
                it.sku.lowercase().contains(query) ||
                (it.ubicacion?.lowercase()?.contains(query) == true)
            }
        }
        lista
    }
    // Calculos Generales
    val materialesPorAlmacen = remember(materiales, selectedAlmacen) {
        if (selectedAlmacen == "Todos") materiales else materiales.filter { it.almacen == selectedAlmacen }
    }
    val resumenStats = remember(materialesPorAlmacen) { calcularResumenInventario(materialesPorAlmacen) }
    val detallePorSku = remember(materialesFiltrados) { InventarioResumenHelper.agruparDetallePorSku(materialesFiltrados) }
    // Estado para dialogo de eliminacion
    var materialToDelete by remember { mutableStateOf(null as MaterialItem?) }
    // Launcher para exportar CSV
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val projectionResult = withContext(Dispatchers.IO) {
                    val projection = repository.obtenerTodosLosEscaneados()
                        .map { entity -> entity.toMaterialItem() }
                    csvService.reemplazarInventario(projection)
                }
                if (projectionResult.isError) {
                    Toast.makeText(
                        context,
                        "No se pudo preparar la copia CSV desde Room",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                val result = csvService.exportarCsv(it)
                val success = result.isSuccess
                val msg = if (result is OperationResult.Error) result.message else "Exportacion exitosa"
                Toast.makeText(
                    context,
                    msg,
                    if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    // Animaciones
    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(30f) }
    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
    }
    // Dialogo de Eliminacion
    if (materialToDelete != null) {
        AlertDialog(
            onDismissRequest = { materialToDelete = null },
            title = { Text("Eliminar Registro?", fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary) },
            text = { Text("Se eliminara el registro del SKU ${materialToDelete?.sku}. Esta accion no se puede deshacer.", color = BrandColors.TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val materialToDel = materialToDelete
                        if (materialToDel != null) {
                            scope.launch {
                                try {
                                    val csvResult = withContext(Dispatchers.IO) {
                                        val removed = repository.eliminarGrupoConsolidado(
                                            codigo = materialToDel.sku,
                                            ubicacion = materialToDel.ubicacion,
                                            almacen = materialToDel.almacen,
                                            tipoTarima = materialToDel.tipoTarima
                                        )
                                        check(removed > 0) {
                                            "El registro ya no existe en el inventario activo"
                                        }
                                        val projection = repository.obtenerTodosLosEscaneados()
                                            .map { it.toMaterialItem() }
                                        csvService.reemplazarInventario(projection)
                                    }
                                    val message = if (csvResult.isSuccess) {
                                        "Registro eliminado"
                                    } else {
                                        "Registro eliminado de Room; no se pudo actualizar la copia CSV"
                                    }
                                    Toast.makeText(
                                        context,
                                        message,
                                        if (csvResult.isSuccess) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
                                    ).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        e.message ?: "No se pudo eliminar el registro",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        materialToDelete = null
                    }
                ) {
                    Text("ELIMINAR", color = BrandColors.ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { materialToDelete = null }) {
                    Text("CANCELAR", color = BrandColors.TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = BrandColors.Surface2
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen Detallado", fontWeight = FontWeight.ExtraBold, color = BrandColors.TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.Surface1,
                    titleContentColor = BrandColors.TextPrimary,
                    navigationIconContentColor = BrandColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? Activity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrandColors.TextPrimary)
                    }
                }
            )
        },
        containerColor = BrandColors.Surface0
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer(alpha = alphaAnim.value, translationY = contentOffsetAnim.value),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        "RESUMEN GENERAL",
                        color = BrandColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    GeneralStatsCard(
                        totalPallets = resumenStats.totalPallets,
                        totalCajas = resumenStats.totalCajas,
                        totalTarimasPlasticas = resumenStats.totalTarimasPlasticas,
                        skusConRestos = resumenStats.skusConRestos,
                        skusEnRiesgoTpm = resumenStats.skusEnRiesgoTpm,
                        skusSinFechaTpm = resumenStats.skusSinFechaTpm,
                        proximaCaducidadTexto = resumenStats.proximaCaducidadTexto
                    )
                }
                item {
                    ResumenAccionesRapidas(
                        totalEventos = materialesFiltrados.size,
                        skusEnRiesgoTpm = resumenStats.skusEnRiesgoTpm,
                        onExportCsv = { exportLauncher.launch(InventarioResumenHelper.buildCsvFileName()) },
                        onSendEmail = {
                            InventarioShareHelper.sendEmailReport(
                                context = context,
                                subject = InventarioResumenHelper.buildEmailSubject(),
                                body = InventarioResumenHelper.buildEmailBody(materialesFiltrados)
                            )
                        },
                        onLimpiarFiltro = {
                            selectedAlmacen = "Todos"
                            searchQuery = ""
                        }
                    )
                }
                if (resumenStats.skusEnRiesgoTpm > 0) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Alerta TPM",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = BrandColors.BrandRed
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${resumenStats.skusEnRiesgoTpm} SKU(s) con menos de 40 dias de vida util. Proxima caducidad visible: ${resumenStats.proximaCaducidadTexto}.",
                                    fontSize = 12.sp,
                                    color = BrandColors.TextSecondary
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "OCUPACION POR ALMACEN",
                        color = BrandColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    val almacenesData = materiales.groupBy { it.almacen ?: "Sin Almacen" }
                        .mapValues { it.value.sumOf { item -> item.totalPallets?.toIntOrNull() ?: 0 } }
                    OcupacionChart(
                        labelsPipe = almacenesData.keys.joinToString("|"),
                        valuesPipe = almacenesData.values.joinToString("|")
                    )
                }
                item {
                    AlmacenFilterSelector(
                        almacenesPipe = almacenes.joinToString("|"),
                        selected = selectedAlmacen,
                        onSelect = { selectedAlmacen = it }
                    )
                }
                item {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "DETALLE SKU/EVENTO (${detallePorSku.size} SKU)",
                            color = BrandColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                if (detallePorSku.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No se encontraron resultados",
                                color = BrandColors.TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                } else {
                    items(detallePorSku) { group ->
                        ExpandableSkuGroupCard(
                            group = group,
                            onEdit = {
                                context.startActivity(Intent(context, AjusteInventarioActivity::class.java))
                                Toast.makeText(
                                    context,
                                    "La edicion directa esta deshabilitada. Usa Ajuste de Inventario.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onDelete = { materialToDelete = it.material }
                        )
                    }
                }
            }
            BottomActionPanel(
                modifier = Modifier.align(Alignment.BottomCenter),
                onExportPdf = { Toast.makeText(context, "Exportar PDF: Proximamente", Toast.LENGTH_SHORT).show() },
                onExportExcel = {
                    val fileName = InventarioResumenHelper.buildCsvFileName()
                    exportLauncher.launch(fileName)
                },
                onSendEmail = {
                    InventarioShareHelper.sendEmailReport(
                        context = context,
                        subject = InventarioResumenHelper.buildEmailSubject(),
                        body = InventarioResumenHelper.buildEmailBody(materialesFiltrados)
                    )
                }
            )
        }
    }
}
