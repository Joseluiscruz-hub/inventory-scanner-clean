package com.joseluiscruz.capturainventario

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.viewmodel.TraspasoState
import com.joseluiscruz.capturainventario.viewmodel.TraspasoViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraspasoScreen(
    viewModel: TraspasoViewModel,
    operador: String,
    deviceId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val inventario by viewModel.inventarioOrigen.collectAsState()
    val almacenesState = viewModel.almacenes.collectAsState()
    val almacenes: List<String> = almacenesState.value

    var paso by remember { mutableIntStateOf(1) }
    var almacenOrigen by remember { mutableStateOf("") }
    var almacenDestino by remember { mutableStateOf("") }
    var itemSeleccionado by remember { mutableStateOf<MaterialItem?>(null) }
    var palletsMover by remember { mutableStateOf("") }
    var restosMover by remember { mutableStateOf("0") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Traspaso de Almacén", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BrandColors.BrandWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandColors.BrandRed)
            )
        },
        containerColor = BrandColors.Surface0
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BrandWaves(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(120.dp),
                waveHeight = 0.3f,
                opacityBack = 0.05f,
                opacityFront = 0.1f
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                    .graphicsLayer(alpha = alphaAnim.value)
            ) {
                // Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(BrandColors.BrandRed))
                    Spacer(Modifier.width(4.dp))
                    Box(modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (paso >= 2) BrandColors.BrandRed else BrandColors.Surface3))
                }
                Text(
                    text = "Paso $paso de 2",
                    style = MaterialTheme.typography.labelMedium,
                    color = BrandColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(Modifier.height(8.dp))

                if (paso == 1) {
                    PasoSeleccionOrigenPremium(
                        almacenes = almacenes,
                        almacenOrigen = almacenOrigen,
                        inventario = inventario,
                        itemSeleccionado = itemSeleccionado,
                        state = state,
                        onAlmacenOrigenSelected = { almacen ->
                            almacenOrigen = almacen
                            viewModel.cargarInventario(almacen)
                        },
                        onItemSelected = { itemSeleccionado = it },
                        onSiguiente = { paso = 2 },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    PasoSeleccionDestinoPremium(
                        almacenes = almacenes,
                        almacenOrigen = almacenOrigen,
                        almacenDestino = almacenDestino,
                        itemSeleccionado = itemSeleccionado,
                        palletsMover = palletsMover,
                        restosMover = restosMover,
                        state = state,
                        onAlmacenDestinoSelected = { almacenDestino = it },
                        onPalletsChanged = { palletsMover = it.filter { c -> c.isDigit() } },
                        onRestosChanged = { restosMover = it.filter { c -> c.isDigit() } },
                        onAtras = {
                            paso = 1
                            palletsMover = ""
                            restosMover = "0"
                            almacenDestino = ""
                            viewModel.resetState()
                        },
                        onConfirmar = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = BrandColors.Surface1,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(Icons.Default.SwapHoriz, null, tint = BrandColors.BrandRed, modifier = Modifier.size(36.dp)) },
            title = { Text("Confirmar Traspaso", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("De: $almacenOrigen", fontWeight = FontWeight.Bold, color = BrandColors.BrandRed)
                            Text("A: $almacenDestino", fontWeight = FontWeight.Bold, color = BrandColors.SuccessGreen)
                            Spacer(Modifier.height(8.dp))
                            Text("SKU: ${itemSeleccionado?.sku}", fontWeight = FontWeight.Bold)
                            Text("Pallets a mover: $palletsMover")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Esta acción actualizará el inventario inmediatamente.", fontSize = 12.sp, color = BrandColors.TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed),
                    onClick = {
                    showConfirmDialog = false
                    itemSeleccionado?.let {
                        viewModel.ejecutarTraspaso(
                            item = it,
                            almacenDestino = almacenDestino,
                            palletsMover = palletsMover.toIntOrNull() ?: 0,
                            restosMover = restosMover.toIntOrNull() ?: 0,
                            operador = operador,
                            deviceId = deviceId
                        )
                    }
                }) { Text("Confirmar", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar", color = BrandColors.TextSecondary) }
            }
        )
    }

    when (val s = state) {
        is TraspasoState.Success -> {
            LaunchedEffect(s) {
                delay(2000)
                viewModel.resetState()
                onNavigateBack()
            }
        }
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasoSeleccionOrigenPremium(
    almacenes: List<String>,
    almacenOrigen: String,
    inventario: List<MaterialItem>,
    itemSeleccionado: MaterialItem?,
    state: TraspasoState,
    onAlmacenOrigenSelected: (String) -> Unit,
    onItemSelected: (MaterialItem) -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (almacenes.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandColors.ErrorRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WarningAmber, null, tint = BrandColors.ErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No hay almacenes configurados.",
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.ErrorRed,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return
        }

        Text("Selecciona el Almacén Origen", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BrandColors.TextPrimary)
        Spacer(Modifier.height(16.dp))

        var expandedOrigen by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedOrigen,
            onExpandedChange = { expandedOrigen = it }
        ) {
            OutlinedTextField(
                value = almacenOrigen,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elige un almacén") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedOrigen) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface1,
                    unfocusedContainerColor = BrandColors.Surface1,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedOrigen,
                onDismissRequest = { expandedOrigen = false },
                modifier = Modifier.background(BrandColors.Surface1)
            ) {
                almacenes.forEach { almacen ->
                    DropdownMenuItem(
                        text = { Text(almacen, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onAlmacenOrigenSelected(almacen)
                            expandedOrigen = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (almacenOrigen.isNotEmpty()) {
            Text("Selecciona el material a mover:", fontWeight = FontWeight.Bold, color = BrandColors.TextSecondary)
            Spacer(Modifier.height(8.dp))

            if (state is TraspasoState.Loading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandColors.BrandRed)
                }
            } else if (inventario.isEmpty()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No hay inventario en este almacén", color = BrandColors.TextSecondary, modifier = Modifier.padding(32.dp))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(inventario) { item ->
                        MaterialTraspasoCardPremium(
                            item = item,
                            isSelected = itemSeleccionado?.sku == item.sku,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSiguiente,
            enabled = itemSeleccionado != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
        ) {
            Text("Siguiente Paso", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrandColors.BrandWhite)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BrandColors.BrandWhite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasoSeleccionDestinoPremium(
    almacenes: List<String>,
    almacenOrigen: String,
    almacenDestino: String,
    itemSeleccionado: MaterialItem?,
    palletsMover: String,
    restosMover: String,
    state: TraspasoState,
    onAlmacenDestinoSelected: (String) -> Unit,
    onPalletsChanged: (String) -> Unit,
    onRestosChanged: (String) -> Unit,
    onAtras: () -> Unit,
    onConfirmar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Configura el Traspaso", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = BrandColors.TextPrimary)
        Spacer(Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, null, tint = BrandColors.BrandRed)
                    Spacer(Modifier.width(8.dp))
                    Text(itemSeleccionado?.sku ?: "", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(itemSeleccionado?.descripcion ?: "", color = BrandColors.TextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = BrandColors.Surface3)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text("Disponible", fontSize = 11.sp, color = BrandColors.TextSecondary)
                        Text("${itemSeleccionado?.totalPallets} pallets", fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Origen", fontSize = 11.sp, color = BrandColors.TextSecondary)
                        Text(almacenOrigen, fontWeight = FontWeight.Bold, color = BrandColors.BrandRed)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Almacén Destino", fontWeight = FontWeight.Bold, color = BrandColors.TextSecondary)
        Spacer(Modifier.height(8.dp))

        var expandedDestino by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedDestino,
            onExpandedChange = { expandedDestino = it }
        ) {
            OutlinedTextField(
                value = almacenDestino,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Elige almacén de destino") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedDestino) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface1,
                    unfocusedContainerColor = BrandColors.Surface1,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedDestino,
                onDismissRequest = { expandedDestino = false },
                modifier = Modifier.background(BrandColors.Surface1)
            ) {
                val almacenesDestino = almacenes.filter { it != almacenOrigen }
                almacenesDestino.forEach { almacen ->
                    DropdownMenuItem(
                        text = { Text(almacen, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onAlmacenDestinoSelected(almacen)
                            expandedDestino = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = palletsMover,
                onValueChange = onPalletsChanged,
                label = { Text("Pallets") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface1,
                    unfocusedContainerColor = BrandColors.Surface1,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = restosMover,
                onValueChange = onRestosChanged,
                label = { Text("Restos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface1,
                    unfocusedContainerColor = BrandColors.Surface1,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))
        
        when (val s = state) {
            is TraspasoState.Loading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrandColors.BrandRed)
                }
            }
            is TraspasoState.Success -> {
                Card(colors = CardDefaults.cardColors(containerColor = BrandColors.SuccessGreen.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                    Text("✅ ${s.mensaje}", color = BrandColors.SuccessGreen, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }
            is TraspasoState.Error -> {
                Card(colors = CardDefaults.cardColors(containerColor = BrandColors.ErrorRed.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
                    Text("❌ ${s.mensaje}", color = BrandColors.ErrorRed, modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                }
            }
            else -> {}
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onAtras,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) { Text("Volver", color = BrandColors.TextSecondary, fontWeight = FontWeight.Bold) }

            Button(
                onClick = onConfirmar,
                enabled = almacenDestino.isNotEmpty() && (palletsMover.toIntOrNull() ?: 0) > 0,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite)
            }
        }
    }
}

@Composable
private fun MaterialTraspasoCardPremium(
    item: MaterialItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) BrandColors.BrandRed.copy(alpha = 0.05f) else BrandColors.Surface1)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) BrandColors.BrandRed else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.sku, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = BrandColors.TextPrimary)
                Text(item.descripcion.ifBlank { "Sin descripción" }, fontSize = 13.sp, color = BrandColors.TextSecondary, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = BrandColors.Surface3, contentColor = BrandColors.TextPrimary) {
                        Text("${item.totalPallets} pallets", modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(item.tipoTarima ?: "-", fontSize = 12.sp, color = BrandColors.BrandRed, fontWeight = FontWeight.Bold)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, tint = BrandColors.BrandRed, modifier = Modifier.size(28.dp))
            }
        }
    }
}
