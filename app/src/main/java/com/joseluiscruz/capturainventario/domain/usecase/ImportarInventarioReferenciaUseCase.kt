package com.joseluiscruz.capturainventario.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.joseluiscruz.capturainventario.util.CsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject

class ImportarInventarioReferenciaUseCase @Inject constructor(
    private val manager: InventarioTiempoRealManager,
    private val firebaseRepository: FirebaseRepository,
    private val filesDir: File // Passed via DI or constructor
) {
    suspend operator fun invoke(
        uri: Uri,
        contentResolver: ContentResolver
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val listaReferencia = mutableListOf<InventarioTiempoRealManager.ItemReferencia>()

            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    val header = reader.readLine()
                    if (header == null) return@withContext Result.failure(Exception("El archivo está vacío"))
                    if (CsvParser.looksLikeSpreadsheetBinary(header)) {
                        return@withContext Result.failure(Exception(EXCEL_BINARY_ERROR))
                    }

                    var invalidRows = 0
                    reader.forEachLine { line ->
                        if (line.isNotBlank()) {
                            val item = CsvParser.parseReferenciaCsvLine(line)
                            if (item != null) {
                                listaReferencia.add(item)
                            } else {
                                invalidRows++
                            }
                        }
                    }
                    if (listaReferencia.isEmpty() && invalidRows > 0) {
                        return@withContext Result.failure(Exception("No se encontraron registros válidos. $EXCEL_BINARY_ERROR"))
                    }
                }
            } ?: return@withContext Result.failure(Exception("No se pudo abrir el archivo CSV"))

            if (listaReferencia.isNotEmpty()) {
                manager.cargarInventarioSistema(listaReferencia)
                
                // Cargar configuraciones dinámicas
                val configs = listaReferencia.mapNotNull { referencia ->
                    val cajas = referencia.cajasXPallet.takeIf { it > 0 } 
                        ?: referencia.armado.toIntOrNull() 
                        ?: referencia.cantidad
                    if (referencia.codigo.isBlank() || cajas <= 0) return@mapNotNull null
                    ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                        sku = referencia.codigo,
                        cajasXPallet = cajas,
                        tarimaPlasticaSku = referencia.tarimaPlasticaSku.ifBlank { ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR }
                    )
                }
                ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(configs)

                // Persistencia legacy
                saveToLegacyFile(listaReferencia)

                // Sincronización Firebase
                firebaseRepository.subirInventarioReferencia(listaReferencia) { _, _ -> }

                Result.success("Se importaron ${listaReferencia.size} registros de referencia")
            } else {
                Result.failure(Exception("No se encontraron registros válidos en el archivo"))
            }
        } catch (e: Exception) {
            Log.e("ImportarUseCase", "Error importando CSV: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun saveToLegacyFile(lista: List<InventarioTiempoRealManager.ItemReferencia>) {
        try {
            val file = File(filesDir, "inventario_almacen.csv")
            val sb = StringBuilder("SKU,Descripcion,Cantidad,CajasXPallet,Armado,TarimaPlasticaSku\n")
            lista.forEach { item ->
                sb.append(
                    listOf(
                        item.codigo,
                        item.descripcion,
                        item.cantidad,
                        item.cajasXPallet,
                        item.armado,
                        item.tarimaPlasticaSku
                    ).joinToString(",")
                ).append('\n')
            }
            file.writeText(sb.toString())
        } catch (e: Exception) {
            Log.e("ImportarUseCase", "Error guardando legacy: ${e.message}")
        }
    }

    private companion object {
        const val EXCEL_BINARY_ERROR =
            "El archivo parece ser Excel (.xlsx) o binario; expórtalo como CSV UTF-8 antes de importarlo."
    }
}
