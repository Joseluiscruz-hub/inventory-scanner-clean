package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.model.Pallet
import com.joseluiscruz.capturainventario.model.Restos
import com.joseluiscruz.capturainventario.model.Rumba
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

// =============================================================================
//  RestosItem — reemplaza RestosAdapter + item_resto.xml
// =============================================================================

/**
 * Composable equivalente a [RestosAdapter].
 * Muestra un registro de Restos con material, lote, cantidad y fecha/hora.
 */
@Composable
fun RestosItem(
    resto: Restos,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                tint = BrandColors.BrandRed,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Material: ${resto.material}",
                    fontWeight = FontWeight.Bold,
                    color = BrandColors.TextPrimary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Lote: ${resto.lote}",
                    color = BrandColors.TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Cant: ${resto.cantidad}  ·  ${resto.fecha} ${resto.hora}",
                    color = BrandColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Lista de Restos — equivalente a un RecyclerView con [RestosAdapter].
 */
@Composable
fun RestosList(
    restos: List<Restos>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(restos, key = { it.id }) { resto ->
            RestosItem(resto = resto)
        }
    }
}

// =============================================================================
//  ScannedDataItem — reemplaza ScannedDataAdapter + item_scanned_data.xml
// =============================================================================

/**
 * Composable equivalente a [ScannedDataAdapter].
 * Acepta tanto [Pallet] como [Rumba] y los muestra con estilos distintos.
 */
@Composable
fun ScannedDataItem(
    item: Any,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = BrandColors.BrandRed,
                modifier = Modifier.size(28.dp)
            )
            when (item) {
                is Rumba -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Material: ${item.material}",
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Lote: ${item.lote}",
                            color = BrandColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Badge(containerColor = BrandColors.BrandRed) {
                        Text(
                            text = "${item.cantidad}",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                is Pallet -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "HU: ${item.hu}",
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                    Badge(containerColor = BrandColors.BrandRed) {
                        Text(
                            text = "${item.cantidad}",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista de elementos escaneados — equivalente a un RecyclerView con [ScannedDataAdapter].
 */
@Composable
fun ScannedDataList(
    items: List<Any>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items) { item ->
            ScannedDataItem(item = item)
        }
    }
}

// =============================================================================
//  DiferenciaItem — reemplaza DiferenciasAdapter + item_diferencia_inventario.xml
// =============================================================================

/**
 * Composable equivalente a [DiferenciasAdapter].
 * Muestra una diferencia de inventario con color semántico según estado.
 */
@Composable
fun DiferenciaItem(
    diferencia: DiferenciaInventario,
    modifier: Modifier = Modifier
) {
    val (barColor, textColor) = when (diferencia.estado) {
        EstadoDiferencia.EXCESO   -> BrandColors.WarningAmber to BrandColors.WarningAmber
        EstadoDiferencia.FALTANTE -> BrandColors.ErrorRed     to BrandColors.ErrorRed
        EstadoDiferencia.COINCIDE -> BrandColors.SuccessGreen to BrandColors.SuccessGreen
        EstadoDiferencia.SIN_REFERENCIA -> Color(0xFF1565C0) to Color(0xFF1565C0)
    }

    val diferenciaTexto = when {
        diferencia.diferencia > 0 -> "+${diferencia.diferencia}"
        diferencia.diferencia < 0 -> "${diferencia.diferencia}"
        else -> "0"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Barra de estado lateral (reemplaza view_indicador_estado)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(barColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = diferencia.codigo,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandColors.TextPrimary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = diferenciaTexto,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 18.sp
                    )
                }
                Text(
                    text = diferencia.descripcion,
                    color = BrandColors.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text("Referencia", color = BrandColors.TextSecondary, fontSize = 11.sp)
                        Text(
                            "${diferencia.cantidadReferencia}",
                            fontWeight = FontWeight.SemiBold,
                            color = BrandColors.TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Column {
                        Text("Actual", color = BrandColors.TextSecondary, fontSize = 11.sp)
                        Text(
                            "${diferencia.cantidadActual}",
                            fontWeight = FontWeight.SemiBold,
                            color = BrandColors.TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lista de diferencias — equivalente a un RecyclerView con [DiferenciasAdapter].
 */
@Composable
fun DiferenciasList(
    diferencias: List<DiferenciaInventario>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(diferencias, key = { "${it.codigo}_${it.timestamp}" }) { diferencia ->
            DiferenciaItem(diferencia = diferencia)
        }
    }
}
