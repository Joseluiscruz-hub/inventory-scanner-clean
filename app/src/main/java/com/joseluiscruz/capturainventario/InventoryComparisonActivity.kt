package com.joseluiscruz.capturainventario

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.lifecycleScope
import com.joseluiscruz.capturainventario.ui.theme.EscaneoDeMaterialesDemoTheme
import com.joseluiscruz.capturainventario.viewmodel.InventoryComparisonViewModel
import kotlinx.coroutines.launch

/**
 * Activity principal para la comparación de inventarios.
 * Integra el InventoryComparisonScreen con toda la lógica de importación y comparación.
 */
class InventoryComparisonActivity : AppCompatActivity() {

    private val viewModel: InventoryComparisonViewModel by viewModels()

    // Launcher para importar inventario del sistema
    private val importSistemaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val success = viewModel.importarInventarioSistema(it)
                if (success) {
                    Toast.makeText(
                        this@InventoryComparisonActivity,
                        "Inventario del sistema importado correctamente", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Launcher para importar inventario escaneado
    private val importEscaneadoLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                val success = viewModel.importarInventarioEscaneado(it)
                if (success) {
                    Toast.makeText(
                        this@InventoryComparisonActivity,
                        "Inventario escaneado importado correctamente", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } ?: run {
            // Si no se selecciona archivo, cargar desde SharedPreferences
            lifecycleScope.launch {
                val success = viewModel.importarInventarioEscaneado(null)
                if (success) {
                    Toast.makeText(
                        this@InventoryComparisonActivity,
                        "Inventario escaneado cargado desde el dispositivo", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EscaneoDeMaterialesDemoTheme {
                InventoryComparisonScreenWrapper()
            }
        }
    }

    @Composable
    private fun InventoryComparisonScreenWrapper() {
        // Observar los LiveData del ViewModel
        val inventarioSistema by viewModel.inventarioSistema.observeAsState(emptyMap())
        val inventarioEscaneado by viewModel.inventarioEscaneado.observeAsState(emptyMap())
        val comparacion by viewModel.comparacion.observeAsState(emptyList())
        val mensaje by viewModel.mensaje.observeAsState(null)

        // Estado para controlar si la actualización en tiempo real está activada
        var actualizacionTiempoRealActivada by remember { mutableStateOf(false) }

        InventoryComparisonScreen(
            onImportarSistema = {
                importSistemaLauncher.launch("text/*")
            },
            onImportarEscaneado = {
                // Mostrar opciones: importar desde archivo o cargar desde dispositivo
                showImportEscaneadoOptions()
            },
            onComparar = {
                viewModel.compararInventarios()
            },
            onReiniciarComparacion = {
                viewModel.reiniciarComparacion()
            },
            onLimpiarDatos = {
                mostrarDialogoConfirmacionLimpiarDatos()
            },
            onToggleActualizacionTiempoReal = { activado ->
                actualizacionTiempoRealActivada = activado
                viewModel.habilitarActualizacionTiempoReal(activado)
            },
            actualizacionTiempoRealActivada = actualizacionTiempoRealActivada,
            inventarioSistema = inventarioSistema,
            inventarioEscaneado = inventarioEscaneado,
            comparacion = comparacion,
            diferencia = { sistema, escaneado ->
                viewModel.calcularDiferencia(sistema, escaneado)
            },
            mensaje = mensaje
        )
    }

    /**
     * Muestra opciones para importar inventario escaneado.
     */
    private fun showImportEscaneadoOptions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Importar Inventario Escaneado")
            .setMessage("¿Desde dónde desea cargar el inventario escaneado?")
            .setPositiveButton("Desde archivo CSV") { _, _ ->
                importEscaneadoLauncher.launch("text/*")
            }
            .setNegativeButton("Desde dispositivo") { _, _ ->
                lifecycleScope.launch {
                    val success = viewModel.importarInventarioEscaneado(null)
                    if (success) {
                        Toast.makeText(
                            this@InventoryComparisonActivity,
                            "Inventario cargado desde el dispositivo", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    /**
     * Muestra un diálogo de confirmación antes de limpiar todos los datos.
     */
    private fun mostrarDialogoConfirmacionLimpiarDatos() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("⚠️ Limpiar Todos los Datos")
            .setMessage(
                "¿Estás seguro de que deseas limpiar todos los datos?\n\n" +
                        "Se eliminarán:\n" +
                        "• Inventario del sistema\n" +
                        "• Inventario escaneado\n" +
                        "• Resultados de comparación\n\n" +
                        "Esta acción no se puede deshacer."
            )
            .setPositiveButton("Sí, Limpiar Todo") { _, _ ->
                viewModel.limpiarDatos()
                Toast.makeText(this, "Todos los datos han sido eliminados", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpiar datos al salir si es necesario
        // viewModel.limpiarDatos()
    }
}
