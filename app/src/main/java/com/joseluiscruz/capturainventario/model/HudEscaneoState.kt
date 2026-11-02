package com.joseluiscruz.capturainventario.model

/**
 * Estado visual del HUD reactivo para mostrar el último escaneo procesado.
 */
sealed class HudEscaneoState {
    object Idle : HudEscaneoState()

    data class Activo(
        val sku: String,
        val descripcion: String,
        val esperado: Int,
        val actual: Int,
        val delta: Int,
        val color: EstadoColor,
        val alertaTPM: AlertaTPM? = null
    ) : HudEscaneoState()
}

/**
 * Paleta semántica del HUD.
 */
enum class EstadoColor {
    NEUTRO,
    VERDE,
    AMARILLO,
    ROJO
}


