package com.joseluiscruz.capturainventario.ui.inventario.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.ui.inventario.theme.InventarioColors

/**
 * Selector de modos de escaneo con botones tipo toggle.
 * 
 * @param selectedMode Modo actualmente seleccionado
 * @param onModeSelected Callback cuando se selecciona un modo
 * @param modifier Modificador para personalizar layout
 */
@Composable
fun ScanModeSelector(
    selectedMode: ScanMode,
    onModeSelected: (ScanMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val scanModes = listOf(ScanMode.MANUAL, ScanMode.PALLET, ScanMode.RUMBA)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        scanModes.forEach { mode ->
            val isSelected = selectedMode == mode
            Button(
                onClick = { onModeSelected(mode) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) InventarioColors.primary else Color.LightGray,
                    contentColor = if (isSelected) Color.White else Color.DarkGray
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isSelected) 4.dp else 0.dp
                )
            ) {
                Text(
                    text = mode.displayName,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
