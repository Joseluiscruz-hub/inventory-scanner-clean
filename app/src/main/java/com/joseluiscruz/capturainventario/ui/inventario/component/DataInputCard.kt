package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioUiState
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataInputCard(
    state: InventarioUiState,
    onSkuManualChange: (String) -> Unit,
    onUbicacionChange: (String) -> Unit,
    onTotalPalletsChange: (String) -> Unit,
    onRestosChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Assuming SectionCard is defined elsewhere or needs to be replaced/adapted
    // For now, using a Column as placeholder if SectionCard is not available in context
    // But based on previous context, SectionCard might be a custom component.
    // If SectionCard is missing, we should probably use a Card or similar.
    // However, the error was about outlinedTextFieldColors.

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SKU Manual
        OutlinedTextField(
            value = state.skuManual,
            onValueChange = onSkuManualChange,
            label = { Text("🏷️ SKU Manual") },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isSkuManualEnabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BrandColors.Surface0,
                unfocusedContainerColor = BrandColors.Surface0,
                disabledContainerColor = BrandColors.Surface0,
                focusedIndicatorColor = BrandColors.BrandRed,
                unfocusedIndicatorColor = BrandColors.Surface3,
                focusedLabelColor = BrandColors.BrandRed,
                unfocusedLabelColor = BrandColors.TextSecondary,
                disabledIndicatorColor = BrandColors.Surface3,
                disabledTextColor = BrandColors.TextHint
            )
        )

        // Ubicación
        OutlinedTextField(
            value = state.ubicacion,
            onValueChange = onUbicacionChange,
            label = { Text("📍 Ubicación") },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isUbicacionEnabled,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = BrandColors.Surface0,
                unfocusedContainerColor = BrandColors.Surface0,
                disabledContainerColor = BrandColors.Surface0,
                focusedIndicatorColor = BrandColors.BrandRed,
                unfocusedIndicatorColor = BrandColors.Surface3,
                focusedLabelColor = BrandColors.BrandRed,
                unfocusedLabelColor = BrandColors.TextSecondary
            )
        )

        // Total Pallets y Restos en una fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.totalPallets,
                onValueChange = onTotalPalletsChange,
                label = { Text("📦 Total Pallets") },
                modifier = Modifier.weight(1f),
                enabled = state.isTotalPalletsEnabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface0,
                    unfocusedContainerColor = BrandColors.Surface0,
                    disabledContainerColor = BrandColors.Surface0,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = BrandColors.Surface3,
                    focusedLabelColor = BrandColors.BrandRed,
                    unfocusedLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = state.restos,
                onValueChange = onRestosChange,
                label = { Text("📊 Restos") },
                modifier = Modifier.weight(1f),
                enabled = state.isRestosEnabled,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = BrandColors.Surface0,
                    unfocusedContainerColor = BrandColors.Surface0,
                    disabledContainerColor = BrandColors.Surface0,
                    focusedIndicatorColor = BrandColors.BrandRed,
                    unfocusedIndicatorColor = BrandColors.Surface3,
                    focusedLabelColor = BrandColors.BrandRed,
                    unfocusedLabelColor = BrandColors.TextSecondary
                )
            )
        }
    }
}
