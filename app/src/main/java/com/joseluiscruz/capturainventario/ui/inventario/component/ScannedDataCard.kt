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
import com.joseluiscruz.capturainventario.ui.inventario.state.ScannedData
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannedDataCard(
    scannedData: ScannedData,
    modifier: Modifier = Modifier,
    showFields: Boolean = true
) {
    if (!showFields) return
    
    // Assuming SectionCard is defined elsewhere or needs to be replaced/adapted
    // For now, using a Column as placeholder if SectionCard is not available in context
    // But based on previous context, SectionCard might be a custom component.
    // If SectionCard is missing, we should probably use a Card or similar.
    // However, the error was about outlinedTextFieldColors.

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Fila 1: SKU y DP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = scannedData.sku,
                onValueChange = { },
                label = { Text("🏷️ SKU") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = scannedData.dp,
                onValueChange = { },
                label = { Text("📦 DP") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )
        }

        // Fila 2: CxPal y FPC
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = scannedData.cxPal,
                onValueChange = { },
                label = { Text("📦 CxPal") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = scannedData.fpc,
                onValueChange = { },
                label = { Text("📅 FPC") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )
        }

        // Fila 3: Con y Centro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = scannedData.con,
                onValueChange = { },
                label = { Text("🔗 Con") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = scannedData.centro,
                onValueChange = { },
                label = { Text("🏢 Centro") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )
        }

        // Fila 4: LINEA y OP
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = scannedData.linea,
                onValueChange = { },
                label = { Text("📋 LINEA") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = scannedData.op,
                onValueChange = { },
                label = { Text("⚙️ OP") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )
        }

        // Fila 5: FProd y Dias V
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = scannedData.fProd,
                onValueChange = { },
                label = { Text("📅 FProd") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )

            OutlinedTextField(
                value = scannedData.diasV,
                onValueChange = { },
                label = { Text("⏱️ Dias V") },
                enabled = false,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    disabledContainerColor = BrandColors.Surface0,
                    disabledIndicatorColor = BrandColors.Surface3,
                    disabledTextColor = BrandColors.TextPrimary,
                    disabledLabelColor = BrandColors.TextSecondary
                )
            )
        }
    }
}
