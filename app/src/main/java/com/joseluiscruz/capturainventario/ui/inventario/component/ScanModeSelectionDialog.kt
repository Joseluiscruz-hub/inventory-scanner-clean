package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.ui.inventario.theme.InventarioColors

/**
 * Diálogo de selección de modo de escaneo.
 * 
 * @param onModeSelected Callback cuando se selecciona un modo
 * @param onDismiss Callback cuando se descarta el diálogo
 * @param selectedMode Modo actualmente seleccionado
 */
@Composable
fun ScanModeSelectionDialog(
    onModeSelected: (ScanMode) -> Unit,
    onDismiss: () -> Unit,
    selectedMode: ScanMode = ScanMode.DEFAULT
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Seleccionar Modo de Escaneo")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScanMode.entries.forEach { mode ->
                    Button(
                        onClick = { onModeSelected(mode) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == mode) 
                                InventarioColors.primary else Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (selectedMode == mode) 4.dp else 0.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = mode.displayName,
                                color = Color.White
                            )
                            Text(
                                text = mode.description,
                                color = Color.White.copy(alpha = 0.8f),
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = InventarioColors.secondary
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}
