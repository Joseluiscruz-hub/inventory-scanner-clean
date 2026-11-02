package com.joseluiscruz.capturainventario.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.joseluiscruz.capturainventario.dashboard.InventarioAlmacen
import com.joseluiscruz.capturainventario.domain.model.ErrorCode
import com.joseluiscruz.capturainventario.domain.model.OperationResult
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.Constants
import com.joseluiscruz.capturainventario.util.CsvSanitizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Servicio centralizado para operaciones CSV de inventario.
 *
 * Reemplaza la lógica dispersa en CapturaInventarioViewModel con
 * funciones puras que retornan OperationResult<T> en vez de callbacks.
 *
 * Beneficios:
 * - Testable sin AndroidViewModel
 * - Thread-safe con Mutex
 * - Errores tipados con ErrorCode
 * - Cacheo inteligente por lastModified
 */
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class CsvInventarioService @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        // También protege las instancias creadas por código legacy fuera de Hilt.
        private val csvMutex = Mutex()
    }
    private var cachedMateriales: List<MaterialItem>? = null
    private var lastModifiedMateriales: Long = 0L

    // ================================================================
    // LECTURA
    // ================================================================

    /**
     * Obtiene todos los materiales escaneados desde el CSV local.
     */
    suspend fun obtenerMaterialesEscaneados(): OperationResult<List<MaterialItem>> =
        withContext(Dispatchers.IO) {
            OperationResult.runCatching {
                val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                if (!file.exists()) return@runCatching emptyList()

                val currentModified = file.lastModified()
                if (cachedMateriales != null && currentModified == lastModifiedMateriales) {
                    return@runCatching cachedMateriales!!
                }

                val lines = file.readLines()
                if (lines.size <= 1) {
                    cachedMateriales = emptyList()
                    lastModifiedMateriales = currentModified
                    return@runCatching emptyList()
                }

                val lista = mutableListOf<MaterialItem>()
                for (i in 1 until lines.size) {
                    val cols = parseCsvLine(lines[i])
                    if (cols.isNotEmpty() && cols[0].isNotBlank()) {
                        lista.add(
                            MaterialItem(
                                sku = cols[0],
                                descripcion = cols.getOrNull(1) ?: "",
                                cxPal = cols.getOrNull(2) ?: "",
                                fpc = cols.getOrNull(3) ?: "",
                                con = cols.getOrNull(4) ?: "",
                                centro = cols.getOrNull(5) ?: "",
                                linea = cols.getOrNull(6) ?: "",
                                op = cols.getOrNull(7) ?: "",
                                fProd = cols.getOrNull(8) ?: "",
                                diasV = cols.getOrNull(9) ?: "",
                                ubicacion = cols.getOrNull(10) ?: "",
                                totalPallets = cols.getOrNull(11) ?: "0",
                                tipoTarima = cols.getOrNull(12) ?: "",
                                almacen = cols.getOrNull(13) ?: "",
                                restos = cols.getOrNull(14) ?: ""
                            )
                        )
                    }
                }
                cachedMateriales = lista
                lastModifiedMateriales = currentModified
                lista
            }
        }

    /**
     * Obtiene el inventario de almacén desde CSV.
     */
    suspend fun obtenerInventarioAlmacen(): OperationResult<List<InventarioAlmacen>> =
        withContext(Dispatchers.IO) {
            OperationResult.runCatching {
                val file = File(context.filesDir, "inventario_almacen.csv")
                if (!file.exists()) return@runCatching emptyList()

                val lines = file.readLines()
                if (lines.size <= 1) return@runCatching emptyList()

                lines.drop(1).mapNotNull { line ->
                    val cols = parseCsvLine(line)
                    if (cols.size >= 4) {
                        InventarioAlmacen(
                            sku = cols[0],
                            descripcion = cols[1],
                            disponible = cols[2].toIntOrNull() ?: 0,
                            centro = cols[3]
                        )
                    } else null
                }
            }
        }

    /**
     * Calcula la cantidad de pallets escaneados en un almacén en específico.
     */
    suspend fun obtenerTotalPalletsPorAlmacen(almacen: String): Int =
        withContext(Dispatchers.IO) {
            val result = obtenerMaterialesEscaneados()
            val materiales = result.getOrNull() ?: emptyList()
            materiales.filter { it.almacen == almacen }
                .sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        }

    // ================================================================
    // ESCRITURA
    // ================================================================

    /**
     * Actualiza un material específico en el archivo CSV de forma atómica.
     */
    suspend fun actualizarMaterial(original: MaterialItem, nuevo: MaterialItem): OperationResult<Unit> =
        withContext(Dispatchers.IO) {
            csvMutex.withLock {
                OperationResult.runCatching {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (!file.exists()) {
                        throw Exception("Archivo no encontrado")
                    }

                    val lines = file.readLines().toMutableList()
                    if (lines.isNotEmpty()) lines[0] = Constants.CSV_HEADER
                    val dataLines = lines.drop(1)

                    val indexToUpdate = dataLines.indexOfFirst { line ->
                        val cols = parseCsvLine(line)
                        cols.getOrNull(0) == original.sku &&
                                cols.getOrNull(10) == (original.ubicacion ?: "") &&
                                cols.getOrNull(11) == (original.totalPallets ?: "") &&
                                cols.getOrNull(13) == (original.almacen ?: "")
                    }

                    if (indexToUpdate != -1) {
                        // Construir la nueva línea CSV
                        val newLine = listOf(
                            escapeForCsv(nuevo.sku),
                            escapeForCsv(nuevo.descripcion),
                            escapeForCsv(nuevo.cxPal),
                            escapeForCsv(nuevo.fpc),
                            escapeForCsv(nuevo.con),
                            escapeForCsv(nuevo.centro),
                            escapeForCsv(nuevo.linea),
                            escapeForCsv(nuevo.op),
                            escapeForCsv(nuevo.fProd),
                            escapeForCsv(nuevo.diasV),
                            escapeForCsv(nuevo.ubicacion ?: ""),
                            nuevo.totalPallets ?: "",
                            escapeForCsv(nuevo.tipoTarima ?: ""),
                            escapeForCsv(nuevo.almacen ?: ""),
                            nuevo.restos ?: ""
                        ).joinToString(",")

                        lines[indexToUpdate + 1] = newLine
                        writeAtomically(file, lines.joinToString("\n"))

                        cachedMateriales = null
                        Unit
                    } else {
                        throw Exception("Registro original no encontrado")
                    }
                }
            }
        }

    /**
     * Guarda o acumula un material en el CSV, thread-safe.
     */
    suspend fun guardarMaterial(material: MaterialItem): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            if (material.sku.isBlank()) {
                return@withContext OperationResult.Error(
                    "El SKU no puede estar vacío",
                    ErrorCode.VALIDATION_ERROR
                )
            }

            val pallets = material.totalPallets?.toIntOrNull() ?: 0
            val restos = material.restos?.toIntOrNull() ?: 0
            if (pallets < 0 || restos < 0) {
                return@withContext OperationResult.Error(
                    "Las cantidades no pueden ser negativas",
                    ErrorCode.VALIDATION_ERROR
                )
            }
            if (pallets == 0 && restos == 0) {
                return@withContext OperationResult.Error(
                    "El registro debe contener pallets o restos",
                    ErrorCode.VALIDATION_ERROR
                )
            }

            csvMutex.withLock {
                OperationResult.runCatching {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    val allLines = if (file.exists()) {
                        file.readLines().toMutableList()
                    } else {
                        mutableListOf()
                    }

                    if (allLines.isEmpty()) {
                        allLines.add(Constants.CSV_HEADER)
                    } else if (!allLines[0].startsWith("SKU")) {
                        allLines.add(0, Constants.CSV_HEADER)
                    } else {
                        // Migra de forma compatible el encabezado anterior de 14 columnas.
                        allLines[0] = Constants.CSV_HEADER
                    }

                    // Para cumplir con el requerimiento "1 escaneo 1 fila", desactivamos la acumulación.
                    // Cada escaneo se registra como una fila independiente en el CSV.
                    val row = listOf(
                        escapeForCsv(material.sku),
                        escapeForCsv(material.descripcion),
                        escapeForCsv(material.cxPal),
                        escapeForCsv(material.fpc),
                        escapeForCsv(material.con),
                        escapeForCsv(material.centro),
                        escapeForCsv(material.linea),
                        escapeForCsv(material.op),
                        escapeForCsv(material.fProd),
                        escapeForCsv(material.diasV),
                        escapeForCsv(material.ubicacion ?: ""),
                        material.totalPallets ?: "",
                        escapeForCsv(material.tipoTarima ?: ""),
                        escapeForCsv(material.almacen ?: ""),
                        material.restos ?: ""
                    )
                    allLines.add(row.joinToString(","))
                    val acumulado = pallets // En este modo, el retorno es la cantidad del escaneo actual

                    writeAtomically(file, allLines.joinToString("\n"))
                    cachedMateriales = null
                    acumulado // Return pallets of the current scan
                }
            }
        }

    /**
     * Reconstruye la copia CSV desde el estado consolidado de Room.
     * El archivo queda como proyección reemplazable, no como fuente de verdad.
     */
    suspend fun reemplazarInventario(materiales: List<MaterialItem>): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            csvMutex.withLock {
                OperationResult.runCatching {
                    val content = buildString {
                        appendLine(Constants.CSV_HEADER)
                        materiales.forEach { material ->
                            appendLine(
                                listOf(
                                    escapeForCsv(material.sku),
                                    escapeForCsv(material.descripcion),
                                    escapeForCsv(material.cxPal),
                                    escapeForCsv(material.fpc),
                                    escapeForCsv(material.con),
                                    escapeForCsv(material.centro),
                                    escapeForCsv(material.linea),
                                    escapeForCsv(material.op),
                                    escapeForCsv(material.fProd),
                                    escapeForCsv(material.diasV),
                                    escapeForCsv(material.ubicacion.orEmpty()),
                                    material.totalPallets ?: "0",
                                    escapeForCsv(material.tipoTarima.orEmpty()),
                                    escapeForCsv(material.almacen.orEmpty()),
                                    material.restos ?: "0"
                                ).joinToString(",")
                            )
                        }
                    }
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    writeAtomically(file, content.trimEnd())
                    cachedMateriales = null
                    materiales.size
                }
            }
        }

    /**
     * Elimina un material específico del archivo CSV de forma atómica.
     */
    suspend fun eliminarMaterial(material: MaterialItem): OperationResult<Unit> =
        withContext(Dispatchers.IO) {
            csvMutex.withLock {
                OperationResult.runCatching {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (!file.exists()) return@runCatching

                    val lines = file.readLines().toMutableList()
                    val dataLines = lines.drop(1)

                    val indexToRemove = dataLines.indexOfFirst { line ->
                        val cols = parseCsvLine(line)
                        // Coincidencia exacta por SKU, Ubicación, Almacén y TotalPallets para seguridad
                        cols.getOrNull(0) == material.sku &&
                                cols.getOrNull(10) == (material.ubicacion ?: "") &&
                                cols.getOrNull(11) == (material.totalPallets ?: "0") &&
                                cols.getOrNull(13) == (material.almacen ?: "")
                    }

                    if (indexToRemove != -1) {
                        lines.removeAt(indexToRemove + 1)
                        writeAtomically(file, lines.joinToString("\n"))
                        cachedMateriales = null
                    } else {
                        throw Exception("No se encontró el registro para eliminar")
                    }
                }
            }
        }

    /**
     * Importa un CSV externo como inventario local.
     */
    suspend fun importarCsv(uri: Uri): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            csvMutex.withLock {
                OperationResult.runCatching {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw IOException("No se pudo abrir el archivo")
                    val content = inputStream.bufferedReader().use { it.readText() }
                    if (content.isBlank()) throw IllegalArgumentException("El archivo está vacío")

                    val lines = content.lines().filter { it.isNotBlank() }
                    if (lines.isEmpty()) throw IllegalArgumentException("El archivo está vacío")

                    val header = parseCsvLine(lines.first().removePrefix("\uFEFF"))
                    if (header.size < 14 || !header.first().equals("SKU", ignoreCase = true)) {
                        throw IllegalArgumentException(
                            "Encabezado inválido: se esperaban al menos 14 columnas y SKU al inicio"
                        )
                    }

                    lines.drop(1).forEachIndexed { index, line ->
                        val columns = parseCsvLine(line)
                        if (columns.size < 14 || columns[0].isBlank()) {
                            throw IllegalArgumentException("Fila ${index + 2} inválida")
                        }
                        if ((columns[11].toIntOrNull() ?: -1) < 0) {
                            throw IllegalArgumentException(
                                "TotalPallets inválido en la fila ${index + 2}"
                            )
                        }
                        val restos = columns.getOrNull(14)
                        if (!restos.isNullOrBlank() && (restos.toIntOrNull() ?: -1) < 0) {
                            throw IllegalArgumentException("Restos inválidos en la fila ${index + 2}")
                        }
                    }

                    val normalizedLines = lines.toMutableList()
                    normalizedLines[0] = Constants.CSV_HEADER
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    writeAtomically(file, normalizedLines.joinToString("\n"))
                    cachedMateriales = null
                    normalizedLines.size - 1
                }
            }
        }

    /**
     * Resetea el inventario local.
     */
    suspend fun resetearInventario(): OperationResult<Unit> =
        withContext(Dispatchers.IO) {
            csvMutex.withLock {
                OperationResult.runCatching {
                    val file = File(context.filesDir, Constants.INVENTORY_FILE_NAME)
                    if (file.exists() && !file.delete()) {
                        throw IOException("No se pudo eliminar el inventario local")
                    }
                    cachedMateriales = null
                    lastModifiedMateriales = 0L
                }
            }
        }

    // ================================================================
    // EXPORTACIÓN
    // ================================================================

    /**
     * Exporta materiales escaneados como CSV a la URI indicada.
     */
    suspend fun exportarCsv(uri: Uri): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            val materialesResult = obtenerMaterialesEscaneados()
            val materiales = materialesResult.getOrNull()
                ?: return@withContext OperationResult.Error(
                    "No se pudieron obtener los materiales",
                    ErrorCode.FILE_READ_ERROR
                )
            exportarMaterialesCsv(uri, materiales)
        }

    /**
     * Escribe [materiales] en [uri] con el mismo formato que [exportarCsv].
     * Permite exportar una fuente distinta al espejo CSV (p. ej. los registros de Room antes de
     * una "Nueva toma", que son exactamente los que se van a borrar).
     */
    suspend fun exportarMaterialesCsv(uri: Uri, materiales: List<MaterialItem>): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            if (materiales.isEmpty()) {
                return@withContext OperationResult.Error(
                    "No hay inventario para exportar",
                    ErrorCode.NOT_FOUND
                )
            }

            OperationResult.runCatching {
                val content = buildString {
                    appendLine(Constants.CSV_HEADER)
                    materiales.forEach { m ->
                        appendLine(
                            listOf(
                                escapeForSpreadsheetCsv(m.sku),
                                escapeForSpreadsheetCsv(m.descripcion),
                                escapeForSpreadsheetCsv(m.cxPal),
                                escapeForSpreadsheetCsv(m.fpc),
                                escapeForSpreadsheetCsv(m.con),
                                escapeForSpreadsheetCsv(m.centro),
                                escapeForSpreadsheetCsv(m.linea),
                                escapeForSpreadsheetCsv(m.op),
                                escapeForSpreadsheetCsv(m.fProd),
                                escapeForSpreadsheetCsv(m.diasV),
                                escapeForSpreadsheetCsv(m.ubicacion ?: ""),
                                m.totalPallets ?: "0",
                                escapeForSpreadsheetCsv(m.tipoTarima ?: ""),
                                escapeForSpreadsheetCsv(m.almacen ?: ""),
                                m.restos ?: "0"
                            ).joinToString(",")
                        )
                    }
                }

                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("No se pudo abrir el destino de exportación")
                outputStream.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                materiales.size
            }
        }

    /**
     * Exporta el inventario a formato PDF corporativo.
     */
    suspend fun exportarPdf(uri: Uri): OperationResult<Int> =
        withContext(Dispatchers.IO) {
            val materialesResult = obtenerMaterialesEscaneados()
            val materiales = materialesResult.getOrNull()
                ?: return@withContext OperationResult.Error(
                    "No se pudieron obtener los materiales",
                    ErrorCode.FILE_READ_ERROR
                )

            if (materiales.isEmpty()) {
                return@withContext OperationResult.Error(
                    "No hay inventario para exportar",
                    ErrorCode.NOT_FOUND
                )
            }

            OperationResult.runCatching {
                val pdfDocument = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                var page = pdfDocument.startPage(pageInfo)
                var canvas = page.canvas
                val paint = android.graphics.Paint()
                val titlePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 18f
                    isFakeBoldText = true
                }
                val headerPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.DKGRAY
                    textSize = 12f
                    isFakeBoldText = true
                }
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 10f
                }

                var yPos = 40f
                canvas.drawText("Reporte de Inventario - PLANTA DE MANUFACTURA", 50f, yPos, titlePaint)
                yPos += 30f

                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())
                canvas.drawText("Fecha: ${sdf.format(java.util.Date())}", 50f, yPos, textPaint)
                yPos += 40f

                canvas.drawText("SKU", 50f, yPos, headerPaint)
                canvas.drawText("Almacén", 150f, yPos, headerPaint)
                canvas.drawText("Pallets", 450f, yPos, headerPaint)
                yPos += 20f
                canvas.drawLine(50f, yPos - 10f, 545f, yPos - 10f, paint)

                for (material in materiales) {
                    if (yPos > 800) {
                        pdfDocument.finishPage(page)
                        val nextPageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                        page = pdfDocument.startPage(nextPageInfo)
                        canvas = page.canvas
                        yPos = 40f
                    }

                    canvas.drawText(material.sku, 50f, yPos, textPaint)
                    canvas.drawText(material.almacen ?: "N/A", 150f, yPos, textPaint)
                    canvas.drawText(material.totalPallets ?: "0", 450f, yPos, textPaint)
                    yPos += 20f
                }

                pdfDocument.finishPage(page)

                val outputStream = context.contentResolver.openOutputStream(uri)
                    ?: throw IOException("No se pudo abrir el destino PDF")
                outputStream.use { pdfDocument.writeTo(it) }
                pdfDocument.close()
                materiales.size
            }
        }

    // ================================================================
    // UTILIDADES
    // ================================================================

    fun invalidateCache() {
        cachedMateriales = null
        lastModifiedMateriales = 0L
    }

    private fun escapeForCsv(value: String): String =
        CsvSanitizer.escapeCell(value, neutralizeFormula = false)

    private fun escapeForSpreadsheetCsv(value: String): String =
        CsvSanitizer.escapeCell(value, neutralizeFormula = true)

    private fun writeAtomically(file: File, content: String) {
        file.parentFile?.mkdirs()
        val tempFile = File(file.parentFile, ".${file.name}.${System.nanoTime()}.tmp")
        try {
            FileOutputStream(tempFile).use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }
            if (!tempFile.renameTo(file)) {
                throw IOException("No se pudo reemplazar ${file.name} de forma atómica")
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i += 2
                        continue
                    }
                    inQuotes = !inQuotes
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.setLength(0)
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
