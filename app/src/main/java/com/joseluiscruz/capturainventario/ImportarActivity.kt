package com.joseluiscruz.capturainventario

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.io.BufferedReader
import java.io.InputStreamReader
import com.joseluiscruz.capturainventario.util.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import android.util.Log

class ImportarActivity : AppCompatActivity() {

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                processCSVFile(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_importar)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<CardView>(R.id.card_import_file).setOnClickListener {
            openFilePicker()
        }

        findViewById<CardView>(R.id.card_import_system).setOnClickListener {
            importFromSystem()
        }

        findViewById<CardView>(R.id.card_import_backup).setOnClickListener {
            restoreFromBackup()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(Intent.createChooser(intent, "Seleccionar archivo CSV"))
    }

    private fun processCSVFile(uri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. OBTENER EL ARCHIVO LOCALMENTE
                val fileLocal = FileHelper.getFileFromUriSeguro(this@ImportarActivity, uri)

                if (fileLocal == null) {
                    Toast.makeText(this@ImportarActivity, "No se pudo descargar el archivo de la nube", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. LEER EL ARCHIVO LOCAL
                val lines = withContext(Dispatchers.IO) {
                    val inputStream = FileInputStream(fileLocal)
                    // Usar ISO-8859-1 o UTF-8
                    val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                    
                    val fileLines = reader.readLines()
                    
                    reader.close()
                    inputStream.close()
                    
                    // Borrar el archivo caché cuando termines para no ocupar espacio
                    fileLocal.delete()
                    
                    fileLines
                }

                if (lines.isEmpty()) {
                    Toast.makeText(this@ImportarActivity, "El archivo está vacío", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // Mostrar diálogo de confirmación con preview
                val preview = if (lines.size > 3) {
                    lines.take(3).joinToString("\n") + "\n... (${lines.size} líneas totales)"
                } else {
                    lines.joinToString("\n")
                }

                AlertDialog.Builder(this@ImportarActivity)
                    .setTitle("Confirmar importación")
                    .setMessage("Se encontraron ${lines.size} líneas.\n\nPreview:\n$preview\n\n¿Deseas continuar?")
                    .setPositiveButton("Importar") { _, _ ->
                        performCSVImport(lines)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()

            } catch (e: Exception) {
                Log.e("Importar", "Error procesando CSV", e)
                Toast.makeText(this@ImportarActivity, "Error al leer el archivo: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun performCSVImport(lines: List<String>) {
        try {
            // Simular procesamiento de importación
            Toast.makeText(this, "Procesando importación...", Toast.LENGTH_SHORT).show()

            Thread {
                // Simular tiempo de procesamiento
                Thread.sleep(2000)

                runOnUiThread {
                    val successCount = lines.size - 1 // Assumir primera línea es header
                    AlertDialog.Builder(this)
                        .setTitle("Importación completada")
                        .setMessage("Se importaron exitosamente $successCount registros.")
                        .setPositiveButton("OK") { _, _ ->
                            finish() // Volver al dashboard
                        }
                        .show()
                }
            }.start()

        } catch (e: Exception) {
            Toast.makeText(this, "Error durante la importación: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importFromSystem() {
        AlertDialog.Builder(this)
            .setTitle("Importar desde sistema")
            .setMessage("Esta función sincronizará los datos más recientes desde el sistema central. ¿Continuar?")
            .setPositiveButton("Sincronizar") { _, _ ->
                performSystemSync()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performSystemSync() {
        Toast.makeText(this, "Sincronizando con sistema central...", Toast.LENGTH_SHORT).show()

        Thread {
            // Simular sincronización
            Thread.sleep(3000)

            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Sincronización completada")
                    .setMessage("Se sincronizaron exitosamente los datos del sistema central.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }.start()
    }

    private fun restoreFromBackup() {
        // Obtener lista de respaldos disponibles (simulado)
        val backups = listOf(
            "Respaldo_2024_01_15.backup",
            "Respaldo_2024_01_10.backup",
            "Respaldo_2024_01_05.backup"
        )

        if (backups.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No hay respaldos")
                .setMessage("No se encontraron respaldos disponibles para restaurar.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Seleccionar respaldo")
            .setItems(backups.toTypedArray()) { _, which ->
                val selectedBackup = backups[which]
                confirmRestore(selectedBackup)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmRestore(backupName: String) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar restauración")
            .setMessage("¿Estás seguro de que deseas restaurar desde '$backupName'?\n\nEsta acción sobrescribirá los datos actuales.")
            .setPositiveButton("Restaurar") { _, _ ->
                performRestore(backupName)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performRestore(backupName: String) {
        Toast.makeText(this, "Restaurando desde $backupName...", Toast.LENGTH_SHORT).show()

        Thread {
            // Simular restauración
            Thread.sleep(2500)

            runOnUiThread {
                AlertDialog.Builder(this)
                    .setTitle("Restauración completada")
                    .setMessage("Los datos se restauraron exitosamente desde '$backupName'.")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
