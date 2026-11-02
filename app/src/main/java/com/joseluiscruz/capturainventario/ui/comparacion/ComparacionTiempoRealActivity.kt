package com.joseluiscruz.capturainventario.ui.comparacion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import androidx.lifecycle.lifecycleScope
import com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioComposeActivity
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import com.joseluiscruz.capturainventario.util.FileHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ComparacionTiempoRealActivity : ComponentActivity() {

    private val viewModel: InventarioSharedViewModel by viewModels()

    private var csvDataToSave: String = ""

    private val importSistemaLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            lifecycleScope.launch {
                try {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Descargando archivo desde la nube...",
                        Toast.LENGTH_SHORT
                    ).show()

                    val fileLocal = FileHelper.getFileFromUriSeguro(this@ComparacionTiempoRealActivity, selectedUri)

                    if (fileLocal != null) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Archivo descargado. Procesando inventario...",
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.importarInventarioReferenciaCsvDesdeArchivo(fileLocal)

                        withContext(Dispatchers.IO) {
                            if (fileLocal.exists()) {
                                fileLocal.delete()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Error: No se pudo descargar el archivo de la nube. Revisa tu conexión.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Error inesperado: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private val importAnteriorLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            lifecycleScope.launch {
                try {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Función de importación legacy migrada a Room",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@ComparacionTiempoRealActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private val guardarLocalLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    contentResolver.openOutputStream(destUri)?.use { outputStream ->
                        outputStream.write(csvDataToSave.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "¡Inventario guardado en tu teléfono exitosamente!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Error al guardar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private val descargarNubeParaGuardarLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            lifecycleScope.launch {
                Toast.makeText(this@ComparacionTiempoRealActivity, "Descargando de la nube...", Toast.LENGTH_SHORT).show()

                val fileLocal = FileHelper.getFileFromUriSeguro(this@ComparacionTiempoRealActivity, selectedUri)

                if (fileLocal != null) {
                    Toast.makeText(this@ComparacionTiempoRealActivity, "Limpiando y formateando datos...", Toast.LENGTH_SHORT).show()

                    csvDataToSave = viewModel.generarCsvLimpio(fileLocal)

                    withContext(Dispatchers.IO) { fileLocal.delete() }

                    val dateFormat = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault())
                    val fechaHoy = dateFormat.format(java.util.Date())
                    val nombreArchivoSugerido = "inventario_dia_$fechaHoy.csv"

                    guardarLocalLauncher.launch(nombreArchivoSugerido)

                } else {
                    Toast.makeText(this@ComparacionTiempoRealActivity, "Error al descargar el archivo.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PremiumTheme(darkTheme = true) {
                ComparacionTiempoRealScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onImportarSistema = { importSistemaLauncher.launch("text/*") },
                    onImportarAnterior = { importAnteriorLauncher.launch("text/*") },
                    onIniciarEscaneo = {
                        val intent = Intent(this@ComparacionTiempoRealActivity, InventarioComposeActivity::class.java)
                        startActivity(intent)
                    },
                    onDescargarNube = { descargarNubeParaGuardarLauncher.launch("text/*") },
                    onExportarSAP = {
                        exportarSapLauncher.launch("ajuste_sap_701_702_${System.currentTimeMillis()}.csv")
                    }
                )
            }
        }
    }

    private val exportarSapLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { destUri ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val diferencias = viewModel.comparacion.value.filter { it.diferencia != 0 }
                    val csvData = com.joseluiscruz.capturainventario.util.SapExportHelper.generarAjusteCSV(diferencias, "0001", "Demo1", true)
                    contentResolver.openOutputStream(destUri)?.use { outputStream ->
                        outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Ajuste SAP exportado exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ComparacionTiempoRealActivity,
                            "Error al exportar: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
