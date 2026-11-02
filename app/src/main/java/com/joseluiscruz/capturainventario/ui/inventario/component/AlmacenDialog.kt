package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.joseluiscruz.capturainventario.ui.inventario.theme.InventarioColors

/**
 * Diálogo de selección de almacén.
 * 
 * @param almacenes Lista de almacenes disponibles
 * @param selectedAlmacen Almacén actualmente seleccionado
 * @param onAlmacenSelected Callback cuando se selecciona un almacén
 * @param onDismiss Callback cuando se descarta el diálogo
 */
@Composable
fun AlmacenDialog(
    almacenes: List<String>,
    selectedAlmacen: String,
    onAlmacenSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = onDismiss,
        title = {
            Text("Seleccionar Almacén")
        },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                almacenes.forEach { almacen ->
                    Button(
                        onClick = { onAlmacenSelected(almacen) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedAlmacen == almacen) 
                                InventarioColors.primary else Color.Gray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (selectedAlmacen == almacen) 4.dp else 0.dp
                        )
                    ) {
                        Text(
                            text = almacen,
                            color = Color.White
                        )
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
