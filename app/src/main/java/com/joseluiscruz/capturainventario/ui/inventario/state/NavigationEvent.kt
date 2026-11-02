package com.joseluiscruz.capturainventario.ui.inventario.state

/**
 * Eventos de navegación emitidos por el ViewModel.
 * La Activity los observa y ejecuta la navegación real,
 * manteniendo el ViewModel libre de dependencias de Android UI.
 */
sealed class NavigationEvent {
    /** Navegar a la pantalla de resumen/exportación. */
    data object ToResumen : NavigationEvent()

    /** Navegar a la pantalla de captura para importar datos. */
    data object ToCapturaParaImportar : NavigationEvent()

    /** Navegar a la pantalla de gestión de restos. */
    data object ToRestos : NavigationEvent()

    /** Navegar a la comparación en tiempo real (limpia el back stack). */
    data object ToComparacionTiempoReal : NavigationEvent()

    /** Navegar a la pantalla de captura configurada para enviar por correo. */
    data object ToEnviarCorreo : NavigationEvent()

    /** Pedir a la Activity que abra el selector de archivo y exporte el CSV existente. */
    data object ExportarCsv : NavigationEvent()
}
