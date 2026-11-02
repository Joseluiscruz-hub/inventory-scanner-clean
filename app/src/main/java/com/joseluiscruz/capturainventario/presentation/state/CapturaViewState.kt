package com.joseluiscruz.capturainventario.presentation.state

/**
 * Sealed class que representa los diferentes estados de la vista de captura
 */
sealed class CapturaViewState {
    object Loading : CapturaViewState()
    data class Content(val modo: ModoEscaneo) : CapturaViewState()
    data class Error(val mensaje: String) : CapturaViewState()
}

/**
 * Enum que define los diferentes modos de escaneo disponibles
 */
enum class ModoEscaneo {
    NORMAL,
    RUMBA,
    PALLET,
    MANUAL
}
