package com.joseluiscruz.capturainventario.ui.inventario.state

import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.model.MaterialItem

/**
 * Representa los datos escaneados de un código QR.
 */
data class ScannedData(
    val sku: String = "",
    val dp: String = "",
    val cxPal: String = "",
    val fpc: String = "",
    val con: String = "",
    val centro: String = "",
    val linea: String = "",
    val op: String = "",
    val fProd: String = "",
    val diasV: String = ""
)

/**
 * Estado inmutable de la UI para la pantalla de inventario.
 * Centraliza todo el estado para hacer la UI predecible y testeable.
 */
data class InventarioUiState(
    // Modo de escaneo actual
    val scanMode: ScanMode = ScanMode.DEFAULT,

    // Datos de entrada manuales
    val skuManual: String = "",
    val ubicacion: String = "",
    val totalPallets: String = "",
    val restos: String = "",
    val fpcManual: String = "",
    val conManual: String = "",

    // Datos escaneados (solo lectura)
    val scannedData: ScannedData = ScannedData(),

    // Estado de la UI
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoadingReferenciaSap: Boolean = false,
    val snackbarMessage: String? = null,
    val snackbarType: SnackbarType = SnackbarType.INFO,
    /** Acción opcional del snackbar (p. ej. "Reintentar"). */
    val snackbarAction: SnackbarAction? = null,
    val errorMessage: String? = null,

    // Diálogos
    val showAlmacenDialog: Boolean = false,
    val showScanModeDialog: Boolean = false,
    val showTipoTarimaDialog: Boolean = false,
    /** Confirmación de "Nueva toma · borrar capturas" (menú secundario). */
    val showNuevaTomaDialog: Boolean = false,
    /** Registros que se borrarían / sin sincronizar; `null` = cargando o no disponible. */
    val nuevaTomaTotalRegistros: Int? = null,
    val nuevaTomaSinSincronizar: Int? = null,
    val nuevaTomaCargando: Boolean = false,

    // Selección de almacén
    val selectedAlmacen: String = "Seleccionar Almacén",
    val almacenesDisponibles: List<String> = emptyList(),

    // Tipo de tarima seleccionada
    val tipoTarimaSeleccionada: String? = null,

    // Totales
    val totalCajas: Int = 0,
    val totalPalletsEscaneados: Int = 0,

    // Información de Producto Terminado (PT)
    val esProductoTerminado: Boolean = false,
    val esMaterial: Boolean = false,
    val cajasXPalletPT: Int = 0,
    val tarimaPlasticaSku: String = "",

    // Habilitación de campos (se calcula según el modo)
    val isSkuManualEnabled: Boolean = true,
    val isUbicacionEnabled: Boolean = true,
    val isTotalPalletsEnabled: Boolean = true,
    val isRestosEnabled: Boolean = true,

    // Visibilidad de campos escaneados
    val showScannedFields: Boolean = true,

    // Datos de comparación (Tiempo Real)
    val totalFaltantes: Int = 0,
    val totalSobrantes: Int = 0,
    val itemsConDiferencia: Int = 0,
    val isComparisonActive: Boolean = false,
    /** Relanza el escáner tras un guardado exitoso (persistido en SharedPreferences "app_config"). */
    val escaneoContinuo: Boolean = false,
    val isReferenciaSistemaCargada: Boolean = false,

    // Lista de materiales escaneados (para navegación con Shared Transition)
    val materialesEscaneados: List<MaterialItem> = emptyList()
) {
    /**
     * Calcula si el botón de escaneo principal debe estar habilitado.
     */
    val isScanButtonEnabled: Boolean = !isLoading && !isSaving

    /**
     * Calcula si el botón de guardar debe estar habilitado.
     * Basta un SKU manual **o** escaneado (en RUMBA el SKU llega del QR y el campo manual está
     * deshabilitado); es la misma condición que valida guardarMaterial().
     */
    val isGuardarButtonEnabled: Boolean =
        !isLoading && !isSaving && (skuManual.isNotBlank() || scannedData.sku.isNotBlank())
}

/** Acciones que un snackbar de error puede ofrecer al operador. */
enum class SnackbarAction(val label: String) {
    REINTENTAR_GUARDADO("Reintentar"),
    REINTENTAR_SAP("Reintentar"),
    REESCANEAR("Escanear"),
    REINTENTAR_EXPORTACION("Reintentar")
}

/** Resultado de la exportación CSV lanzada desde la pantalla de captura. */
sealed interface ResultadoExportacion {
    data class Exitosa(val registros: Int) : ResultadoExportacion
    data class Fallida(val sinDatos: Boolean) : ResultadoExportacion
    data object Cancelada : ResultadoExportacion
}

enum class SnackbarType {
    INFO,
    SUCCESS,
    ERROR
}
