package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.inventario.component.MaterialCard
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

/**
 * Pantalla de lista de materiales escaneados.
 * Muestra todos los materiales capturados y permite navegar a los detalles con Shared Transition.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MaterialesListaScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: InventarioComposeViewModel = viewModel(),
    onBack: () -> Unit,
    onMaterialClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "MATERIALES ESCANEADOS",
                        fontWeight = FontWeight.ExtraBold,
                        color = PremiumTheme.colors.onPrimary,
                        letterSpacing = 1.sp,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Volver",
                            tint = PremiumTheme.colors.onPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar filtro */ }) {
                        Icon(
                            Icons.Default.Warehouse,
                            "Filtrar",
                            tint = PremiumTheme.colors.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremiumTheme.colors.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: Implementar nuevo escaneo */ },
                containerColor = PremiumTheme.colors.secondary,
                contentColor = PremiumTheme.colors.onSecondary
            ) {
                Icon(Icons.Default.Add, "Nuevo material")
            }
        },
        containerColor = PremiumTheme.colors.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Fondo decorativo
            BrandWaves(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(160.dp),
                waveHeight = 0.4f,
                opacityBack = 0.08f,
                opacityFront = 0.15f
            )

            // Lista de materiales
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header con resumen
                item {
                    ResumenHeaderCard(
                        totalMateriales = uiState.materialesEscaneados.size,
                        totalPallets = uiState.materialesEscaneados.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
                    )
                }

                // Lista de materiales
                itemsIndexed(
                    items = uiState.materialesEscaneados,
                    key = { index, material -> materialTransitionKey(material, index) }
                ) { index, material ->
                    val transitionKey = materialTransitionKey(material, index)

                    MaterialCard(
                        item = material,
                        transitionKey = transitionKey,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { onMaterialClick(transitionKey) }
                    )
                }

                // Footer con espacio
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }

            // Estado vacío
            if (uiState.materialesEscaneados.isEmpty()) {
                EmptyState(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

internal fun materialTransitionKey(material: MaterialItem, index: Int): String =
    "material_card_${index}_${material.sku}_${material.ubicacion.orEmpty()}_${material.almacen.orEmpty()}"

/**
 * Tarjeta de resumen en la parte superior
 */
@Composable
private fun ResumenHeaderCard(
    totalMateriales: Int,
    totalPallets: Int
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = PremiumTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "RESUMEN DE CAPTURA",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = PremiumTheme.colors.primary,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResumenItem(
                    label = "Materiales",
                    value = totalMateriales.toString(),
                    icon = Icons.Default.Inventory
                )

                HorizontalDivider(
                    modifier = Modifier.height(40.dp),
                    color = PremiumTheme.colors.divider
                )

                ResumenItem(
                    label = "Total Piezas",
                    value = totalPallets.toString(),
                    icon = Icons.Default.Layers
                )
            }
        }
    }
}

/**
 * Item individual de resumen
 */
@Composable
private fun ResumenItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PremiumTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = PremiumTheme.colors.textPrimary
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = PremiumTheme.colors.textSecondary
        )
    }
}

/**
 * Estado vacío cuando no hay materiales
 */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            tint = PremiumTheme.colors.textSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No hay materiales escaneados",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = PremiumTheme.colors.textSecondary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Presiona el botón + para comenzar a capturar",
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = PremiumTheme.colors.textSecondary.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
