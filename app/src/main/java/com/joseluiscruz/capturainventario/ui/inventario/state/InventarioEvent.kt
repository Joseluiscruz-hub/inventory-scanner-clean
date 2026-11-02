package com.joseluiscruz.capturainventario.ui.inventario.state

import com.joseluiscruz.capturainventario.domain.model.ScanMode

/**
 * Eventos de usuario que pueden ocurrir en la pantalla de inventario.
 * Sellado para garantizar exhaustividad y type-safety.
 */
sealed class InventarioEvent {

    // Eventos de modo de escaneo
    data class OnScanModeSelected(val mode: ScanMode) : InventarioEvent()
    data class OnScanModeDialogDismissed(val mode: ScanMode) : InventarioEvent()

    // Eventos de entrada de datos
    data class OnSkuManualChanged(val value: String) : InventarioEvent()
    data class OnUbicacionChanged(val value: String) : InventarioEvent()
    data class OnTotalPalletsChanged(val value: String) : InventarioEvent()
    data class OnRestosChanged(val value: String) : InventarioEvent()
    data class OnFpcManualChanged(val value: String) : InventarioEvent()
    data class OnConManualChanged(val value: String) : InventarioEvent()

    // Eventos de acción principal
    data object OnScanClick : InventarioEvent()
    data object OnGuardarClick : InventarioEvent()
    data object OnSiguienteClick : InventarioEvent()
    data object OnExportarClick : InventarioEvent()
    data object OnEnviarCorreoClick : InventarioEvent()
    data object OnResumenClick : InventarioEvent()
    data object OnCargarClick : InventarioEvent()
    data object OnRestosButtonClick : InventarioEvent()
    data object OnCompararEnviarClick : InventarioEvent()

    // Eventos de selección de tarima
    data class OnTipoTarimaSelected(val tipo: String) : InventarioEvent()

    // Eventos de almacén
    data object OnAlmacenClick : InventarioEvent()
    data class OnAlmacenSelected(val almacen: String) : InventarioEvent()
    data object OnAlmacenDialogDismissed : InventarioEvent()

    // Eventos de escaneo QR
    data class OnScanResultReceived(val qrContent: String) : InventarioEvent()
    data object OnScanCancelled : InventarioEvent()
    /** Resultado normalizado de ML Kit o ZXing (lectura, cancelación o fallo). */
    data class OnScannerOutcome(
        val outcome: com.joseluiscruz.capturainventario.util.ScannerOutcome
    ) : InventarioEvent()
    /** Activa/desactiva el relanzado automático del escáner tras guardar (persistente). */
    data class OnEscaneoContinuoChanged(val enabled: Boolean) : InventarioEvent()

    // Eventos de ciclo de vida
    data object OnScreenLoaded : InventarioEvent()
    data object OnSnackbarShown : InventarioEvent()

    // Eventos de botones de modo rápido
    data object OnScanManualClick : InventarioEvent()
    data object OnScanPalletsClick : InventarioEvent()
    data object OnScanRumbaClick : InventarioEvent()

    // Diálogos de tarima
    data object OnTipoTarimaClick : InventarioEvent()
    data object OnTipoTarimaDialogDismissed : InventarioEvent()
    data object OnResetSession : InventarioEvent()

    // Nueva toma (menú secundario) con confirmación y exportación previa
    data object OnNuevaTomaClick : InventarioEvent()
    data object OnNuevaTomaDialogDismissed : InventarioEvent()
    data object OnNuevaTomaExportarClick : InventarioEvent()
    /** El operador eligió dónde guardar el CSV de respaldo (se exportan los registros de Room). */
    data class OnExportarCsvDestinoElegido(val uri: android.net.Uri) : InventarioEvent()
    data class OnExportacionCsvTerminada(val resultado: ResultadoExportacion) : InventarioEvent()

    /** El operador pulsó la acción del snackbar (p. ej. "Reintentar"). */
    data class OnSnackbarAction(val action: SnackbarAction) : InventarioEvent()

    // Eventos de comparación en tiempo real (activar/desactivar manualmente)
    data object OnComparisonStart : InventarioEvent()
    data object OnComparisonStop : InventarioEvent()
    data object OnLoadSystemInventory : InventarioEvent()
}
