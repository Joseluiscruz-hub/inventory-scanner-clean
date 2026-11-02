package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joseluiscruz.capturainventario.R
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

/**
 * Pantalla de detalles del material con Shared Element Transition.
 * Esta pantalla se expande fluidamente desde la tarjeta en la lista.
 */
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetalleMaterialScreen(
    materialKey: String,
    viewModel: InventarioComposeViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val material = uiState.materialesEscaneados
        .mapIndexed { index, item -> materialTransitionKey(item, index) to item }
        .firstOrNull { (transitionKey, _) -> transitionKey == materialKey }
        ?.second
        ?: MaterialItem(
            sku = materialKey,
            descripcion = "Cargando...",
            cxPal = "",
            fpc = "",
            con = "",
            centro = "",
            linea = "",
            op = "",
            fProd = "",
            diasV = "",
            ubicacion = "",
            totalPallets = "",
            restos = "",
            tipoTarima = "",
            almacen = ""
        )

    with(sharedTransitionScope) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "DETALLE DEL MATERIAL",
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PremiumTheme.colors.primary
                    )
                )
            },
            containerColor = PremiumTheme.colors.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Fondo decorativo con ondas
                BrandWaves(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .height(200.dp),
                    waveHeight = 0.5f,
                    opacityBack = 0.08f,
                    opacityFront = 0.15f
                )

                // Contenido principal con scroll
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(
                                key = materialKey.ifBlank { materialTransitionKey(material, 0) }
                            ),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    color = Color.Transparent
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(Modifier.height(8.dp))

                        // HEADER CON INFORMACIÓN PRINCIPAL
                        DetailHeaderCard(material)

                        Spacer(Modifier.height(16.dp))

                        // SECCIÓN: INFORMACIÓN DE UBICACIÓN
                        DetailSectionCard(
                            title = "📍 Ubicación y Almacenamiento",
                            icon = Icons.Default.LocationOn
                        ) {
                            DetailInfoRow("Almacén", material.almacen ?: "N/A")
                            DetailInfoRow("Ubicación", material.ubicacion ?: "N/A")
                            DetailInfoRow("Centro", material.centro)
                            DetailInfoRow("Línea", material.linea)
                        }

                        Spacer(Modifier.height(12.dp))

                        // SECCIÓN: INFORMACIÓN DE PRODUCTO
                        DetailSectionCard(
                            title = "📦 Información del Producto",
                            icon = Icons.Default.Inventory
                        ) {
                            DetailInfoRow("SKU", material.sku)
                            DetailInfoRow("Descripción", material.descripcion)
                            DetailInfoRow("Cajas x Pallet", material.cxPal)
                            DetailInfoRow("FPC", material.fpc)
                            DetailInfoRow("CON", material.con)
                            DetailInfoRow("OP", material.op)
                        }

                        Spacer(Modifier.height(12.dp))

                        // SECCIÓN: FECHAS Y VENCIMIENTO
                        DetailSectionCard(
                            title = "📅 Fechas y Vencimiento",
                            icon = Icons.Default.Layers
                        ) {
                            DetailInfoRow("Fecha Producción", material.fProd)
                            DetailInfoRow("Días de Vida", material.diasV)
                        }

                        Spacer(Modifier.height(12.dp))

                        // SECCIÓN: CANTIDADES
                        material.totalPallets?.let { pallets ->
                            if (pallets.isNotEmpty()) {
                                DetailSectionCard(
                                    title = "📊 Cantidades",
                                    icon = Icons.Default.Inventory
                                ) {
                                    DetailInfoRow("Total Piezas", pallets)
                                    DetailInfoRow("Restos", material.restos ?: "0")
                                    DetailInfoRow("Tipo Tarima", material.tipoTarima ?: "N/A")
                                }
                                Spacer(Modifier.height(12.dp))
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de cabecera con la información principal del material
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailHeaderCard(
    material: MaterialItem
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = PremiumTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            PremiumTheme.colors.primary,
                            PremiumTheme.colors.primaryVariant
                        )
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono grande
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(12.dp, CircleShape)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_inventory),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(16.dp))

            // SKU
            Text(
                text = material.sku,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(8.dp))

            // Descripción
            Text(
                text = material.descripcion,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(16.dp))

            // Chips de metadata
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                material.ubicacion?.let { ubicacion ->
                    if (ubicacion.isNotEmpty()) {
                        MetadataChip(ubicacion)
                    }
                }

                material.totalPallets?.let { pallets ->
                    if (pallets.isNotEmpty()) {
                        MetadataChip("$pallets pzs")
                    }
                }

                material.tipoTarima?.let { tarima ->
                    if (tarima.isNotEmpty()) {
                        MetadataChip(tarima)
                    }
                }
            }
        }
    }
}

/**
 * Chip pequeño para metadata
 */
@Composable
private fun MetadataChip(label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Tarjeta de sección para información detallada
 */
@Composable
private fun DetailSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = PremiumTheme.colors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Título de sección
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PremiumTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = PremiumTheme.colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Contenido de la sección
            content()
        }
    }
}

/**
 * Fila de información detallada
 */
@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = PremiumTheme.colors.textSecondary
        )

        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = PremiumTheme.colors.textPrimary,
            maxLines = 1
        )
    }
}
