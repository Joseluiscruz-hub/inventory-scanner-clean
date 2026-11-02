package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioUiState
import com.joseluiscruz.capturainventario.ui.inventario.theme.InventarioColors
import com.joseluiscruz.capturainventario.ui.theme.EscaneoDeMaterialesDemoTheme

/**
 * Previews para la pantalla de Inventario.
 * 
 * Estos previews permiten visualizar la UI en diferentes estados y modos
 * directamente en Android Studio sin necesidad de compilar ni ejecutar.
 */

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_Default() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.DEFAULT,
            skuManual = "",
            ubicacion = "",
            totalPallets = "",
            restos = ""
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_Rumba() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.RUMBA,
            skuManual = "SKU123456",
            ubicacion = "A-01-02",
            totalPallets = "5",
            restos = "",
            isSkuManualEnabled = false,
            isRestosEnabled = false,
            showScannedFields = false
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_Pallet() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.PALLET,
            skuManual = "",
            ubicacion = "B-02-03",
            totalPallets = "1",
            restos = "",
            isSkuManualEnabled = false,
            isTotalPalletsEnabled = false,
            isRestosEnabled = false,
            showScannedFields = true,
            selectedAlmacen = "Almacén Central"
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_Manual() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.MANUAL,
            skuManual = "SKU789012",
            ubicacion = "C-03-04",
            totalPallets = "3",
            restos = "2",
            isSkuManualEnabled = true,
            isTotalPalletsEnabled = true,
            isRestosEnabled = true,
            showScannedFields = true
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_Loading() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.DEFAULT,
            isLoading = true,
            isSaving = true
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_DarkMode() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.DEFAULT,
            skuManual = "SKU123",
            ubicacion = "A-01",
            totalPallets = "5"
        ),
        darkTheme = true
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun InventarioScreenPreview_WithData() {
    InventarioScreenPreviewImpl(
        state = InventarioUiState(
            scanMode = ScanMode.DEFAULT,
            skuManual = "SKU123456789",
            ubicacion = "A-01-02-03",
            totalPallets = "10",
            restos = "5",
            totalCajas = 150,
            totalPalletsEscaneados = 25,
            selectedAlmacen = "Almacén Norte",
            showScannedFields = true
        )
    )
}

/**
 * Implementación base para los previews.
 */
@Composable
private fun InventarioScreenPreviewImpl(
    state: InventarioUiState,
    darkTheme: Boolean = false
) {
    EscaneoDeMaterialesDemoTheme(
        darkTheme = darkTheme
    ) {
        Surface(
            color = if (darkTheme) InventarioColors.Dark.background else InventarioColors.background
        ) {
            // Nota: Este preview muestra el estado, pero no es interactivo
            // Para pruebas interactivas, usar el emulador
            InventarioScreenReadOnly(state = state)
        }
    }
}

/**
 * Versión de solo lectura del screen para previews.
 * No tiene ViewModel ni interacción real.
 */
@Composable
private fun InventarioScreenReadOnly(
    state: InventarioUiState
) {
    // Implementación simplificada que solo muestra el estado visual
    // La implementación completa está en InventarioScreen.kt
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Preview: ${state.scanMode.displayName} Mode",
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "SKU: ${state.skuManual}",
            style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        Text(
            text = "Ubicación: ${state.ubicacion}",
            style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        Text(
            text = "Pallets: ${state.totalPallets}",
            style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )
        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }
}
