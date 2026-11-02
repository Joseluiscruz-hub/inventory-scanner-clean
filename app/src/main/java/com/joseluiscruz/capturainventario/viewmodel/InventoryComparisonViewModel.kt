package com.joseluiscruz.capturainventario.viewmodel

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.dashboard.ComparacionInventario
import com.joseluiscruz.capturainventario.dashboard.ComparadorInventario
import com.joseluiscruz.capturainventario.dashboard.InventarioAlmacen
import com.joseluiscruz.capturainventario.model.InventarioItem
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.InventarioConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * ViewModel para manejar la comparación de inventarios con actualización en tiempo real.
 */
class InventoryComparisonViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "InventoryComparisonVM"
        private const val SHARED_PREFS_NAME = "material_data"
        private const val MATERIALS_KEY = "materials"
    }

    private val sharedPreferences: SharedPreferences =
        getApplication<Application>().getSharedPreferences(SHARED_PREFS_NAME, 0)
    private val gson = Gson()

    private val _inventarioSistema = MutableLiveData<Map<String, InventarioItem>>(emptyMap())
    val inventarioSistema: LiveData<Map<String, InventarioItem>> = _inventarioSistema

    private val _inventarioEscaneado = MutableLiveData<Map<String, InventarioItem>>(emptyMap())
    val inventarioEscaneado: LiveData<Map<String, InventarioItem>> = _inventarioEscaneado

    private val _comparacion = MutableLiveData<List<Triple<String, InventarioItem?, InventarioItem?>>>(emptyList())
    val comparacion: LiveData<List<Triple<String, InventarioItem?, InventarioItem?>>> = _comparacion

    // LiveData para la comparación detallada
    private val _comparacionDetallada = MutableLiveData<List<ComparacionInventario>>(emptyList())
    val comparacionDetallada: LiveData<List<ComparacionInventario>> = _comparacionDetallada

    private val _mensaje = MutableLiveData<String?>(null)
    val mensaje: LiveData<String?> = _mensaje

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Control de actualización en tiempo real
    private var tiempoRealHabilitado = true
    private var isComparacionInProgress = false

    // Listener para cambios en SharedPreferences (actualización en tiempo real)
    private val preferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
        if (key == MATERIALS_KEY && tiempoRealHabilitado && !isComparacionInProgress) {
            Log.d(TAG, "SharedPreferences cambió para key: $key")
            viewModelScope.launch {
                actualizarInventarioEscaneadoEnTiempoReal()
            }
        }
    }

    init {
        Log.d(TAG, "Inicializando InventoryComparisonViewModel")
        // Registrar listener para actualizaciones en tiempo real
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferencesListener)

        // Cargar inventario escaneado inicial
        viewModelScope.launch {
            cargarInventarioEscaneado()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "Limpiando InventoryComparisonViewModel")
        // Desregistrar listener
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferencesListener)
    }

    /**
     * Importa el inventario del sistema desde un archivo URI
     */
    suspend fun importarInventarioSistema(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            _isLoading.postValue(true)
            _error.postValue(null)

            val context = getApplication<Application>()
            val inventarioMap = mutableMapOf<String, InventarioItem>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val header = reader.readLine()?.split(",")

                if (header.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        _error.value = "Archivo CSV inválido: sin encabezados"
                    }
                    return@withContext false
                }

                var lineNumber = 1
                reader.forEachLine { line ->
                    lineNumber++
                    if (line.isNotBlank()) {
                        try {
                            val columns = line.split(",").map { it.trim() }
                            if (columns.size >= 3) {
                                val sku = columns[0]
                                val descripcion = columns.getOrNull(1) ?: ""
                                val cantidadStr = columns.getOrNull(2) ?: "0"
                                val tipoTarima = columns.getOrNull(3) ?: ""

                                val cantidad = cantidadStr.toIntOrNull() ?: 0

                                if (sku.isNotEmpty()) {
                                    inventarioMap[sku] = InventarioItem(
                                        sku = sku,
                                        descripcion = descripcion,
                                        totalPallets = cantidad,
                                        tipoTarima = tipoTarima
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error procesando línea $lineNumber: ${e.message}")
                        }
                    }
                }
            }

            if (inventarioMap.isEmpty()) {
                withContext(Dispatchers.Main) {
                    _error.value = "No se pudieron cargar datos del archivo CSV"
                }
                return@withContext false
            }

            withContext(Dispatchers.Main) {
                _inventarioSistema.value = inventarioMap
                _mensaje.value = "Inventario del sistema cargado: ${inventarioMap.size} items"
                Log.d(TAG, "Inventario del sistema cargado: ${inventarioMap.size} items")

                // Guardar en archivo para uso compartido
                saveSystemInventoryToFile(inventarioMap)

                // Realizar comparación inicial
                realizarComparacion()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar inventario del sistema", e)
            withContext(Dispatchers.Main) {
                _error.value = "Error al cargar inventario: ${e.message}"
                _mensaje.value = "Error al cargar inventario: ${e.message}"
            }
            false
        } finally {
            _isLoading.postValue(false)
        }
    }

    /**
     * Guarda el inventario del sistema en un archivo local para que sea accesible por otros ViewModels
     */
    private fun saveSystemInventoryToFile(inventario: Map<String, InventarioItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = java.io.File(getApplication<Application>().filesDir, "inventario_almacen.csv")
                val header = "SKU,Descripcion,Disponible,Centro\n"
                val content = StringBuilder(header)
                inventario.values.forEach { item ->
                    content.append("${item.sku},${item.descripcion},${item.totalPallets},${item.centro ?: ""}\n")
                }
                file.writeText(content.toString())
                Log.d(TAG, "Inventario del sistema guardado en archivo")
            } catch (e: Exception) {
                Log.e(TAG, "Error al guardar inventario del sistema en archivo", e)
            }
        }
    }

    /**
     * Carga el inventario escaneado desde SharedPreferences
     */
    private suspend fun cargarInventarioEscaneado() = withContext(Dispatchers.IO) {
        try {
            val materialsJson = sharedPreferences.getString(MATERIALS_KEY, "[]") ?: "[]"
            Log.d(TAG, "Cargando inventario escaneado, JSON length: ${materialsJson.length}")

            if (materialsJson == "[]" || materialsJson.isEmpty()) {
                withContext(Dispatchers.Main) {
                    _inventarioEscaneado.value = emptyMap()
                }
                return@withContext
            }

            val materialListType = object : TypeToken<List<MaterialItem>>() {}.type
            val materials: List<MaterialItem> = gson.fromJson(materialsJson, materialListType)

            val inventarioMap = InventarioConverter.convertirMaterialItemsAInventario(materials)

            withContext(Dispatchers.Main) {
                _inventarioEscaneado.value = inventarioMap
                Log.d(TAG, "Inventario escaneado cargado: ${inventarioMap.size} items")

                // Realizar comparación si hay inventario del sistema
                if (_inventarioSistema.value?.isNotEmpty() == true) {
                    realizarComparacion()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar inventario escaneado", e)
            withContext(Dispatchers.Main) {
                _error.value = "Error al cargar inventario escaneado: ${e.message}"
                _mensaje.value = "Error al cargar inventario escaneado: ${e.message}"
            }
        }
    }

    /**
     * Actualiza el inventario escaneado en tiempo real
     */
    private suspend fun actualizarInventarioEscaneadoEnTiempoReal() {
        if (!tiempoRealHabilitado || isComparacionInProgress) {
            Log.d(TAG, "Actualización en tiempo real deshabilitada o comparación en progreso")
            return
        }

        Log.d(TAG, "Actualizando inventario en tiempo real")
        cargarInventarioEscaneado()
        withContext(Dispatchers.Main) {
            _mensaje.value = "Actualizado automáticamente - ${
                java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            }"
        }
    }

    /**
     * Realiza la comparación entre inventarios
     */
    private fun realizarComparacion() {
        if (isComparacionInProgress) {
            Log.d(TAG, "Comparación ya en progreso, saltando")
            return
        }

        viewModelScope.launch {
            try {
                isComparacionInProgress = true
                _isLoading.value = true

                val inventarioSistema = _inventarioSistema.value ?: emptyMap()
                val inventarioEscaneado = _inventarioEscaneado.value ?: emptyMap()

                Log.d(
                    TAG,
                    "Realizando comparación: Sistema=${inventarioSistema.size}, Escaneado=${inventarioEscaneado.size}"
                )

                if (inventarioSistema.isEmpty()) {
                    _comparacionDetallada.value = emptyList()
                    _comparacion.value = emptyList()
                    _mensaje.value = "No hay inventario del sistema cargado"
                    return@launch
                }

                // Convertir inventario sistema a formato InventarioAlmacen
                val almacenList = inventarioSistema.values.map { item ->
                    InventarioAlmacen(
                        sku = item.sku,
                        descripcion = item.descripcion,
                        disponible = item.totalPallets,
                        centro = item.centro ?: ""
                    )
                }

                // Convertir inventario escaneado a formato MaterialItem
                val materialList = inventarioEscaneado.values.map { item ->
                    MaterialItem(
                        sku = item.sku,
                        descripcion = item.descripcion,
                        totalPallets = item.totalPallets.toString(),
                        tipoTarima = item.tipoTarima ?: "",
                        cxPal = "0",
                        fpc = "",
                        con = "",
                        centro = item.centro ?: "",
                        linea = "",
                        op = "",
                        fProd = "",
                        diasV = "",
                        ubicacion = item.ubicacion ?: ""
                    )
                }

                // Crear mapa de tipos de tarima
                val baseTarimas = inventarioEscaneado.values.associate { it.sku to (it.tipoTarima ?: "") }

                // Realizar comparación usando ComparadorInventario
                val comparacion = ComparadorInventario.generarComparacion(
                    escaneado = materialList,
                    almacen = almacenList,
                    baseTarimas = baseTarimas
                )

                _comparacionDetallada.value = comparacion

                // Generar comparación simple también
                val comparacionSimple = mutableListOf<Triple<String, InventarioItem?, InventarioItem?>>()
                val todosLosSKUs = (inventarioSistema.keys + inventarioEscaneado.keys).distinct()

                for (sku in todosLosSKUs) {
                    val itemSistema = inventarioSistema[sku]
                    val itemEscaneado = inventarioEscaneado[sku]
                    comparacionSimple.add(Triple(sku, itemSistema, itemEscaneado))
                }

                _comparacion.value = comparacionSimple

                Log.d(TAG, "Comparación completada: ${comparacion.size} diferencias encontradas")
                _mensaje.value = "Comparación actualizada: ${comparacion.size} items procesados"

            } catch (e: Exception) {
                Log.e(TAG, "Error en comparación", e)
                _error.value = "Error en comparación: ${e.message}"
                _mensaje.value = "Error en comparación: ${e.message}"
            } finally {
                _isLoading.value = false
                isComparacionInProgress = false
            }
        }
    }

    /**
     * Habilita o deshabilita la actualización en tiempo real
     */
    fun habilitarActualizacionTiempoReal(habilitar: Boolean) {
        Log.d(TAG, "Actualización en tiempo real: $habilitar")
        tiempoRealHabilitado = habilitar
        if (habilitar) {
            // Forzar actualización inmediata
            viewModelScope.launch {
                actualizarInventarioEscaneadoEnTiempoReal()
            }
        }
    }

    /**
     * Fuerza una actualización manual de la comparación
     */
    fun actualizarComparacion() {
        Log.d(TAG, "Forzando actualización manual")
        viewModelScope.launch {
            cargarInventarioEscaneado()
        }
    }

    /**
     * Limpia todos los datos
     */
    fun limpiarDatos() {
        _inventarioSistema.value = emptyMap()
        _inventarioEscaneado.value = emptyMap()
        _comparacion.value = emptyList()
        _comparacionDetallada.value = emptyList()
        _mensaje.value = "Datos limpiados"
        _error.value = null
        Log.d(TAG, "Datos limpiados")
    }

    /**
     * Función para limpiar completamente todos los datos de la aplicación
     */
    fun clearAllApplicationData() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)

                // Limpiar todos los LiveData
                _inventarioSistema.postValue(emptyMap())
                _inventarioEscaneado.postValue(emptyMap())
                _comparacion.postValue(emptyList())
                _comparacionDetallada.postValue(emptyList())
                _mensaje.postValue(null)
                _error.postValue(null)

                // Limpiar SharedPreferences de materiales
                sharedPreferences.edit().clear().apply()

                // Limpiar otras SharedPreferences de la app
                val appPrefs = getApplication<Application>().getSharedPreferences("app_config", 0)
                val editor = appPrefs.edit()
                editor.remove("inventario_sistema_cargado")
                editor.remove("inventario_escaneado_activo")
                editor.remove("comparacion_activa")
                editor.remove("ultima_sincronizacion")
                editor.remove("configuracion_comparacion")
                editor.apply()

                _mensaje.postValue("Todos los datos de la aplicación han sido eliminados")
                Log.d(TAG, "Todos los datos de la aplicación eliminados")

            } catch (e: Exception) {
                _error.postValue("Error al limpiar datos: ${e.message}")
                Log.e(TAG, "Error limpiando datos de aplicación", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Reinicio completo del sistema con preservación del historial
     */
    fun resetSystemComplete() {
        viewModelScope.launch {
            try {
                _isLoading.postValue(true)

                // Preservar historial de reinicios
                val appPrefs = getApplication<Application>().getSharedPreferences("app_config", 0)
                val historialReinicios = appPrefs.getString("historial_reinicios", "[]")
                val ultimoReinicio = appPrefs.getLong("ultimo_reinicio", 0L)

                // Limpiar todo
                clearAllApplicationData()

                // Restaurar historial
                appPrefs.edit()
                    .putString("historial_reinicios", historialReinicios)
                    .putLong("ultimo_reinicio", ultimoReinicio)
                    .apply()

                _mensaje.postValue("Sistema reiniciado completamente")
                Log.d(TAG, "Sistema reiniciado completamente")

            } catch (e: Exception) {
                _error.postValue("Error en reinicio completo: ${e.message}")
                Log.e(TAG, "Error en reinicio completo", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Limpia solo los datos de comparación
     */
    fun clearComparisonData() {
        viewModelScope.launch {
            try {
                _comparacion.postValue(emptyList())
                _comparacionDetallada.postValue(emptyList())

                val appPrefs = getApplication<Application>().getSharedPreferences("app_config", 0)
                appPrefs.edit().putBoolean("comparacion_activa", false).apply()

                _mensaje.postValue("Datos de comparación eliminados")
                Log.d(TAG, "Datos de comparación eliminados")

            } catch (e: Exception) {
                _error.postValue("Error al limpiar comparación: ${e.message}")
                Log.e(TAG, "Error limpiando comparación", e)
            }
        }
    }

    /**
     * Limpia solo el inventario escaneado
     */
    fun clearScannedInventory() {
        viewModelScope.launch {
            try {
                // Limpiar inventario escaneado
                _inventarioEscaneado.postValue(emptyMap())

                // Limpiar SharedPreferences
                sharedPreferences.edit().clear().apply()

                val appPrefs = getApplication<Application>().getSharedPreferences("app_config", 0)
                appPrefs.edit().putBoolean("inventario_escaneado_activo", false).apply()

                // Rehacer comparación con datos actuales
                realizarComparacion()

                _mensaje.postValue("Inventario escaneado eliminado")
                Log.d(TAG, "Inventario escaneado eliminado")

            } catch (e: Exception) {
                _error.postValue("Error al limpiar inventario escaneado: ${e.message}")
                Log.e(TAG, "Error limpiando inventario escaneado", e)
            }
        }
    }

    /**
     * Limpia todos los datos de inventario pero mantiene configuración
     */
    fun clearAllInventoryData() {
        viewModelScope.launch {
            try {
                // Limpiar todos los inventarios
                _inventarioSistema.postValue(emptyMap())
                _inventarioEscaneado.postValue(emptyMap())
                _comparacion.postValue(emptyList())
                _comparacionDetallada.postValue(emptyList())

                // Limpiar SharedPreferences de materiales
                sharedPreferences.edit().clear().apply()

                // Actualizar estados en app_config
                val appPrefs = getApplication<Application>().getSharedPreferences("app_config", 0)
                appPrefs.edit()
                    .putBoolean("inventario_sistema_cargado", false)
                    .putBoolean("inventario_escaneado_activo", false)
                    .putBoolean("comparacion_activa", false)
                    .apply()

                _mensaje.postValue("Todos los datos de inventario eliminados")
                Log.d(TAG, "Todos los datos de inventario eliminados")

            } catch (e: Exception) {
                _error.postValue("Error al limpiar inventarios: ${e.message}")
                Log.e(TAG, "Error limpiando inventarios", e)
            }
        }
    }

    /**
     * Reinicio completo del sistema
     */
    fun resetSystem() {
        viewModelScope.launch {
            try {
                // Reinicio completo pero preservando historial
                resetSystemComplete()

            } catch (e: Exception) {
                _error.postValue("Error en reinicio del sistema: ${e.message}")
                Log.e(TAG, "Error en reinicio del sistema", e)
            }
        }
    }

    /**
     * Reinicia solo la comparación manteniendo los inventarios
     */
    fun reiniciarComparacion() {
        viewModelScope.launch {
            try {
                clearComparisonData()
                // Rehacer la comparación con los datos actuales
                realizarComparacion()
                _mensaje.postValue("Comparación reiniciada")
                Log.d(TAG, "Comparación reiniciada")
            } catch (e: Exception) {
                _error.postValue("Error al reiniciar comparación: ${e.message}")
                Log.e(TAG, "Error reiniciando comparación", e)
            }
        }
    }

    /**
     * Reinicia completamente el sistema
     */
    fun reiniciarCompletamente() {
        viewModelScope.launch {
            try {
                resetSystemComplete()
                _mensaje.postValue("Sistema reiniciado completamente")
                Log.d(TAG, "Sistema reiniciado completamente")
            } catch (e: Exception) {
                _error.postValue("Error en reinicio completo: ${e.message}")
                Log.e(TAG, "Error en reinicio completo", e)
            }
        }
    }

    /**
     * Importa el inventario escaneado desde un archivo URI o desde SharedPreferences
     */
    suspend fun importarInventarioEscaneado(uri: Uri?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (uri == null) {
                // Cargar desde SharedPreferences (datos escaneados previamente)
                cargarInventarioEscaneado()
                withContext(Dispatchers.Main) {
                    _mensaje.value = "Inventario escaneado cargado desde el dispositivo"
                }
                return@withContext true
            }

            // Si hay URI, cargar desde archivo CSV
            val context = getApplication<Application>()
            val inventarioMap = mutableMapOf<String, InventarioItem>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
                val header = reader.readLine()?.split(",")

                if (header != null && header.isNotEmpty()) {
                    reader.forEachLine { line ->
                        if (line.isNotBlank()) {
                            val columns = line.split(",")
                            if (columns.size >= 3) {
                                val sku = columns[0].trim()
                                val descripcion = columns.getOrNull(1)?.trim() ?: ""
                                val cantidadStr = columns.getOrNull(2)?.trim() ?: "0"
                                val tipoTarima = columns.getOrNull(3)?.trim() ?: ""

                                try {
                                    val cantidad = cantidadStr.toIntOrNull() ?: 0
                                    inventarioMap[sku] = InventarioItem(
                                        sku = sku,
                                        descripcion = descripcion,
                                        totalPallets = cantidad,
                                        tipoTarima = tipoTarima
                                    )
                                } catch (e: Exception) {
                                    // Continuar con el siguiente item si hay error
                                }
                            }
                        }
                    }
                }
            }

            withContext(Dispatchers.Main) {
                _inventarioEscaneado.value = inventarioMap
                _mensaje.value = "Inventario escaneado importado: ${inventarioMap.size} items"
                // Realizar comparación si hay inventario del sistema
                if (_inventarioSistema.value?.isNotEmpty() == true) {
                    realizarComparacion()
                }
            }

            true
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                _mensaje.value = "Error al importar inventario escaneado: ${e.message}"
            }
            false
        }
    }

    /**
     * Realiza la comparación entre inventarios (método público)
     */
    fun compararInventarios() {
        realizarComparacion()
        _mensaje.value = "Comparación completada"
    }

    /**
     * Calcula la diferencia entre un item del sistema y un item escaneado
     */
    fun calcularDiferencia(itemSistema: InventarioItem?, itemEscaneado: InventarioItem?): Int {
        if (itemSistema == null && itemEscaneado == null) return 0
        if (itemSistema == null) return itemEscaneado?.totalPallets ?: 0
        if (itemEscaneado == null) return -(itemSistema.totalPallets)
        return itemEscaneado.totalPallets - itemSistema.totalPallets
    }

    /**
     * Reiniciar el inventario escaneado
     */
    fun reiniciarInventarioEscaneado() {
        _inventarioEscaneado.value = emptyMap()
        _comparacion.value = emptyList()
        _comparacionDetallada.value = emptyList()
        _mensaje.value = "Inventario escaneado reiniciado"
    }
}
