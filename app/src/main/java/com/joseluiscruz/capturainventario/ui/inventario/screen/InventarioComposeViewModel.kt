package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.FechaOrigen
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.comparacion.calcularEstadoDiferencia
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioEvent
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioUiState
import com.joseluiscruz.capturainventario.ui.inventario.state.NavigationEvent
import com.joseluiscruz.capturainventario.ui.inventario.state.ScannedData
import com.joseluiscruz.capturainventario.ui.inventario.state.ResultadoExportacion
import com.joseluiscruz.capturainventario.ui.inventario.state.SnackbarAction
import com.joseluiscruz.capturainventario.ui.inventario.state.SnackbarType
import com.joseluiscruz.capturainventario.util.MensajesUsuario
import com.joseluiscruz.capturainventario.util.ContinuousScanPolicy
import com.joseluiscruz.capturainventario.util.FeedbackManager
import com.joseluiscruz.capturainventario.util.ScanFeedback
import com.joseluiscruz.capturainventario.util.ScanFeedbackKind
import com.joseluiscruz.capturainventario.util.ScannerEngine
import com.joseluiscruz.capturainventario.util.ScannerOutcome
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel adaptado para Jetpack Compose.
 * Envuelve el ViewModel legacy y expone estado reactivo para Compose.
 * 
 * Este ViewModel actúa como un "puente" seguro entre la implementación XML
 * existente y la nueva UI en Compose, permitiendo una migración progresiva.
 */
class InventarioComposeViewModel(
    application: Application,
    private val csvService: com.joseluiscruz.capturainventario.data.repository.CsvInventarioService,
    private val sharedViewModel: com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel,
    private val repository: InventarioRepository
) : AndroidViewModel(application) {

    private val context: Context = application.applicationContext
    private val feedbackManager = FeedbackManager(context)
    // Feedback de escaneo compartido por ML Kit y ZXing (mismos tonos/vibración para ambos motores).
    private val scanFeedback = ScanFeedback(feedbackManager)
    private val appPrefs = context.getSharedPreferences(PREFS_APP_CONFIG, Context.MODE_PRIVATE)

    // Mapa O(1) para búsqueda rápida del inventario de referencia (SAP)
    private var inventarioReferencia = mapOf<String, ItemReferenciaSAP>()

    // Estado para el HUD de la "Dynamic Island" - alerta visual post-escaneo
    private val _ultimoItemHUD = MutableStateFlow<DiferenciaInventario?>(null)
    val ultimoItemHUD: StateFlow<DiferenciaInventario?> = _ultimoItemHUD.asStateFlow()

    // Último escaneo guardado: persiste en pantalla (no se limpia con el HUD) hasta una nueva toma.
    private val _ultimoEscaneo = MutableStateFlow<DiferenciaInventario?>(null)
    val ultimoEscaneo: StateFlow<DiferenciaInventario?> = _ultimoEscaneo.asStateFlow()

    // Estado UI reactivo para Compose
    private val _uiState = MutableStateFlow(InventarioUiState())
    val uiState: StateFlow<InventarioUiState> = _uiState.asStateFlow()

    // Eventos de navegación — la Activity observa este Flow y ejecuta la navegación.
    // Esto mantiene la responsabilidad de navegación fuera del ViewModel.
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    // Referencia al modo de escaneo del ViewModel legacy
    private var currentScanMode: ScanMode = ScanMode.DEFAULT

    // --- VARIABLES PARA ANTI-DUPLICADOS (DEBOUNCE) ---
    private var ultimoCodigoEscaneado: String = ""
    private var tiempoUltimoEscaneo: Long = 0L
    private val DEBOUNCE_TIME_MS = 1500L // 1.5 segundos de "ceguera" temporal para el mismo código

    // Observer guardado por referencia para poder removerlo correctamente en onCleared()
    private val modoEscaneoObserver = Observer<String> { modeString ->
        val newMode = ScanMode.fromString(modeString)
        currentScanMode = newMode
        updateUiStateForMode(newMode)
    }

    init {
        _uiState.update {
            it.copy(escaneoContinuo = appPrefs.getBoolean(KEY_ESCANEO_CONTINUO, false))
        }

        // Cargar almacenes disponibles
        loadAlmacenes()

        // Cargar materiales escaneados
        loadMaterialesEscaneados()
    }

    /**
     * Procesa eventos de usuario desde la UI Compose.
     */
    fun handleEvent(event: InventarioEvent) {
        when (event) {
            // Eventos de modo de escaneo
            is InventarioEvent.OnScanModeSelected -> {
                updateUiStateForMode(event.mode)
                _uiState.update { it.copy(showScanModeDialog = false) }
                feedbackManager.playClick()

                // Si el modo es PALLET, mostrar el diálogo de tarima automáticamente
                if (event.mode == ScanMode.PALLET) {
                    _uiState.update { it.copy(showTipoTarimaDialog = true) }
                }
            }

            InventarioEvent.OnLoadSystemInventory -> {
                if (!_uiState.value.isLoadingReferenciaSap) {
                    feedbackManager.playClick()
                    loadSystemInventory()
                    showSnackbar("Cargando inventario de referencia...")
                }
            }

            is InventarioEvent.OnScanModeDialogDismissed -> {
                _uiState.update { it.copy(showScanModeDialog = false) }
            }

            // Eventos de entrada de datos
            is InventarioEvent.OnSkuManualChanged -> {
                val esProductoTerminado = ProductoTerminadoCatalogo.esProductoTerminado(event.value)
                val esMaterial = ProductoTerminadoCatalogo.esMaterial(event.value)
                val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(event.value) ?: 0
                val pallets = _uiState.value.totalPallets.toIntOrNull() ?: 0
                val calculoPT = ProductoTerminadoCatalogo.calcularInventarioPT(event.value, pallets)

                _uiState.update {
                    it.copy(
                        skuManual = event.value,
                        // Si es modo MANUAL, sincronizar con campo automático
                        scannedData = if (currentScanMode == ScanMode.MANUAL) {
                            it.scannedData.copy(sku = event.value)
                        } else {
                            it.scannedData
                        },
                        // Actualizar información de producto terminado
                        esProductoTerminado = esProductoTerminado,
                        esMaterial = esMaterial,
                        cajasXPalletPT = cajasXPallet,
                        totalCajas = calculoPT?.totalCajas ?: 0,
                        tarimaPlasticaSku = calculoPT?.tarimaPlasticaSku ?: ""
                    )
                }
            }

            is InventarioEvent.OnUbicacionChanged -> {
                _uiState.update { it.copy(ubicacion = event.value) }
            }

            is InventarioEvent.OnTotalPalletsChanged -> {
                val pallets = event.value.toIntOrNull() ?: 0
                val currentSku = _uiState.value.scannedData.sku.ifBlank { _uiState.value.skuManual }
                val calculoPT = ProductoTerminadoCatalogo.calcularInventarioPT(currentSku, pallets)

                _uiState.update {
                    it.copy(
                        totalPallets = event.value,
                        totalCajas = calculoPT?.totalCajas ?: 0,
                        // Actualizar info de PT si aplica
                        esProductoTerminado = calculoPT != null,
                        cajasXPalletPT = ProductoTerminadoCatalogo.obtenerCajasXPallet(currentSku) ?: 0,
                        tarimaPlasticaSku = calculoPT?.tarimaPlasticaSku ?: ""
                    )
                }
            }

            is InventarioEvent.OnRestosChanged -> {
                _uiState.update { it.copy(restos = event.value) }
            }

            is InventarioEvent.OnFpcManualChanged -> {
                _uiState.update { it.copy(fpcManual = event.value) }
            }

            is InventarioEvent.OnConManualChanged -> {
                _uiState.update { it.copy(conManual = event.value) }
            }

            // Eventos de acción principal
            InventarioEvent.OnScanClick -> {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - tiempoUltimoEscaneo) > 1000L) { // 1 segundo de debounce
                    tiempoUltimoEscaneo = currentTime
                    feedbackManager.playClick()
                    triggerScanRequest()
                }
            }

            InventarioEvent.OnGuardarClick -> {
                feedbackManager.playClick()
                guardarMaterial()
            }

            InventarioEvent.OnSiguienteClick -> {
                feedbackManager.playClick()
                navegarAComparacionTiempoReal()
            }

            InventarioEvent.OnExportarClick -> {
                feedbackManager.playClick()
                // Navegar a resumen para exportar
                navigateToResumen()
            }

            InventarioEvent.OnEnviarCorreoClick -> {
                feedbackManager.playClick()
                enviarPorCorreo()
            }

            InventarioEvent.OnResumenClick -> {
                feedbackManager.playClick()
                navigateToResumen()
            }

            InventarioEvent.OnCargarClick -> {
                feedbackManager.playClick()
                // Navegar a captura para importar
                navigateToCapturaParaImportar()
            }

            InventarioEvent.OnRestosButtonClick -> {
                feedbackManager.playClick()
                navigateToRestos()
            }

            InventarioEvent.OnCompararEnviarClick -> {
                feedbackManager.playClick()
                compararInventarios()
            }

            // Eventos de selección de tarima
            is InventarioEvent.OnTipoTarimaSelected -> {
                _uiState.update {
                    it.copy(
                        tipoTarimaSeleccionada = event.tipo,
                        showTipoTarimaDialog = false
                    )
                }
                feedbackManager.playClick()
            }

            InventarioEvent.OnTipoTarimaClick -> {
                _uiState.update { it.copy(showTipoTarimaDialog = true) }
                feedbackManager.playClick()
            }

            InventarioEvent.OnTipoTarimaDialogDismissed -> {
                _uiState.update { it.copy(showTipoTarimaDialog = false) }
            }

            // Eventos de almacén
            InventarioEvent.OnAlmacenClick -> {
                _uiState.update { it.copy(showAlmacenDialog = true) }
                feedbackManager.playClick()
            }

            is InventarioEvent.OnAlmacenSelected -> {
                _uiState.update {
                    it.copy(
                        selectedAlmacen = event.almacen,
                        showAlmacenDialog = false
                    )
                }
                // Recalcular comparación al cambiar de almacén
                recalculateComparison(event.almacen)
                feedbackManager.playClick()
            }

            InventarioEvent.OnAlmacenDialogDismissed -> {
                _uiState.update { it.copy(showAlmacenDialog = false) }
            }

            // Eventos de escaneo QR
            is InventarioEvent.OnScanResultReceived -> {
                procesarResultadoEscaneo(event.qrContent)
            }

            InventarioEvent.OnScanCancelled -> {
                showSnackbar("Escaneo cancelado")
            }

            is InventarioEvent.OnScannerOutcome -> {
                onScannerOutcome(event.outcome)
            }

            is InventarioEvent.OnEscaneoContinuoChanged -> {
                appPrefs.edit { putBoolean(KEY_ESCANEO_CONTINUO, event.enabled) }
                _uiState.update { it.copy(escaneoContinuo = event.enabled) }
                feedbackManager.playClick()
            }

            // Eventos de ciclo de vida
            InventarioEvent.OnScreenLoaded -> {
                // Inicializar estado según modo actual
                updateUiStateForMode(currentScanMode)
            }

            InventarioEvent.OnSnackbarShown -> {
                _uiState.update {
                    it.copy(snackbarMessage = null, snackbarType = SnackbarType.INFO, snackbarAction = null)
                }
            }

            is InventarioEvent.OnSnackbarAction -> {
                when (event.action) {
                    SnackbarAction.REINTENTAR_GUARDADO -> guardarMaterial()
                    SnackbarAction.REINTENTAR_SAP -> handleEvent(InventarioEvent.OnLoadSystemInventory)
                    SnackbarAction.REESCANEAR -> triggerScanRequest()
                    SnackbarAction.REINTENTAR_EXPORTACION ->
                        _navigationEvent.tryEmit(NavigationEvent.ExportarCsv)
                }
            }

            InventarioEvent.OnNuevaTomaClick -> {
                feedbackManager.playClick()
                abrirConfirmacionNuevaToma()
            }

            InventarioEvent.OnNuevaTomaDialogDismissed -> {
                reabrirNuevaTomaTrasExportar = false
                _uiState.update { it.copy(showNuevaTomaDialog = false) }
            }

            InventarioEvent.OnNuevaTomaExportarClick -> {
                feedbackManager.playClick()
                reabrirNuevaTomaTrasExportar = true
                _uiState.update { it.copy(showNuevaTomaDialog = false) }
                _navigationEvent.tryEmit(NavigationEvent.ExportarCsv)
            }

            is InventarioEvent.OnExportarCsvDestinoElegido -> {
                exportarRespaldoCsv(event.uri)
            }

            is InventarioEvent.OnExportacionCsvTerminada -> {
                onExportacionCsvTerminada(event.resultado)
            }

            // Eventos de botones de modo rápido
            InventarioEvent.OnScanManualClick -> {
                updateUiStateForMode(ScanMode.MANUAL)
                showSnackbar("Modo MANUAL activado")
                feedbackManager.playClick()
            }

            InventarioEvent.OnScanPalletsClick -> {
                updateUiStateForMode(ScanMode.PALLET)
                showSnackbar("Modo PALLET activado")
                _uiState.update { it.copy(showTipoTarimaDialog = true) }
                feedbackManager.playClick()
            }

            InventarioEvent.OnScanRumbaClick -> {
                updateUiStateForMode(ScanMode.RUMBA)
                showSnackbar("Modo RUMBA activado")
                feedbackManager.playClick()
            }

            // Eventos de comparación en tiempo real (activar/desactivar manualmente)
            InventarioEvent.OnComparisonStart -> {
                startComparison()
                feedbackManager.playClick()
            }

            InventarioEvent.OnComparisonStop -> {
                stopComparison()
                feedbackManager.playClick()
            }

            InventarioEvent.OnResetSession -> {
                reabrirNuevaTomaTrasExportar = false
                _uiState.update { it.copy(showNuevaTomaDialog = false) }
                resetSession()
                feedbackManager.playClick()
            }
        }
    }

    /**
     * Actualiza el estado UI según el modo de escaneo seleccionado.
     * Conserva el comportamiento del flujo de captura anterior a Compose.
     */
    private fun updateUiStateForMode(mode: ScanMode) {
        currentScanMode = mode

        val newState = when (mode) {
            ScanMode.RUMBA -> {
                _uiState.value.copy(
                    scanMode = mode,
                    isSkuManualEnabled = false,
                    isUbicacionEnabled = true,
                    isTotalPalletsEnabled = true, // Habilitado para ingreso manual
                    isRestosEnabled = false,
                    showScannedFields = true // Mostrar datos extraídos
                )
            }

            ScanMode.PALLET -> {
                _uiState.value.copy(
                    scanMode = mode,
                    isSkuManualEnabled = false,
                    isUbicacionEnabled = true,
                    isTotalPalletsEnabled = false,
                    totalPallets = "1", // Siempre es 1 en modo PALLET
                    isRestosEnabled = false,
                    showScannedFields = true
                )
            }

            ScanMode.MANUAL -> {
                _uiState.value.copy(
                    scanMode = mode,
                    isSkuManualEnabled = true,
                    isUbicacionEnabled = true,
                    isTotalPalletsEnabled = true,
                    isRestosEnabled = true,
                    showScannedFields = true,
                    skuManual = "", // Limpiar en cambio a manual
                    totalPallets = ""
                )
            }

            ScanMode.DEFAULT -> {
                _uiState.value.copy(
                    scanMode = mode,
                    isSkuManualEnabled = true,
                    isUbicacionEnabled = true,
                    isTotalPalletsEnabled = true,
                    isRestosEnabled = true,
                    showScannedFields = true
                )
            }
        }

        _uiState.update { newState }
    }

    /**
     * Procesa el resultado del escaneo QR.
     * Conserva la validación del flujo de captura anterior a Compose.
     */
    private fun procesarResultadoEscaneo(qrContent: String) {
        val currentTime = System.currentTimeMillis()

        // Sin un identificador único de pallet solo se bloquea el rebote inmediato
        // del lector. Un QR idéntico puede representar otra unidad válida.
        if (qrContent == ultimoCodigoEscaneado &&
            (currentTime - tiempoUltimoEscaneo) < DEBOUNCE_TIME_MS
        ) {
            Log.d("InventarioVM", "Debounce: escaneo inmediato bloqueado")
            return
        }

        try {
            val datos = parseQrData(qrContent)
            // El debounce se arma únicamente después de que el QR pudo interpretarse.
            ultimoCodigoEscaneado = qrContent
            tiempoUltimoEscaneo = currentTime

            _uiState.update {
                it.copy(
                    scannedData = ScannedData(
                        sku = datos["SKU"] ?: "",
                        dp = datos["DP"] ?: "",
                        cxPal = datos["CxPal"] ?: "",
                        fpc = datos["FPC"] ?: "",
                        con = datos["Con"] ?: "",
                        centro = datos["Centro"] ?: "",
                        linea = datos["LINEA"] ?: "",
                        op = datos["OP"] ?: "",
                        fProd = datos["FProd"] ?: "",
                        diasV = datos["Dias V"] ?: ""
                    )
                )
            }

            // Lógica específica por modo
            when (currentScanMode) {
                ScanMode.PALLET -> {
                    // Modo PALLET: Guardar automáticamente (1 pallet)
                    guardarMaterial()
                }

                ScanMode.RUMBA -> {
                    // Modo RUMBA: Mostrar datos y esperar ingreso manual de pallets
                    scanFeedback.play(ScanFeedback.kindForParse(parsedOk = true))
                    showSnackbar("Datos escaneados. Ingrese cantidad de pallets.")
                }

                else -> {
                    scanFeedback.play(ScanFeedback.kindForParse(parsedOk = true))
                    showSnackbar("Datos QR extraídos correctamente")
                }
            }
        } catch (e: Exception) {
            scanFeedback.play(ScanFeedback.kindForParse(parsedOk = false))
            Log.w("InventarioVM", "QR no interpretable", e)
            _uiState.update { it.copy(errorMessage = MensajesUsuario.QR_NO_RECONOCIDO) }
            // errorMessage no se muestra en ninguna pantalla: el snackbar es lo que ve el operador.
            showSnackbar(MensajesUsuario.QR_NO_RECONOCIDO, SnackbarType.ERROR, SnackbarAction.REESCANEAR)
            limpiarCampos()
        }
    }

    /**
     * Parsea los datos de un código QR.
     * Conserva la lógica compleja del flujo de captura anterior a Compose.
     */
    private fun parseQrData(qrText: String): Map<String, String> {
        if (qrText.isBlank()) {
            throw IllegalArgumentException("El código QR está vacío")
        }

        val datos = mutableMapOf<String, String>()

        try {
            // Paso 1: Intentar parsear líneas en formato "clave:valor" o con regex específicos
            qrText.lines().forEach { line ->
                val idx = line.indexOf(":")
                if (idx != -1) {
                    val key = line.substring(0, idx).trim()
                    val value = line.substring(idx + 1).trim()
                    datos[key] = value
                } else {
                    // Buscar patrones específicos con regex
                    val centroRegex = Regex("^Centro:(\\S+)", RegexOption.IGNORE_CASE)
                    val lineaRegex = Regex("^LINEA:(\\S+)", RegexOption.IGNORE_CASE)
                    val matchCentro = centroRegex.find(line)
                    val matchLinea = lineaRegex.find(line)

                    if (matchCentro != null) {
                        datos["Centro"] = matchCentro.groupValues[1]
                    } else if (matchLinea != null) {
                        datos["LINEA"] = matchLinea.groupValues[1]
                    }
                }
            }

            // Paso 2: Si no se detectó ningún campo, intentar asignar por orden de línea
            if (datos.isEmpty()) {
                val lines = qrText.lines().map { it.trim() }.filter { it.isNotEmpty() }

                if (lines.size >= 10) {
                    datos["SKU"] = lines[0]
                    datos["DP"] = lines[1]
                    datos["CxPal"] = lines[2]
                    datos["FPC"] = lines[3]
                    datos["Con"] = lines[4]
                    datos["Centro"] = lines[5]
                    datos["LINEA"] = lines[6]
                    datos["OP"] = lines[7]
                    datos["FProd"] = lines[8]
                    datos["Dias V"] = lines[9]
                } else if (lines.size >= 3) {
                    datos["SKU"] = lines[0]
                    datos["DP"] = lines[1]
                    datos["CxPal"] = lines[2]
                } else if (lines.size >= 2) {
                    datos["SKU"] = lines[0]
                    datos["DP"] = lines[1]
                } else if (lines.isNotEmpty()) {
                    datos["SKU"] = lines[0]
                }
            }

            // Verificar que al menos se haya extraído el SKU
            if (datos["SKU"].isNullOrBlank()) {
                throw IllegalArgumentException("No se pudo extraer el SKU del código QR")
            }

            // Paso 3: Limpiar prefijos y devolver el mapa
            return limpiarPrefijosQR(datos)
        } catch (e: Exception) {
            throw IllegalArgumentException("Error al procesar el código QR: ${e.message}")
        }
    }

    private fun limpiarPrefijosQR(datos: Map<String, String>): Map<String, String> {
        val prefijos = listOf(
            "SKU", "DP", "CxPal", "FPC", "Con", "Centro", "LINEA", "OP", "FProd", "Dias V"
        )
        return datos.mapValues { (k, v) ->
            val prefijo = prefijos.find { k.equals(it, ignoreCase = true) }
            if (prefijo != null && v.startsWith("$prefijo:")) {
                v.removePrefix("$prefijo:").trim()
            } else {
                v.trim()
            }
        }
    }

    /**
     * Guarda el material actual.
     */
    private fun guardarMaterial() {
        val state = _uiState.value
        if (state.skuManual.isBlank() && state.scannedData.sku.isBlank()) {
            feedbackManager.playError()
            _uiState.update { it.copy(errorMessage = MensajesUsuario.SKU_VACIO) }
            showSnackbar(MensajesUsuario.SKU_VACIO, SnackbarType.ERROR)
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        val material = crearMaterialDesdeEstado(state)
        val pallets = material.totalPallets?.toIntOrNull() ?: 0
        val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(material.sku) ?: 0
        val totalCajas = pallets * cajasXPallet
        val mensajeDetallado = buildString {
            append("SKU: ${material.sku}\n")
            append("Pallets: $pallets\n")
            if (cajasXPallet > 0) {
                append("Cajas: $totalCajas\n")
                append("Tarimas Plásticas: $pallets pzs")
            }
        }

        viewModelScope.launch {
            val comparisonFeedbackEnabled =
                _uiState.value.isComparisonActive && _uiState.value.isReferenciaSistemaCargada

            try {
                // Room confirma primero el evento; el CSV queda como espejo/exportación.
                procesarEscaneoConHUD(
                    codigo = material.sku,
                    cantidadEscaneada = pallets,
                    cantidadRestos = material.restos?.toIntOrNull() ?: 0,
                    descripcion = material.descripcion,
                    fechaEtiqueta = material.fProd,
                    diasV = material.diasV,
                    almacen = material.almacen,
                    ubicacion = material.ubicacion,
                    tipoTarima = material.tipoTarima,
                    feedbackEnabled = comparisonFeedbackEnabled
                )
            } catch (e: Exception) {
                Log.e("InventarioVM", "No se guardó el escaneo de ${material.sku}", e)
                val message = MensajesUsuario.guardarEscaneo(e)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        snackbarMessage = message,
                        snackbarType = SnackbarType.ERROR,
                        snackbarAction = SnackbarAction.REINTENTAR_GUARDADO,
                        errorMessage = message
                    )
                }
                feedbackManager.playError()
                return@launch
            }

            val csvResult = csvService.guardarMaterial(material)
            val csvError =
                (csvResult as? com.joseluiscruz.capturainventario.domain.model.OperationResult.Error)
                    ?.message
            val message = if (csvError == null) {
                mensajeDetallado
            } else {
                Log.w("InventarioVM", "Copia CSV no actualizada: $csvError")
                "$mensajeDetallado\n${MensajesUsuario.COPIA_CSV_PENDIENTE}"
            }

            _uiState.update {
                it.copy(
                    isSaving = false,
                    snackbarMessage = message,
                    snackbarType =
                        if (csvError == null) SnackbarType.SUCCESS else SnackbarType.INFO,
                    errorMessage = null
                )
            }

            if (!comparisonFeedbackEnabled) {
                feedbackManager.playSuccess()
            }

            recalculateComparison()
            loadMaterialesEscaneados()
            actualizarCapacidadAlmacen()

            // PALLET siempre relanza; RUMBA/DEFAULT solo con "escaneo continuo" activado.
            if (ContinuousScanPolicy.debeRelanzar(currentScanMode, _uiState.value.escaneoContinuo)) {
                kotlinx.coroutines.delay(800.milliseconds)
                triggerScanRequest()
            }

            limpiarCampos()
            Log.d("InventarioVM", "Material confirmado en Room: ${material.sku}")
        }
    }

    /**
     * Crea un MaterialItem desde el estado actual de la UI.
     */
    private fun crearMaterialDesdeEstado(state: InventarioUiState): MaterialItem {
        return MaterialItem(
            sku = state.scannedData.sku.ifBlank { state.skuManual },
            descripcion = state.scannedData.dp,
            cxPal = state.scannedData.cxPal,
            fpc = state.scannedData.fpc.ifBlank { state.fpcManual },
            con = state.scannedData.con.ifBlank { state.conManual },
            centro = state.scannedData.centro,
            linea = state.scannedData.linea,
            op = state.scannedData.op,
            fProd = state.scannedData.fProd.ifBlank { if (state.fpcManual.length >= 8) state.fpcManual else "" },
            diasV = state.scannedData.diasV,
            ubicacion = state.ubicacion,
            totalPallets = state.totalPallets.ifBlank { "1" },
            restos = state.restos,
            tipoTarima = state.tipoTarimaSeleccionada,
            almacen = state.selectedAlmacen
        )
    }

    /**
     * Carga el inventario del sistema desde el archivo local.
     */
    private fun loadSystemInventory() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingReferenciaSap = true) }
            try {
                val result = csvService.obtenerInventarioAlmacen()
                if (result is com.joseluiscruz.capturainventario.domain.model.OperationResult.Error) {
                    Log.e("InventarioVM", "Referencia SAP no disponible: ${result.message} (${result.code})", result.cause)
                    val mensaje = MensajesUsuario.cargarReferenciaSap(result.cause)
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isReferenciaSistemaCargada = false,
                                errorMessage = mensaje
                            )
                        }
                        showSnackbar(mensaje, SnackbarType.ERROR, SnackbarAction.REINTENTAR_SAP)
                    }
                    return@launch
                }

                val items = result.getOrNull() ?: emptyList()
                
                if (items.isNotEmpty()) {
                    // Actualizar el mapa de referencia para el HUD
                    inventarioReferencia = items.associateBy(
                        keySelector = { it.sku },
                        valueTransform = { item ->
                            ItemReferenciaSAP(
                                codigo = item.sku,
                                descripcion = item.descripcion,
                                cantidad = item.disponible
                            )
                        }
                    )
                    
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isReferenciaSistemaCargada = true) }
                        showSnackbar("Referencia SAP cargada: ${items.size} SKUs", SnackbarType.SUCCESS)
                        Log.d("InventarioVM", "Referencia SAP cargada: ${items.size} items")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(isReferenciaSistemaCargada = false) }
                        showSnackbar("No hay inventario SAP disponible", SnackbarType.ERROR)
                    }
                }
            } catch (e: Exception) {
                Log.e("InventarioVM", "Error cargando referencia SAP", e)
                val mensaje = MensajesUsuario.cargarReferenciaSap(e)
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(isReferenciaSistemaCargada = false, errorMessage = mensaje) }
                    showSnackbar(mensaje, SnackbarType.ERROR, SnackbarAction.REINTENTAR_SAP)
                }
            } finally {
                _uiState.update { it.copy(isLoadingReferenciaSap = false) }
            }
        }
    }

    /**
     * Inicia la comparación en tiempo real manualmente.
     */
    private fun startComparison() {
        _uiState.update { it.copy(isComparisonActive = true) }
        recalculateComparison()
        if (_uiState.value.isReferenciaSistemaCargada) {
            showSnackbar("Comparación en tiempo real ACTIVADA")
        } else {
            showSnackbar("Conteo ciego activo: carga la referencia SAP para comparar")
        }
    }

    /**
     * Detiene la comparación en tiempo real manualmente.
     */
    private fun stopComparison() {
        _uiState.update {
            it.copy(
                isComparisonActive = false,
                totalFaltantes = 0,
                totalSobrantes = 0,
                itemsConDiferencia = 0
            )
        }
        showSnackbar("Comparación en tiempo real DESACTIVADA")
    }

    /**
     * Recalcula las estadísticas de comparación
     */
    private fun recalculateComparison(almacenOverride: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedAlmacen = almacenOverride ?: _uiState.value.selectedAlmacen
            val escaneadoTotal = repository.obtenerTodosLosEscaneados()
                .map(::entityToMaterial)
            val sistemaTotal = csvService.obtenerInventarioAlmacen().getOrNull() ?: emptyList()

            // Un conteo ciego no es una discrepancia contra un inventario de cero.
            if (sistemaTotal.isEmpty()) {
                _uiState.update {
                    it.copy(
                        totalFaltantes = 0,
                        totalSobrantes = 0,
                        itemsConDiferencia = 0
                    )
                }
                return@launch
            }

            val escaneado = if (selectedAlmacen != "Seleccionar Almacén") {
                escaneadoTotal.filter {
                    it.almacen.equals(selectedAlmacen, ignoreCase = true)
                }
            } else {
                escaneadoTotal
            }
            val sistema = if (selectedAlmacen != "Seleccionar Almacén") {
                sistemaTotal.filter {
                    it.centro.equals(selectedAlmacen, ignoreCase = true)
                }
            } else {
                sistemaTotal
            }

            if (sistema.isEmpty()) return@launch

            var faltantes = 0
            var sobrantes = 0
            var diffItems = 0
            val sistemaMap = sistema.associateBy { it.sku.trim().uppercase() }
            val escaneadoMap = escaneado.groupBy { it.sku.trim().uppercase() }

            sistemaMap.forEach { (sku, itemSis) ->
                val totalEsc = escaneadoMap[sku]
                    ?.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
                    ?: 0
                val diff = totalEsc - itemSis.disponible
                when {
                    diff < 0 -> {
                        faltantes += kotlin.math.abs(diff)
                        diffItems++
                    }
                    diff > 0 -> {
                        sobrantes += diff
                        diffItems++
                    }
                }
            }

            escaneadoMap.forEach { (sku, items) ->
                if (sku !in sistemaMap) {
                    sobrantes += items.sumOf {
                        it.totalPallets?.toIntOrNull() ?: 0
                    }
                    diffItems++
                }
            }

            _uiState.update {
                it.copy(
                    totalFaltantes = faltantes,
                    totalSobrantes = sobrantes,
                    itemsConDiferencia = diffItems
                )
            }
        }
    }

    /**
     * Limpia los campos de entrada.
     */
    private fun limpiarCampos() {
        _uiState.update {
            it.copy(
                skuManual = "",
                ubicacion = "",
                totalPallets = "",
                restos = "",
                scannedData = ScannedData()
            )
        }
    }

    /**
     * Muestra un mensaje Snackbar.
     */
    private fun showSnackbar(
        message: String,
        type: SnackbarType = SnackbarType.INFO,
        action: SnackbarAction? = null
    ) {
        _uiState.update { it.copy(snackbarMessage = message, snackbarType = type, snackbarAction = action) }
    }

    /** Si el operador exportó desde la confirmación de "Nueva toma", se vuelve a mostrar al terminar. */
    private var reabrirNuevaTomaTrasExportar = false

    private fun abrirConfirmacionNuevaToma() {
        _uiState.update {
            it.copy(
                showNuevaTomaDialog = true,
                nuevaTomaCargando = true,
                nuevaTomaTotalRegistros = null,
                nuevaTomaSinSincronizar = null
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            val resumen = try {
                repository.resumenNuevaToma()
            } catch (e: Exception) {
                Log.e("InventarioVM", "No se pudo contar registros para nueva toma", e)
                null
            }
            _uiState.update {
                it.copy(
                    nuevaTomaCargando = false,
                    nuevaTomaTotalRegistros = resumen?.totalRegistros,
                    nuevaTomaSinSincronizar = resumen?.sinSincronizar
                )
            }
        }
    }

    /**
     * Exporta a CSV los registros de Room de todas las sesiones: exactamente lo que cuenta la
     * confirmación y lo que borra iniciarNuevaToma(). No usa el espejo CSV, que puede estar
     * desactualizado si alguna escritura CSV falló después de confirmar en Room.
     */
    private fun exportarRespaldoCsv(uri: android.net.Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val resultado = try {
                val materiales = repository.obtenerEventosParaNuevaToma().map(::entityToMaterial)
                if (materiales.isEmpty()) {
                    ResultadoExportacion.Fallida(sinDatos = true)
                } else {
                    when (val r = csvService.exportarMaterialesCsv(uri, materiales)) {
                        is com.joseluiscruz.capturainventario.domain.model.OperationResult.Success ->
                            ResultadoExportacion.Exitosa(r.data)
                        is com.joseluiscruz.capturainventario.domain.model.OperationResult.Error -> {
                            Log.e("InventarioVM", "Exportación CSV fallida: ${r.message} (${r.code})", r.cause)
                            ResultadoExportacion.Fallida(sinDatos = false)
                        }
                        com.joseluiscruz.capturainventario.domain.model.OperationResult.Loading ->
                            ResultadoExportacion.Fallida(sinDatos = false)
                    }
                }
            } catch (e: Exception) {
                Log.e("InventarioVM", "Exportación CSV fallida", e)
                ResultadoExportacion.Fallida(sinDatos = false)
            }
            withContext(Dispatchers.Main) { onExportacionCsvTerminada(resultado) }
        }
    }

    private fun onExportacionCsvTerminada(resultado: ResultadoExportacion) {
        when (resultado) {
            is ResultadoExportacion.Exitosa ->
                showSnackbar("CSV exportado: ${resultado.registros} registros", SnackbarType.SUCCESS)
            is ResultadoExportacion.Fallida ->
                if (resultado.sinDatos) {
                    showSnackbar("No hay capturas para exportar.", SnackbarType.INFO)
                } else {
                    showSnackbar(
                        MensajesUsuario.EXPORTAR_CSV,
                        SnackbarType.ERROR,
                        SnackbarAction.REINTENTAR_EXPORTACION
                    )
                }
            ResultadoExportacion.Cancelada -> Unit
        }
        if (reabrirNuevaTomaTrasExportar) {
            reabrirNuevaTomaTrasExportar = false
            abrirConfirmacionNuevaToma()
        }
    }

    /**
     * Navega a la pantalla de resumen.
     * Emite un NavigationEvent; la Activity ejecuta la navegación real.
     */
    private fun navigateToResumen() {
        _navigationEvent.tryEmit(NavigationEvent.ToResumen)
    }

    /**
     * Navega al flujo de captura para importar.
     * Emite un NavigationEvent; la Activity ejecuta la navegación real.
     */
    private fun navigateToCapturaParaImportar() {
        _navigationEvent.tryEmit(NavigationEvent.ToCapturaParaImportar)
    }

    /**
     * Navega a RestosActivity.
     * Emite un NavigationEvent; la Activity ejecuta la navegación real.
     */
    private fun navigateToRestos() {
        _navigationEvent.tryEmit(NavigationEvent.ToRestos)
    }

    /**
     * Compara inventarios y muestra resultados.
     */
    private fun compararInventarios() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val total = repository.contarItemsEscaneadosSincrono()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = "Comparación preparada con $total SKUs",
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("InventarioVM", "Error preparando la comparación", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = MensajesUsuario.PREPARAR_COMPARACION,
                        snackbarMessage = MensajesUsuario.PREPARAR_COMPARACION,
                        snackbarType = SnackbarType.ERROR
                    )
                }
            }
        }
    }

    /**
     * Envía el inventario por correo.
     * Emite un NavigationEvent; la Activity ejecuta la navegación real.
     */
    private fun enviarPorCorreo() {
        _navigationEvent.tryEmit(NavigationEvent.ToEnviarCorreo)
    }

    /**
     * Navega a la pantalla de Comparación en Tiempo Real (ComparacionTiempoRealActivity).
     * Se usa cuando el operador finaliza el escaneo de un pallet y quiere ver el resumen.
     * Emite un NavigationEvent; la Activity ejecuta la navegación real.
     */
    private fun navegarAComparacionTiempoReal() {
        _navigationEvent.tryEmit(NavigationEvent.ToComparacionTiempoReal)
    }

    /**
     * Carga la lista de almacenes disponibles desde Gestión de Almacenes (fuente de verdad).
     */
    private fun loadAlmacenes() {
        viewModelScope.launch {
            try {
                val manager = com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager(context)
                val almacenesReales = manager
                    .obtenerNombresAlmacenes()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

                _uiState.update { current ->
                    val selectedValido = current.selectedAlmacen in almacenesReales
                    current.copy(
                        // El diálogo debe listar solo almacenes reales configurados.
                        almacenesDisponibles = almacenesReales,
                        // El placeholder se mantiene solo como texto visual cuando no hay selección.
                        selectedAlmacen = if (selectedValido) current.selectedAlmacen else "Seleccionar Almacén"
                    )
                }
            } catch (e: Exception) {
                Log.e("InventarioViewModel", "Error cargando almacenes: ${e.message}")
                _uiState.update { it.copy(almacenesDisponibles = emptyList(), selectedAlmacen = "Seleccionar Almacén") }
            }
        }
    }

    /**
     * Carga la lista de materiales escaneados desde el ViewModel legacy.
     */
    private fun entityToMaterial(entity: InventarioEscaneadoEntity): MaterialItem {
        return MaterialItem(
            sku = entity.codigo,
            descripcion = entity.descripcion,
            cxPal = "",
            fpc = "",
            con = "",
            centro = "",
            linea = "",
            op = "",
            fProd = entity.fechaEtiqueta.orEmpty(),
            diasV = "",
            ubicacion = entity.ubicacion.orEmpty(),
            totalPallets = entity.cantidadTotal.toString(),
            restos = entity.cantidadRestos.toString(),
            tipoTarima = entity.tipoTarima.orEmpty(),
            almacen = entity.almacen.orEmpty()
        )
    }

    private fun loadMaterialesEscaneados() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val materiales = repository.obtenerTodosLosEscaneados()
                    .map(::entityToMaterial)
                _uiState.update { it.copy(materialesEscaneados = materiales) }
            } catch (e: Exception) {
                Log.e("InventarioViewModel", "Error cargando materiales: ${e.message}")
                _uiState.update { it.copy(materialesEscaneados = emptyList()) }
            }
        }
    }

    // Canal para solicitar escaneo desde la UI
    private val _scanRequestTrigger = MutableStateFlow<Long?>(null)
    val scanRequestTrigger: StateFlow<Long?> = _scanRequestTrigger.asStateFlow()

    /**
     * Trigger para solicitar escaneo desde la Activity.
     * Este evento debe ser observado por la Activity para lanzar el scanLauncher.
     */
    private fun triggerScanRequest() {
        _scanRequestTrigger.value = System.currentTimeMillis()
    }

    // Se elimina el init redundante y la carga automática de SAP




    /**
     * Procesa un escaneo y dispara el HUD de validación SAP.
     * 
     * Este método:
     * 1. Guarda el escaneo en Room (base de datos local)
     * 2. Obtiene el total acumulado de ese código
     * 3. Compara con la cantidad SAP
     * 4. Dispara el HUD con la diferencia para mostrar alerta visual
     * 
     * @param codigo El código SKU escaneado
     * @param cantidadEscaneada La cantidad escaneada (por defecto 1)
     * @param descripcion Descripción opcional del material
     */
    private suspend fun procesarEscaneoConHUD(
        codigo: String,
        cantidadEscaneada: Int = 1,
        cantidadRestos: Int = 0,
        descripcion: String = "",
        fechaEtiqueta: String? = null,
        diasV: String? = null,
        almacen: String? = null,
        ubicacion: String? = null,
        tipoTarima: String? = null,
        feedbackEnabled: Boolean = false
    ) = withContext(Dispatchers.IO) {
        val inventoryRepository = repository

        val codigoNormalizado = codigo.trim().uppercase()
        val fechaCaducidadEpochDay =
            TpmRiskPolicy.calcularFechaCaducidadEpochDay(fechaEtiqueta, diasV)
        val riesgoTpm = TpmRiskPolicy.evaluar(fechaCaducidadEpochDay)
        val metadata = InventarioRepository.ScanMetadata(
            fechaEtiqueta = fechaEtiqueta,
            fechaCaducidadEpochDay = fechaCaducidadEpochDay,
            almacen = almacen,
            ubicacion = ubicacion,
            tipoTarima = tipoTarima,
            origenFecha =
                if (fechaCaducidadEpochDay != null) FechaOrigen.CALCULATED
                else FechaOrigen.UNKNOWN
        )

        inventoryRepository.registrarScan(
            codigo = codigoNormalizado,
            descripcion = descripcion.ifBlank { "Item Desconocido" },
            cantidad = cantidadEscaneada,
            restos = cantidadRestos,
            metadata = metadata
        )

        val totalAcumulado =
            inventoryRepository.obtenerItemPorCodigo(codigoNormalizado)?.cantidadTotal
                ?: cantidadEscaneada
        val itemSAP = inventarioReferencia[codigoNormalizado] ?: inventarioReferencia[codigo]
        val cantSAP = itemSAP?.cantidad ?: 0
        val diferencia = totalAcumulado - cantSAP
        val estado = calcularEstadoDiferencia(
            diferencia = diferencia,
            referenciaDisponible = inventarioReferencia.isNotEmpty()
        )

        val resultadoEscaneo = DiferenciaInventario(
            codigo = codigoNormalizado,
            descripcion = itemSAP?.descripcion ?: descripcion.ifBlank { "Item Desconocido" },
            cantidadReferencia = cantSAP,
            cantidadActual = totalAcumulado,
            diferencia = diferencia,
            timestamp = System.currentTimeMillis(),
            estado = estado
        )
        _ultimoItemHUD.value = resultadoEscaneo
        _ultimoEscaneo.value = resultadoEscaneo

        if (feedbackEnabled) {
            withContext(Dispatchers.Main) {
                when (estado) {
                    EstadoDiferencia.EXCESO -> feedbackManager.playScanError()
                    EstadoDiferencia.FALTANTE -> feedbackManager.playScanWarning()
                    EstadoDiferencia.COINCIDE -> feedbackManager.playScanMatch()
                    EstadoDiferencia.SIN_REFERENCIA -> feedbackManager.playSuccess()
                }
            }
        }

        Log.d(
            "InventarioVM",
            "HUD: $codigoNormalizado | SAP: $cantSAP | Escaneado: $totalAcumulado | Diff: $diferencia"
        )
        if (riesgoTpm.level == TpmRiskLevel.RIESGO_ALTO) {
            Log.w(
                "InventarioVM",
                "TPM en riesgo para $codigoNormalizado: " +
                    "${riesgoTpm.diasRestantes ?: 0} días restantes"
            )
        }
    }

    /**
     * Limpia el HUD después de mostrar la alerta.
     * Se llama automáticamente desde la UI después de 3.5 segundos.
     */
    fun limpiarHUD() {
        _ultimoItemHUD.value = null
    }

    /**
     * Actualiza la capacidad de los almacenes basada en lo escaneado.
     */
    private fun actualizarCapacidadAlmacen() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val almacenManager =
                    com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager(context)
                val totals = repository.obtenerTodosLosEscaneados()
                    .groupBy { it.almacen.orEmpty().trim().uppercase() }
                    .mapValues { (_, items) -> items.sumOf { it.cantidadTotal } }

                almacenManager.obtenerAlmacenes().forEach { almacen ->
                    val total = totals[almacen.nombreAlmacen.trim().uppercase()] ?: 0
                    almacenManager.actualizarPalletsEscaneados(
                        almacen.nombreAlmacen,
                        total
                    )
                }
            } catch (e: Exception) {
                Log.e("InventarioVM", "Error actualizando capacidad: ${e.message}")
            }
        }
    }

    /**
     * Obtiene el inventario de referencia cargado.
     * Útil para debugging o exportación.
     */
    fun getInventarioReferencia(): Map<String, ItemReferenciaSAP> = inventarioReferencia


    /**
     * Verifica si el inventario de referencia está cargado.
     */
    fun tieneInventarioReferencia(): Boolean = inventarioReferencia.isNotEmpty()

    /**
     * Punto único de entrada para los resultados de ML Kit y ZXing.
     * El detalle técnico se loguea; al operador se le muestra un mensaje claro.
     */
    private fun onScannerOutcome(outcome: ScannerOutcome) {
        scanFeedback.play(ScanFeedback.kindFor(outcome))
        when (outcome) {
            is ScannerOutcome.Read -> procesarResultadoEscaneo(outcome.raw)
            is ScannerOutcome.Cancelled -> showSnackbar("Escaneo cancelado")
            is ScannerOutcome.Failed -> {
                Log.w("InventarioVM", "Fallo del escáner ${outcome.engine}", outcome.cause)
                val mensaje = when {
                    outcome.willFallback ->
                        "El escáner de Google no está disponible. Abriendo la cámara alternativa…"
                    outcome.engine == ScannerEngine.ZXING ->
                        "No se pudo abrir la cámara. Revisa el permiso de cámara e inténtalo de nuevo."
                    else -> "No se pudo abrir el escáner. Inténtalo de nuevo."
                }
                showSnackbar(
                    mensaje,
                    if (outcome.willFallback) SnackbarType.INFO else SnackbarType.ERROR
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedbackManager.release()
    }

    /**
     * Inicia una toma desde cero, limpiando todas las sesiones locales y la referencia.
     */
    private fun resetSession() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.iniciarNuevaToma()
                val csvReset = csvService.resetearInventario()
                if (csvReset.isError) {
                    Log.w("InventarioVM", "No se pudo limpiar la copia CSV")
                }

                val referenceFile = java.io.File(context.filesDir, "inventario_almacen.csv")
                if (referenceFile.exists() && !referenceFile.delete()) {
                    throw java.io.IOException("No se pudo limpiar la referencia local")
                }

                inventarioReferencia = emptyMap()
                _ultimoItemHUD.value = null
                _ultimoEscaneo.value = null
                _uiState.update {
                    it.copy(
                        isReferenciaSistemaCargada = false,
                        totalFaltantes = 0,
                        totalSobrantes = 0,
                        itemsConDiferencia = 0,
                        isComparisonActive = false,
                        materialesEscaneados = emptyList(),
                        totalCajas = 0,
                        totalPalletsEscaneados = 0,
                        skuManual = "",
                        ubicacion = "",
                        totalPallets = "",
                        restos = ""
                    )
                }
                showSnackbar("Nueva toma lista: se eliminaron las capturas anteriores")
            } catch (e: Exception) {
                Log.e("InventarioVM", "Error al reiniciar sesión: ${e.message}")
                showSnackbar("No se pudo reiniciar la sesión", SnackbarType.ERROR)
            }
        }
    }

    private companion object {
        const val PREFS_APP_CONFIG = "app_config"
        const val KEY_ESCANEO_CONTINUO = "escaneo_continuo"
    }
}

/**
 * Data class para representar un item de referencia del sistema (SAP).
 * Se usa para búsquedas rápidas O(1) durante la validación de escaneos.
 */
data class ItemReferenciaSAP(
    val codigo: String,
    val descripcion: String,
    val cantidad: Int
)
