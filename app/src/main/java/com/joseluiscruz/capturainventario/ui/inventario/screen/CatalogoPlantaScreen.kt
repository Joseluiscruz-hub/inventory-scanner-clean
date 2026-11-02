package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.joseluiscruz.capturainventario.data.CatalogoPlantaEntity
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.inventario.viewmodel.CatalogoPlantaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoPlantaScreen(
    onBack: () -> Unit,
    viewModel: CatalogoPlantaViewModel = hiltViewModel()
) {
    val plantas by viewModel.plantas.collectAsState()
    val catalogoActual by viewModel.catalogoActual.collectAsState()
    val plantaSeleccionada by viewModel.plantaSeleccionada.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catálogos por Planta", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = BrandColors.BrandWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.AddBusiness, contentDescription = "Agregar Planta", tint = BrandColors.BrandWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandColors.BrandRed)
            )
        },
        containerColor = BrandColors.Surface0
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Selector de Plantas
            if (plantas.isNotEmpty()) {
                val selectedIndex = plantas.indexOf(plantaSeleccionada).coerceAtLeast(0)
                ScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = BrandColors.Surface1,
                    contentColor = BrandColors.BrandRed,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    plantas.forEachIndexed { index, planta ->
                        Tab(
                            selected = index == selectedIndex,
                            onClick = { viewModel.seleccionarPlanta(planta) },
                            text = { Text(planta, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }

            if (catalogoActual.isEmpty()) {
                EmptyCatalogState { showImportDialog = true }
            } else {
                CatalogList(catalogoActual)
            }
        }
    }

    if (showImportDialog) {
        ImportCatalogDialog(
            onDismiss = { showImportDialog = false },
            onConfirm = { nombre, contenido ->
                viewModel.importarCatalogoTexto(nombre, contenido)
                showImportDialog = false
            }
        )
    }
}

@Composable
fun CatalogList(items: List<CatalogoPlantaEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "SKUs Registrados: ${items.size}",
                fontSize = 12.sp,
                color = BrandColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )
        }
        items(items) { item ->
            CatalogItemCard(item)
        }
    }
}

@Composable
fun CatalogItemCard(item: CatalogoPlantaEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrandColors.BrandRed.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = null, tint = BrandColors.BrandRed)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("SKU: ${item.sku}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(item.descripcion, fontSize = 12.sp, color = BrandColors.TextSecondary, maxLines = 1)
                }
                Badge(containerColor = BrandColors.SuccessGreen.copy(alpha = 0.2f)) {
                    Text("${item.cajasXTarima} CxP", color = BrandColors.SuccessGreen, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = BrandColors.Surface3)
            Spacer(Modifier.height(12.dp))
            
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                CatalogStat("Marca", item.marca)
                CatalogStat("TPM", "${item.tpmDias} d")
                CatalogStat("Estiba", "${item.nivelEstiba}")
            }
        }
    }
}

@Composable
fun CatalogStat(label: String, value: String) {
    Column {
        Text(label, fontSize = 10.sp, color = BrandColors.TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
    }
}

@Composable
fun EmptyCatalogState(onAdd: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(64.dp), tint = BrandColors.Surface3)
            Spacer(Modifier.height(16.dp))
            Text("No hay catálogos configurados", color = BrandColors.TextSecondary)
            Button(
                onClick = onAdd,
                modifier = Modifier.padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Text("IMPORTAR CATÁLOGO")
            }
        }
    }
}

@Composable
fun ImportCatalogDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var nombrePlanta by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Planta", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombrePlanta,
                    onValueChange = { nombrePlanta = it },
                    label = { Text("Nombre de la Planta (ej. Norte)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Pega aquí el catálogo") },
                    placeholder = { Text("Pega las columnas desde Excel...") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10
                )
                Text(
                    "Format: Sku, Descripcion, ..., Cajas x Tarima",
                    fontSize = 10.sp,
                    color = BrandColors.TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nombrePlanta, contenido) },
                enabled = nombrePlanta.isNotBlank() && contenido.isNotBlank()
            ) {
                Text("IMPORTAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
