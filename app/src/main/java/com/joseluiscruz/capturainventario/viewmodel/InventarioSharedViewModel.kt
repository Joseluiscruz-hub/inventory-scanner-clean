package com.joseluiscruz.capturainventario.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.AppDatabase
import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.data.TpmDao
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.repository.CatalogoPlantaRepository
import com.joseluiscruz.capturainventario.data.CatalogoPlantaEntity
import com.joseluiscruz.capturainventario.model.AlertaTPM
import com.joseluiscruz.capturainventario.model.HudEscaneoState
import com.joseluiscruz.capturainventario.model.ProgresoCierre
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.joseluiscruz.capturainventario.ui.comparacion.TPMMonitorManager
import com.joseluiscruz.capturainventario.ui.comparacion.toEstadoColor
import com.joseluiscruz.capturainventario.domain.usecase.*
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.joseluiscruz.capturainventario.util.CsvParser
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import javax.inject.Inject

/**
 * SharedViewModel para sincronizar las actividades de captura y comparación.
 * 
 * Este ViewModel actúa como "fuente única de verdad" compartida entre:
 * - InventarioComposeActivity (captura de escaneos)
 * - ComparacionTiempoRealActivity (visualización de diferencias)
 * 
 * Ambas actividades deben usar este ViewModel (vía activityViewModels()) para
 * garantizar que vean los mismos datos al instante.
 * 
 * Arquitectura:
 * - Usa el repositorio de inventario como fuente de datos (inyectado por Hilt)
 * - Expone StateFlows reactivos que la UI observa
 * - Las actualizaciones en Room se propagan automáticamente a ambas pantallas
 */
@HiltViewModel
class InventarioSharedViewModel @Inject constructor(
    application: Application,
    private val repository: InventarioRepository,
    private val firebaseRepository: FirebaseRepository,
    private val catalogoPlantaRepository: CatalogoPlantaRepository,
    private val tpmDao: TpmDao,
    val manager: InventarioTiempoRealManager,
    private val tpmMonitorManager: TPMMonitorManager,
    private val registrarScanUseCase: RegistrarScanUseCase,
    private val limpiarInventarioUseCase: LimpiarInventarioUseCase,
    private val eliminarItemEscaneadoUseCase: EliminarItemEscaneadoUseCase,
    private val importarInventarioReferenciaUseCase: ImportarInventarioReferenciaUseCase,
    private val sincronizarDesdeNubeUseCase: SincronizarDesdeNubeUseCase,
    private val getEstadisticasUseCase: GetEstadisticasUseCase
) : AndroidViewModel(application) {

    // Flujo defensivo para que errores de Room TPM no cancelen el scope del ViewModel.
    private val skusTpmEnRiesgoSeguro: Flow<List<com.joseluiscruz.capturainventario.data.TpmEntity>> =
        tpmDao
            .observarSkusEnRiesgo()
            .catch { e ->
                Log.e("SharedVM", "Error observando SKUs TPM en riesgo", e)
                emit(emptyList())
            }

    init {
        cargarDesdeArchivoLegacy()
        cargarReferenciaDesdeNube()
        cargarCatalogoPlantaActiva()
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * StateFlow con la comparación completa en tiempo real.
     * Se actualiza automáticamente cuando hay nuevos escaneos.
     */
    val comparacion: StateFlow<List<DiferenciaInventario>> =
        manager.comparacionEnTiempoReal
            .flowOn(Dispatchers.Default)
            .catch { e ->
                Log.e("SharedVM", "Error observando comparación en tiempo real", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * StateFlow con el último item escaneado y su comparación.
     * Útil para mostrar alertas inmediatas en la pantalla de captura.
     */
    val ultimoItemEscaneado: StateFlow<DiferenciaInventario?> =
        manager.ultimoItemEscaneado
            .flowOn(Dispatchers.Default)
            .catch { e ->
                Log.e("SharedVM", "Error observando último item escaneado", e)
                emit(null)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    /**
     * Alertas TPM activas para el SKU recien escaneado.
     */
    val alertasTPMActivas: StateFlow<List<AlertaTPM>> =
        tpmMonitorManager.alertasTPMActivas
            .catch { e ->
                Log.e("SharedVM", "Error observando alertas TPM activas", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * HUD reactivo del ultimo escaneo para overlays premium.
     * Se enriquece con alerta TPM cuando exista coincidencia por SKU.
     */
    val hudUltimoEscaneo: StateFlow<HudEscaneoState> =
        combine(ultimoItemEscaneado, alertasTPMActivas) { item, alertas ->
            item.toHudEscaneoState(alertas.firstOrNull())
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HudEscaneoState.Idle
            )

    /**
     * StateFlow con estadísticas de la sesión de inventario.
     * Se calcula reactivamente basado en la comparación actual.
     */
    val estadisticas: StateFlow<EstadisticasSesion> =
        comparacion
            .map { lista ->
                getEstadisticasUseCase(lista, manager.getInventarioReferencia().size)
            }
            .flowOn(Dispatchers.Default)
            .catch { e ->
                Log.e("SharedVM", "Error calculando estadísticas", e)
                emit(EstadisticasSesion())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = EstadisticasSesion()
            )

    /**
     * StateFlow con solo las diferencias críticas (EXCESO o FALTANTE).
     * Útil para mostrar alertas filtradas.
     */
    val diferenciasCriticas: StateFlow<List<DiferenciaInventario>> =
        manager.diferenciasCriticas
            .flowOn(Dispatchers.Default)
            .catch { e ->
                Log.e("SharedVM", "Error observando diferencias críticas", e)
                emit(emptyList())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    /**
     * Progreso operativo de cierre para vista de supervisor.
     * Incluye conteo real de SKUs TPM en riesgo.
     */
    val progresoCierre: StateFlow<ProgresoCierre> =
        combine(manager.progresoCierre, skusTpmEnRiesgoSeguro) { progresoBase, skusRiesgo ->
            progresoBase.copy(skusTPMRiesgo = skusRiesgo.size)
        }
            .catch { e ->
                Log.e("SharedVM", "Error calculando progreso de cierre", e)
                emit(ProgresoCierre())
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ProgresoCierre()
            )

    /**
     * Registra un nuevo escaneo en la base de datos.
     * 
     * Este método debe ser llamado desde InventarioComposeActivity cada vez
     * que el usuario escanea un código de barras. El Flow de comparacion
     * se actualizará automáticamente, notificando a ambas actividades.
     * 
     * @param codigo El código del material escaneado (SKU)
     * @param descripcion La descripción del material
     * @param cantidad La cantidad escaneada (por defecto 1)
     */
    fun registrarScan(codigo: String, descripcion: String, cantidad: Int = 1) {
        viewModelScope.launch {
            registrarScanUseCase(codigo, descripcion, cantidad)
        }
    }

    // Delegado a CsvParser (objeto testeable en util/CsvParser.kt)
    private fun parseCsvLineSafe(line: String, delimiter: Char = ',') =
        CsvParser.parseCsvLineSafe(line, delimiter)

    /**
     * Importa el inventario de referencia desde un archivo CSV.
     * 
     * @param uri URI del archivo seleccionado
     * @param contentResolver ContentResolver para acceder al archivo
     */
    fun importarInventarioReferenciaCsv(
        uri: Uri,
        contentResolver: ContentResolver,
        callback: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = importarInventarioReferenciaUseCase(uri, contentResolver)
            result.fold(
                onSuccess = { callback(true, it) },
                onFailure = { callback(false, it.message ?: "Error desconocido") }
            )
            _isLoading.value = false
        }
    }

    /**
     * Importa el CSV de referencia SAP desde un archivo físico en caché.
     * Esto evita los timeouts de OneDrive y permite controlar la codificación.
     */
    suspend fun importarInventarioReferenciaCsvDesdeArchivo(
        file: File,
        callback: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        withContext(Dispatchers.IO) {
            try {
                _isLoading.value = true

                val nuevosItems = mutableListOf<InventarioTiempoRealManager.ItemReferencia>()

                FileInputStream(file).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                        var line: String?
                        // Si tu CSV tiene encabezados, lee y descarta la primera línea:
                        val header = reader.readLine()
                        if (header == null) {
                            withContext(Dispatchers.Main) {
                                callback(false, "El archivo está vacío")
                            }
                            return@withContext
                        }
                        if (CsvParser.looksLikeSpreadsheetBinary(header)) {
                            withContext(Dispatchers.Main) {
                                callback(false, "El archivo parece ser Excel (.xlsx) o binario; expórtalo como CSV UTF-8 antes de importarlo.")
                            }
                            return@withContext
                        }

                        // 3. Empezamos a leer línea por línea
                        while (reader.readLine().also { line = it } != null) {
                            val currentLine = line ?: continue
                            if (currentLine.isBlank()) continue

                            parseReferenciaCsvLine(currentLine)?.let { nuevosItems.add(it) }
                        }
                    }
                }

                // 4. Actualizamos el mapa O(1) de referencia SAP y la UI
                if (nuevosItems.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        manager.cargarInventarioSistema(nuevosItems)
                        ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
                            nuevosItems.mapNotNull { referencia ->
                                val cajas = referencia.cajasXPallet.takeIf { it > 0 } ?: referencia.armado.toIntOrNull() ?: referencia.cantidad
                                if (referencia.codigo.isBlank() || cajas <= 0) return@mapNotNull null
                                ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                                    sku = referencia.codigo,
                                    cajasXPallet = cajas,
                                    tarimaPlasticaSku = referencia.tarimaPlasticaSku.ifBlank { ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR }
                                )
                            }
                        )
                        saveToLegacyFile(nuevosItems)
                        firebaseRepository.subirInventarioReferencia(nuevosItems) { _, _ -> }
                        Log.d("ViewModel", "Se importaron ${nuevosItems.size} artículos del CSV.")
                        callback(true, "Se importaron ${nuevosItems.size} artículos de referencia")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(false, "El archivo no contenía registros válidos")
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error parseando CSV local", e)
                withContext(Dispatchers.Main) {
                    callback(false, "Error parseando CSV local: ${e.message}")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Función ETL: Extrae datos de un archivo sucio de Excel/OneDrive, 
     * los limpia y genera un String de formato CSV perfecto y estandarizado.
     */
    suspend fun generarCsvLimpio(file: File): String {
        return withContext(Dispatchers.IO) {
            val stringBuilder = java.lang.StringBuilder()
            stringBuilder.append("SKU,Cantidad,Descripcion\n")

            try {
                FileInputStream(file).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, "ISO-8859-1")).use { reader ->
                        var line: String?
                        var lineCount = 0

                        while (reader.readLine().also { line = it } != null) {
                            lineCount++
                            var currentLine = line ?: continue
                            if (currentLine.isBlank()) continue

                            // 1. Quitar basura de Excel en la primera línea y saltar encabezados
                            if (lineCount == 1) {
                                currentLine = currentLine.replace("\uFEFF", "")
                                if (currentLine.contains("SKU", ignoreCase = true) ||
                                    currentLine.contains("Material", ignoreCase = true)
                                ) {
                                    continue
                                }
                            }

                            // 2. Usar nuestro LECTOR INTELIGENTE en lugar de un simple split
                            val delimitador = if (currentLine.contains(";")) ';' else ','
                            val tokens = parseCsvLineSafe(currentLine, delimitador)

                            // 3. Extraer asumiendo tu formato (Ej: SKU, Descripción, Cantidad)
                            if (tokens.size >= 2) {
                                val sku = tokens[0].trim()
                                val descripcion = if (tokens.size >= 3) tokens[1].trim() else "N/A"
                                val cantidadStr = if (tokens.size >= 3) tokens[2].trim() else tokens[1].trim()

                                if (sku.isNotEmpty()) {
                                    // Limpiamos todo rastro de comas de miles y quitamos decimales (.00)
                                    val cantidadLimpia = cantidadStr.replace(",", "").substringBefore(".")
                                    val cantidad = cantidadLimpia.toIntOrNull() ?: 0

                                    stringBuilder.append("$sku,$cantidad,\"$descripcion\"\n")
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error generando CSV limpio", e)
            }

            stringBuilder.toString()
        }
    }

    /**
     * Guarda la lista de referencia en el archivo local para persistencia legacy.
     */
    private fun saveToLegacyFile(lista: List<InventarioTiempoRealManager.ItemReferencia>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().filesDir, "inventario_almacen.csv")
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
                Log.e("SharedVM", "Error guardando legacy: ${e.message}")
            }
        }
    }

    // Delegado a CsvParser (objeto testeable en util/CsvParser.kt)
    private fun parseReferenciaCsvLine(line: String) =
        CsvParser.parseReferenciaCsvLine(line)

    private fun cargarReferenciaDesdeNube() {
        firebaseRepository.descargarInventarioReferencia { lista, _ ->
            if (!lista.isNullOrEmpty()) {
                viewModelScope.launch(Dispatchers.Main) {
                    manager.cargarInventarioSistema(lista)
                    ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
                        lista.mapNotNull { referencia ->
                            val cajas = referencia.cajasXPallet.takeIf { it > 0 } ?: referencia.armado.toIntOrNull() ?: referencia.cantidad
                            if (referencia.codigo.isBlank() || cajas <= 0) return@mapNotNull null
                            ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                                sku = referencia.codigo,
                                cajasXPallet = cajas,
                                tarimaPlasticaSku = referencia.tarimaPlasticaSku.ifBlank { ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR }
                            )
                        }
                    )
                }
            }
        }
    }

    fun sincronizarDesdeNube(callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = sincronizarDesdeNubeUseCase()
            result.fold(
                onSuccess = { callback(true, it) },
                onFailure = { callback(false, it.message ?: "Error desconocido") }
            )
        }
    }

    /**
     * Carga el inventario de referencia desde una lista (CSV/SAP).
     * Este método debe llamarse al iniciar la sesión de inventario.
     * 
     * @param lista Lista de items de referencia del sistema
     */
    fun cargarInventarioReferencia(lista: List<InventarioTiempoRealManager.ItemReferencia>) {
        manager.cargarInventarioSistema(lista)
    }

    /**
     * Carga el inventario de referencia desde el archivo CSV local (inventario_almacen.csv).
     * Proporciona compatibilidad con el sistema legacy.
     */
    fun cargarDesdeArchivoLegacy() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(getApplication<Application>().filesDir, "inventario_almacen.csv")
                if (!file.exists()) return@launch

                val lines = file.readLines()
                if (lines.size <= 1) return@launch

                val lista = mutableListOf<InventarioTiempoRealManager.ItemReferencia>()
                for (i in 1 until lines.size) {
                    parseReferenciaCsvLine(lines[i])?.let { lista.add(it) }
                }
                manager.cargarInventarioSistema(lista)
                ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
                    lista.mapNotNull { referencia ->
                        val cajas = referencia.cajasXPallet.takeIf { it > 0 } ?: referencia.armado.toIntOrNull() ?: referencia.cantidad
                        if (referencia.codigo.isBlank() || cajas <= 0) return@mapNotNull null
                        ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                            sku = referencia.codigo,
                            cajasXPallet = cajas,
                            tarimaPlasticaSku = referencia.tarimaPlasticaSku.ifBlank { ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR }
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e("SharedVM", "Error cargando legacy: ${e.message}")
            }
        }
    }

    /**
     * Limpia todo el inventario escaneado.
     * Útil para reiniciar al comenzar una nueva sesión.
     */
    fun limpiarInventarioEscaneado() {
        viewModelScope.launch {
            limpiarInventarioUseCase()
        }
    }

    /**
     * Elimina un item específico del inventario escaneado.
     */
    fun eliminarItemEscaneado(codigo: String) {
        viewModelScope.launch {
            eliminarItemEscaneadoUseCase(codigo)
        }
    }

    /**
     * Obtiene todos los items escaneados de forma síncrona.
     * Para uso en operaciones que no requieren observación reactiva.
     */
    suspend fun obtenerTodosLosEscaneados(): List<InventarioEscaneadoEntity> =
        repository.obtenerTodosLosEscaneados()

    /**
     * Verifica si el inventario de referencia está cargado.
     */
    fun tieneReferenciaCargada(): Boolean =
        manager.tieneReferenciaCargada()

    /**
     * Obtiene el inventario de referencia actual.
     */
    fun getInventarioReferencia(): Map<String, InventarioTiempoRealManager.ItemReferencia> =
        manager.getInventarioReferencia()

    /**
     * Carga el catálogo de la planta activa en el ProductoTerminadoCatalogo global.
     */
    private fun cargarCatalogoPlantaActiva() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("app_config", 0)
            val plantaActiva = prefs.getString("planta_activa", "Norte") ?: "Norte"
            
            catalogoPlantaRepository.getCatalogoPorPlanta(plantaActiva).collect { lista ->
                if (lista.isNotEmpty()) {
                    ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
                        lista.map {
                            ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                                sku = it.sku,
                                cajasXPallet = it.cajasXTarima,
                                tarimaPlasticaSku = ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR
                            )
                        }
                    )
                    Log.d("SharedVM", "Catálogo dinámico de $plantaActiva cargado: ${lista.size} SKUs")
                }
            }
        }
    }

    private fun DiferenciaInventario?.toHudEscaneoState(alertaTPM: AlertaTPM?): HudEscaneoState {
        val item = this ?: return HudEscaneoState.Idle

        return HudEscaneoState.Activo(
            sku = item.codigo,
            descripcion = item.descripcion,
            esperado = item.cantidadReferencia,
            actual = item.cantidadActual,
            delta = item.diferencia,
            color = item.toEstadoColor(),
            alertaTPM = alertaTPM
        )
    }
}

/**
 * Data class para estadísticas de la sesión de inventario.
 */
data class EstadisticasSesion(
    val totalSKUs: Int = 0,
    val coincidencias: Int = 0,
    val faltantes: Int = 0,
    val excesos: Int = 0,
    val efectividad: Int = 0,
    val itemsEscaneados: Int = 0,
    val totalSistema: Int = 0,
    val skusPendientes: Int = 0,
    val timestampCalculo: Long = System.currentTimeMillis()
)
