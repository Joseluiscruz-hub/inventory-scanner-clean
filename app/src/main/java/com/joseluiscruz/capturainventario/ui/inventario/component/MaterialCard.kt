package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.R
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.extensions.premiumClickable
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

/**
 * Tarjeta de material con soporte para Shared Element Transition.
 * Esta tarjeta se expandirá fluidamente hacia la pantalla de detalles.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun MaterialCard(
    item: MaterialItem,
    transitionKey: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = transitionKey),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .premiumClickable { onClick() },
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = PremiumTheme.colors.cardBackground
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lado izquierdo: Icono + Información principal
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono del material
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_inventory),
                            contentDescription = null,
                            tint = PremiumTheme.colors.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(Modifier.padding(horizontal = 12.dp))

                    // Información del material
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = item.sku,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = PremiumTheme.colors.textPrimary,
                            maxLines = 1
                        )

                        Text(
                            text = item.descripcion,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = PremiumTheme.colors.textSecondary,
                            maxLines = 2
                        )

                        // Metadata en fila
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MaterialMetadataChip(
                                icon = Icons.Default.LocationOn,
                                label = item.ubicacion ?: "N/A"
                            )

                            item.totalPallets?.let { pallets ->
                                if (pallets.isNotEmpty()) {
                                    MaterialMetadataChip(
                                        icon = Icons.Default.Inventory,
                                        label = "$pallets pzs"
                                    )
                                }
                            }

                            item.tipoTarima?.let { tarima ->
                                if (tarima.isNotEmpty()) {
                                    MaterialMetadataChip(
                                        icon = Icons.Default.Layers,
                                        label = tarima
                                    )
                                }
                            }
                        }
                    }
                }

                // Flecha indicadora
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = PremiumTheme.colors.textSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Chip pequeño para mostrar metadata del material
 */
@Composable
private fun MaterialMetadataChip(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PremiumTheme.colors.primary,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = PremiumTheme.colors.textSecondary
        )
    }
}
