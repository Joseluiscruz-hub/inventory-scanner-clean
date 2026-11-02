package com.joseluiscruz.capturainventario.ui.almacenes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import com.joseluiscruz.capturainventario.ui.components.AuroraBackground
import com.joseluiscruz.capturainventario.ui.theme.DarkPremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import java.util.Locale

class GestionAlmacenesActivity : ComponentActivity() {

    private val viewModel: GestionAlmacenesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkPremiumTheme {
                val uiState by viewModel.uiState.collectAsState()
                GestionAlmacenesScreen(
                    uiState = uiState,
                    onBack = ::finish,
                    onRefresh = viewModel::refresh,
                    onSave = viewModel::saveWarehouse,
                    onDelete = viewModel::deleteWarehouse,
                    onMessageShown = viewModel::consumeMessage
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAlmacenesScreen(
    uiState: GestionAlmacenesUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onSave: (String, String, String?) -> Unit,
    onDelete: (AlmacenCapacidad) -> Unit,
    onMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var editingWarehouse by remember { mutableStateOf<AlmacenCapacidad?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var warehouseToDelete by remember { mutableStateOf<AlmacenCapacidad?>(null) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            onMessageShown()
        }
    }

    if (showEditor) {
        WarehouseEditorDialog(
            warehouse = editingWarehouse,
            existingNames = uiState.almacenes.map { it.nombreAlmacen },
            isSaving = uiState.isSaving,
            onDismiss = { showEditor = false },
            onConfirm = { name, capacity ->
                onSave(name, capacity, editingWarehouse?.nombreAlmacen)
                showEditor = false
            }
        )
    }

    warehouseToDelete?.let { warehouse ->
        DeleteWarehouseDialog(
            warehouse = warehouse,
            onDismiss = { warehouseToDelete = null },
            onConfirm = {
                warehouseToDelete = null
                onDelete(warehouse)
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "CONFIGURACIÓN DE ALMACENES",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            letterSpacing = 0.7.sp
                        )
                        Text(
                            "Capacidad operativa",
                            fontSize = 11.sp,
                            color = BrandColors.BrandWhite.copy(alpha = 0.72f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(Icons.Default.Refresh, "Actualizar ocupación")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BrandColors.BrandBlack,
                    titleContentColor = BrandColors.BrandWhite,
                    navigationIconContentColor = BrandColors.BrandWhite,
                    actionIconContentColor = BrandColors.BrandWhite
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (uiState.isError) BrandColors.ErrorRed else BrandColors.Surface2,
                    contentColor = BrandColors.BrandWhite
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingWarehouse = null
                    showEditor = true
                },
                containerColor = BrandColors.BrandRed,
                contentColor = BrandColors.BrandWhite,
                icon = { Icon(Icons.Default.AddBusiness, contentDescription = null) },
                text = { Text("AGREGAR ALMACÉN", fontWeight = FontWeight.ExtraBold) }
            )
        },
        containerColor = BrandColors.Surface0
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AuroraBackground(
                modifier = Modifier.fillMaxSize(),
                alphaIntensity = 0.055f
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = alpha.value),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    OperationsSummaryCard(uiState)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "ALMACENES OPERATIVOS",
                                color = BrandColors.TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                "${uiState.almacenes.size} configurados",
                                color = BrandColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            color = BrandColors.BrandRed.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "PALLETS",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                color = BrandColors.BrandRed,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (uiState.almacenes.isEmpty() && !uiState.isLoading) {
                    item {
                        EmptyWarehousesCard(
                            onAdd = {
                                editingWarehouse = null
                                showEditor = true
                            }
                        )
                    }
                }

                items(
                    items = uiState.almacenes,
                    key = { it.nombreAlmacen.uppercase(Locale.ROOT) }
                ) { warehouse ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                    ) {
                        WarehouseCapacityCard(
                            warehouse = warehouse,
                            onEdit = {
                                editingWarehouse = warehouse
                                showEditor = true
                            },
                            onDelete = { warehouseToDelete = warehouse }
                        )
                    }
                }

                item {
                    OperationalNoteCard()
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = BrandColors.BrandRed,
                    trackColor = BrandColors.Surface2
                )
            }

            if (uiState.isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BrandColors.Surface0.copy(alpha = 0.62f)),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = BrandColors.Surface2,
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = BrandColors.BrandRed,
                                strokeWidth = 3.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("Guardando configuración…", color = BrandColors.TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OperationsSummaryCard(state: GestionAlmacenesUiState) {
    val percentage = state.porcentajeOcupacion
    val status = occupancyStatus(percentage)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            BrandColors.BrandDarkRed,
                            Color(0xFF3A1015),
                            BrandColors.BrandBlack
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = BrandColors.BrandWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "CONTROL OPERATIVO",
                        color = BrandColors.GoldAccent,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        "Capacidad consolidada",
                        color = BrandColors.BrandWhite,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    String.format(Locale.getDefault(), "%.1f%%", percentage),
                    color = status.color,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = status.color,
                trackColor = Color.White.copy(alpha = 0.14f)
            )

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMetric("OCUPADO", state.totalOcupado.toString(), BrandColors.BrandWhite)
                SummaryMetric("DISPONIBLE", state.totalDisponible.toString(), status.color)
                SummaryMetric("CAPACIDAD", state.totalCapacidad.toString(), BrandColors.GoldAccent)
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(
            label,
            color = BrandColors.BrandWhite.copy(alpha = 0.62f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun WarehouseCapacityCard(
    warehouse: AlmacenCapacidad,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val percentage = warehouse.porcentajeSaturacion
    val status = occupancyStatus(percentage)
    val available = (warehouse.capacidadMaxima - warehouse.palletsEscaneados).coerceAtLeast(0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.Surface3),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(status.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Warehouse,
                        contentDescription = null,
                        tint = status.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        warehouse.nombreAlmacen,
                        color = BrandColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        "Capacidad configurada: ${warehouse.capacidadMaxima}",
                        color = BrandColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }
                Surface(
                    color = status.color.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        status.color.copy(alpha = 0.45f)
                    )
                ) {
                    Text(
                        status.label,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        color = status.color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.4.sp
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CapacityMetric("OCUPADO", warehouse.palletsEscaneados, BrandColors.TextPrimary)
                CapacityMetric("DISPONIBLE", available, status.color)
                CapacityMetric("OCUPACIÓN", "${percentage.toInt()}%", status.color)
            }

            Spacer(Modifier.height(14.dp))
            LinearProgressIndicator(
                progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = status.color,
                trackColor = BrandColors.Surface3
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = BrandColors.Surface3)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("EDITAR CAPACIDAD", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = BrandColors.ErrorRed)
                ) {
                    Icon(Icons.Default.DeleteOutline, null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ELIMINAR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun CapacityMetric(label: String, value: Int, color: Color) {
    CapacityMetric(label, value.toString(), color)
}

@Composable
private fun CapacityMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        Text(
            label,
            color = BrandColors.TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyWarehousesCard(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.Surface3)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BrandColors.BrandRed.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warehouse,
                    contentDescription = null,
                    tint = BrandColors.BrandRed,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Configura tu primer almacén",
                color = BrandColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Define la capacidad máxima para controlar ocupación y disponibilidad durante la toma.",
                color = BrandColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onAdd,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddBusiness, null)
                Spacer(Modifier.width(8.dp))
                Text("AGREGAR ALMACÉN", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun OperationalNoteCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF132331),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24445C))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF64B5F6),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "La ocupación se actualiza con los pallets registrados en cada almacén. " +
                    "Eliminar una configuración no borra los escaneos del inventario.",
                color = BrandColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun WarehouseEditorDialog(
    warehouse: AlmacenCapacidad?,
    existingNames: List<String>,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember(warehouse?.nombreAlmacen) {
        mutableStateOf(warehouse?.nombreAlmacen.orEmpty())
    }
    var capacity by remember(warehouse?.capacidadMaxima) {
        mutableStateOf(warehouse?.capacidadMaxima?.toString().orEmpty())
    }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        icon = {
            Icon(
                Icons.Default.AddBusiness,
                contentDescription = null,
                tint = BrandColors.BrandRed
            )
        },
        title = {
            Text(
                if (warehouse == null) "Nuevo almacén" else "Editar capacidad",
                fontWeight = FontWeight.ExtraBold,
                color = BrandColors.TextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    if (warehouse == null) {
                        "Registra el nombre operativo y la capacidad máxima en pallets."
                    } else {
                        "Actualiza la capacidad sin alterar los registros de inventario."
                    },
                    color = BrandColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 40) name = it
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = warehouse == null,
                    label = { Text("Nombre operativo") },
                    supportingText = if (warehouse != null) {
                        { Text("El nombre se conserva para mantener la trazabilidad") }
                    } else {
                        null
                    },
                    singleLine = true,
                    colors = warehouseTextFieldColors()
                )

                OutlinedTextField(
                    value = capacity,
                    onValueChange = {
                        if (it.length <= 7 && it.all(Char::isDigit)) capacity = it
                        validationError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Capacidad máxima") },
                    suffix = { Text("pallets") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = validationError != null,
                    supportingText = validationError?.let { error ->
                        {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(error)
                            }
                        }
                    },
                    colors = warehouseTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = AlmacenConfigurationRules.validate(
                        nombre = name,
                        capacidadText = capacity,
                        existingNames = existingNames,
                        originalName = warehouse?.nombreAlmacen
                    )
                    if (validation.isValid) {
                        onConfirm(validation.nombre, validation.capacidad.toString())
                    } else {
                        validationError = validation.error
                    }
                },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("CANCELAR", color = BrandColors.TextSecondary)
            }
        },
        containerColor = BrandColors.Surface2
    )
}

@Composable
private fun warehouseTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = BrandColors.TextPrimary,
    unfocusedTextColor = BrandColors.TextPrimary,
    disabledTextColor = BrandColors.TextSecondary,
    focusedBorderColor = BrandColors.BrandRed,
    unfocusedBorderColor = BrandColors.Surface3,
    disabledBorderColor = BrandColors.Surface3,
    focusedLabelColor = BrandColors.BrandRed,
    unfocusedLabelColor = BrandColors.TextSecondary,
    disabledLabelColor = BrandColors.TextHint,
    cursorColor = BrandColors.BrandRed,
    errorBorderColor = BrandColors.ErrorRed,
    errorSupportingTextColor = BrandColors.ErrorRed
)

@Composable
private fun DeleteWarehouseDialog(
    warehouse: AlmacenCapacidad,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.DeleteOutline, null, tint = BrandColors.ErrorRed)
        },
        title = {
            Text("¿Eliminar configuración?", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Text(
                "Se eliminará “${warehouse.nombreAlmacen}” de la lista operativa. " +
                    "Los escaneos guardados no se borrarán.",
                color = BrandColors.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.ErrorRed)
            ) {
                Text("ELIMINAR", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = BrandColors.TextSecondary)
            }
        },
        containerColor = BrandColors.Surface2
    )
}

private data class OccupancyStatus(val label: String, val color: Color)

private fun occupancyStatus(percentage: Double): OccupancyStatus = when {
    percentage >= 100.0 -> OccupancyStatus("SOBRECAPACIDAD", BrandColors.ErrorRed)
    percentage >= 90.0 -> OccupancyStatus("CRÍTICO", BrandColors.ErrorRed)
    percentage >= 75.0 -> OccupancyStatus("ALTO", Color(0xFFFF7043))
    percentage >= 50.0 -> OccupancyStatus("MODERADO", BrandColors.WarningAmber)
    else -> OccupancyStatus("DISPONIBLE", BrandColors.SuccessGreen)
}
