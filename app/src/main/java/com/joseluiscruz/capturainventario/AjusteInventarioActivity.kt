package com.joseluiscruz.capturainventario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.viewmodel.AjusteInventarioPendiente
import com.joseluiscruz.capturainventario.viewmodel.AjusteInventarioViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class AjusteInventarioActivity : ComponentActivity() {

    private val viewModel: AjusteInventarioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!com.joseluiscruz.capturainventario.manager.AuthManager.isAdmin()) {
            Toast.makeText(this, "Acceso restringido a administradores", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        setContent {
            PremiumTheme {
                AjusteInventarioScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjusteInventarioScreen(viewModel: AjusteInventarioViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    val materialesFiltrados = remember(uiState.materiales, uiState.searchQuery) {
        val query = uiState.searchQuery.trim().lowercase(Locale.getDefault())
        if (query.isBlank()) {
            uiState.materiales
        } else {
            uiState.materiales.filter { item ->
                listOf(
                    item.sku,
                    item.descripcion,
                    item.tipoTarima,
                    item.almacen,
                    item.ubicacion,
                    item.op
                ).any { it?.lowercase(Locale.getDefault())?.contains(query) == true }
            }
        }
    }

    if (uiState.showAuthDialog) {
        val user by com.joseluiscruz.capturainventario.manager.AuthManager.currentUser.collectAsState()
        val usuarioActual = user?.username ?: "Administrador"

        AlertDialog(
            onDismissRequest = { viewModel.onMostrarAuthDialog(false) },
            icon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandColors.BrandRed)
            },
            title = {
                Text(
                    stringResource(R.string.auth_gerencia_title),
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        stringResource(R.string.auth_gerencia_message),
                        color = BrandColors.TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Sesión: $usuarioActual",
                        color = BrandColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (!uiState.authError.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            uiState.authError.orEmpty(),
                            color = BrandColors.ErrorRed,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.autorizarYAplicarAjuste { success, message ->
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !uiState.isApplyingAjuste,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                ) {
                    Text(
                        stringResource(R.string.btn_autorizar),
                        color = BrandColors.BrandWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onMostrarAuthDialog(false) }) {
                    Text(stringResource(R.string.btn_cancelar), color = BrandColors.TextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = BrandColors.Surface2
        )
    }

    if (uiState.materialEnEdicion != null) {
        val itemActual = uiState.materialEnEdicion!!
        val draftExistente = uiState.ajustesPendientes.firstOrNull { viewModel.sameRegistro(it.material, itemActual) }
        var cantidadCorrecta by remember(itemActual) {
            mutableStateOf(draftExistente?.cantidadCorrecta?.toString() ?: (itemActual.totalPallets ?: "0"))
        }
        var motivo by remember(itemActual) {
            mutableStateOf(draftExistente?.motivo.orEmpty())
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.onEditarMaterial(null) },
            containerColor = BrandColors.Surface1,
            dragHandle = {
                Surface(
                    modifier = Modifier.padding(top = 10.dp),
                    color = BrandColors.Surface3,
                    shape = CircleShape
                ) {
                    Box(Modifier.size(width = 48.dp, height = 5.dp))
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.title_ajuste_inventario),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = BrandColors.TextPrimary
                )
                Text(
                    text = stringResource(R.string.subtitle_ajuste_inventario),
                    color = BrandColors.TextSecondary,
                    fontSize = 13.sp
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandColors.Surface2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(itemActual.sku, fontWeight = FontWeight.Bold, color = BrandColors.BrandRed)
                        Spacer(Modifier.height(4.dp))
                        Text(itemActual.descripcion.ifBlank { stringResource(R.string.sin_descripcion) }, color = BrandColors.TextPrimary)
                        Spacer(Modifier.height(8.dp))
                        DetailRow(stringResource(R.string.label_actual), itemActual.totalPallets ?: "0")
                        DetailRow(stringResource(R.string.label_almacen), itemActual.almacen ?: "-")
                        DetailRow(stringResource(R.string.label_tipo), itemActual.tipoTarima ?: "-")
                        DetailRow(stringResource(R.string.label_ubicacion), itemActual.ubicacion ?: "-")
                    }
                }

                OutlinedTextField(
                    value = cantidadCorrecta,
                    onValueChange = { cantidadCorrecta = it.filter { caracter -> caracter.isDigit() } },
                    label = { Text(stringResource(R.string.label_cantidad_correcta)) },
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

                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text(stringResource(R.string.label_motivo_ajuste)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    minLines = 3,
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

                val cantidadCorrectaInt = cantidadCorrecta.toIntOrNull()
                val cantidadActual = itemActual.totalPallets?.toIntOrNull() ?: 0
                val diferencia = if (cantidadCorrectaInt != null) cantidadCorrectaInt - cantidadActual else 0

                if (cantidadCorrectaInt != null) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.label_diferencia), color = BrandColors.TextSecondary)
                        Text(
                            if (diferencia >= 0) "+$diferencia" else diferencia.toString(),
                            fontWeight = FontWeight.Bold,
                            color = if (diferencia == 0) BrandColors.TextSecondary else BrandColors.BrandRed
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.onEditarMaterial(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_cancelar), color = BrandColors.TextSecondary)
                    }
                    Button(
                        onClick = {
                            val cantidadFinal = cantidadCorrecta.toIntOrNull()
                            val motivoLimpio = motivo.trim()

                            when {
                                cantidadFinal == null -> {
                                    Toast.makeText(context, context.getString(R.string.error_cantidad_invalida), Toast.LENGTH_SHORT).show()
                                }
                                cantidadFinal < 0 -> {
                                    Toast.makeText(context, context.getString(R.string.error_cantidad_negativa), Toast.LENGTH_SHORT).show()
                                }
                                motivoLimpio.isBlank() -> {
                                    Toast.makeText(context, context.getString(R.string.error_motivo_vacio), Toast.LENGTH_SHORT).show()
                                }
                                cantidadFinal == cantidadActual -> {
                                    Toast.makeText(context, context.getString(R.string.error_sin_diferencia), Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    viewModel.onGuardarAjusteBorrador(cantidadFinal, motivoLimpio)
                                    Toast.makeText(context, context.getString(R.string.msg_ajuste_borrador), Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                    ) {
                        Text(stringResource(R.string.btn_guardar), color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.title_ajuste_inventario), fontWeight = FontWeight.ExtraBold, color = BrandColors.BrandWhite)
                        Text(
                            stringResource(R.string.subtitle_ajuste_appbar),
                            fontSize = 12.sp,
                            color = BrandColors.BrandWhite.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.BrandRed,
                    titleContentColor = BrandColors.BrandWhite,
                    navigationIconContentColor = BrandColors.BrandWhite,
                    actionIconContentColor = BrandColors.BrandWhite
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrandColors.BrandWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.cargarMateriales() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = BrandColors.BrandWhite)
                    }
                }
            )
        },
        containerColor = BrandColors.Surface0,
        bottomBar = {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.limpiarBorradores() },
                        enabled = uiState.ajustesPendientes.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = BrandColors.ErrorRed)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_limpiar), color = BrandColors.ErrorRed)
                    }
                    Button(
                        onClick = {
                            if (uiState.ajustesPendientes.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.error_no_ajustes), Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.onMostrarAuthDialog(true)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = BrandColors.BrandWhite)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_generar_ajuste), color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BrandWaves(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(120.dp),
                waveHeight = 0.3f,
                opacityBack = 0.05f,
                opacityFront = 0.1f
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer(alpha = alphaAnim.value),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AjusteResumenCard(
                        totalEscaneos = uiState.materiales.size,
                        ajustesPendientes = uiState.ajustesPendientes.size,
                        operador = uiState.operadorActual,
                        onRefrescar = { viewModel.cargarMateriales() }
                    )
                }

                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.hint_buscar_sku)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrandColors.TextSecondary) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda", tint = BrandColors.TextSecondary)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BrandColors.Surface1,
                            unfocusedContainerColor = BrandColors.Surface1,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = BrandColors.TextPrimary,
                            unfocusedTextColor = BrandColors.TextPrimary,
                            cursorColor = BrandColors.BrandRed
                        )
                    )
                }

                if (uiState.ajustesPendientes.isNotEmpty()) {
                    item {
                        PendingAdjustmentsCard(
                            ajustes = uiState.ajustesPendientes,
                            onRemove = { item -> viewModel.onRemoverAjusteBorrador(item) }
                        )
                    }
                }

                item {
                    Text(
                        text = if (materialesFiltrados.isEmpty()) stringResource(R.string.empty_list) else stringResource(R.string.list_escaneos),
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                if (materialesFiltrados.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else {
                    items(materialesFiltrados) { item ->
                        val pendiente = uiState.ajustesPendientes.firstOrNull { viewModel.sameRegistro(it.material, item) }
                        ScanAdjustmentCard(
                            material = item,
                            pendiente = pendiente,
                            onAjustar = { viewModel.onEditarMaterial(it) }
                        )
                    }
                }

                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
private fun AjusteResumenCard(
    totalEscaneos: Int,
    ajustesPendientes: Int,
    operador: String,
    onRefrescar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.title_resumen), fontWeight = FontWeight.ExtraBold, color = BrandColors.BrandRed, fontSize = 18.sp)
                IconButton(onClick = onRefrescar) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refrescar", tint = BrandColors.BrandRed)
                }
            }
            DetailRow(stringResource(R.string.label_escaneos_cargados), totalEscaneos.toString())
            DetailRow(stringResource(R.string.label_ajustes_borrador), ajustesPendientes.toString())
            DetailRow(stringResource(R.string.label_operador), operador)
            Text(
                text = stringResource(R.string.msg_edicion_deshabilitada),
                color = BrandColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun PendingAdjustmentsCard(
    ajustes: List<AjusteInventarioPendiente>,
    onRemove: (AjusteInventarioPendiente) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.title_borrador_ajuste), fontWeight = FontWeight.ExtraBold, color = BrandColors.TextPrimary)
            ajustes.forEach { ajuste ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SKU ${ajuste.material.sku} · ${if (ajuste.diferencia >= 0) "+" else ""}${ajuste.diferencia}",
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.TextPrimary
                        )
                        Text(
                            text = ajuste.motivo,
                            color = BrandColors.TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                    IconButton(onClick = { onRemove(ajuste) }) {
                        Icon(Icons.Default.Close, contentDescription = "Eliminar borrador", tint = BrandColors.ErrorRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanAdjustmentCard(
    material: MaterialItem,
    pendiente: AjusteInventarioPendiente?,
    onAjustar: (MaterialItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (pendiente != null) BrandColors.Surface2 else BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(material.sku, fontWeight = FontWeight.ExtraBold, color = BrandColors.BrandRed, fontSize = 16.sp)
                    Text(
                        material.descripcion.ifBlank { stringResource(R.string.sin_descripcion) },
                        color = BrandColors.TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 2
                    )
                }
                if (pendiente != null) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text(stringResource(R.string.label_en_borrador)) },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BrandColors.SuccessGreen.copy(alpha = 0.18f),
                            selectedLabelColor = BrandColors.SuccessGreen,
                            selectedLeadingIconColor = BrandColors.SuccessGreen
                        )
                    )
                }
            }

            DetailRow(stringResource(R.string.label_actual), material.totalPallets ?: "0")
            DetailRow(stringResource(R.string.label_almacen), material.almacen ?: "-")
            DetailRow(stringResource(R.string.label_tipo), material.tipoTarima ?: "-")
            DetailRow(stringResource(R.string.label_ubicacion), material.ubicacion ?: "-")

            if (pendiente != null) {
                DetailRow(stringResource(R.string.label_correcta), pendiente.cantidadCorrecta.toString())
                DetailRow(stringResource(R.string.label_motivo_ajuste), pendiente.motivo)
                DetailRow(stringResource(R.string.label_diferencia), if (pendiente.diferencia >= 0) "+${pendiente.diferencia}" else pendiente.diferencia.toString())
            }

            Button(
                onClick = { onAjustar(material) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = BrandColors.BrandWhite)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (pendiente == null) stringResource(R.string.btn_registrar_ajuste) else stringResource(R.string.btn_editar_ajuste),
                    color = BrandColors.BrandWhite,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Inventory2, contentDescription = null, tint = BrandColors.TextSecondary)
            Text(stringResource(R.string.empty_state_title), fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
            Text(
                stringResource(R.string.empty_state_desc),
                color = BrandColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = BrandColors.TextSecondary, fontSize = 12.sp)
        Text(value, color = BrandColors.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
