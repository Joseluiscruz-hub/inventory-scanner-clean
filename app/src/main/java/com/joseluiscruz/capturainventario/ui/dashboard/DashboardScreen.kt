package com.joseluiscruz.capturainventario.ui.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.model.MaterialItem
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val csvService = remember { com.joseluiscruz.capturainventario.data.repository.CsvInventarioService(context) }

    var materiales by remember { mutableStateOf<List<MaterialItem>>(emptyList()) }
    var totalPallets by remember { mutableStateOf(0) }
    var alertas by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        val result = csvService.obtenerMaterialesEscaneados()
        val lista = result.getOrNull() ?: emptyList()
        materiales = lista
        totalPallets = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }

        val nuevasAlertas = mutableListOf<String>()
        val skusPorUbicacion = lista.groupBy { it.sku }
        skusPorUbicacion.forEach { (sku, items) ->
            val ubicaciones = items.mapNotNull { it.ubicacion }.distinct()
            if (ubicaciones.size > 1) {
                nuevasAlertas.add("SKU $sku encontrado en ${ubicaciones.size} ubicaciones diferentes")
            }
        }
        lista.forEach { material ->
            val pallets = material.totalPallets?.toIntOrNull() ?: 0
            if (pallets > 100) {
                nuevasAlertas.add("Cantidad alta: ${material.sku} tiene $pallets pallets")
            }
        }
        val tiposNoEstandar = lista.filter {
            val tipo = it.tipoTarima?.uppercase()
            if (tipo == null) false else tipo !in listOf("PLASTICO", "MADERA", "METAL")
        }
        if (tiposNoEstandar.isNotEmpty()) {
            nuevasAlertas.add("${tiposNoEstandar.size} items con tipo de tarima no estandar")
        }
        alertas = nuevasAlertas
    }

    val items = materiales
    val palletsPorAlmacen = items.groupBy { it.almacen ?: "Sin Asignar" }
        .mapValues { it.value.sumOf { mat -> mat.totalPallets?.toIntOrNull() ?: 0 } }

    val topMateriales = items.groupBy { it.sku }
        .mapValues { it.value.sumOf { mat -> mat.totalPallets?.toIntOrNull() ?: 0 } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)

    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard Ejecutivo",
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumTheme.colors.textPrimary
                    )
                },
                backgroundColor = PremiumTheme.colors.surface,
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PremiumTheme.colors.textPrimary
                        )
                    }
                },
                elevation = 0.dp
            )
        },
        backgroundColor = PremiumTheme.colors.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .graphicsLayer(alpha = alphaAnim.value, translationY = contentOffsetAnim.value),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    TimeFilterSection()
                }

                item {
                    KpiSection(totalPallets)
                }

                item {
                    ChartCard(title = "Distribucion por Almacen", icon = Icons.Default.PieChart) {
                        PieChartComponent(
                            palletsPorAlmacen.keys.joinToString("|"),
                            palletsPorAlmacen.values.joinToString("|")
                        )
                    }
                }

                item {
                    ChartCard(title = "Top 5 Materiales (Stock)", icon = Icons.Default.BarChart) {
                        BarChartComponent(
                            topMateriales.map { it.first }.joinToString("|"),
                            topMateriales.map { it.second }.joinToString("|")
                        )
                    }
                }

                item {
                    AlertsSection(alertas.joinToString("|"))
                }
            }
        }
    }
}
