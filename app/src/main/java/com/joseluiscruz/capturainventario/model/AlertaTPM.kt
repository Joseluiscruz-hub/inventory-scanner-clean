package com.joseluiscruz.capturainventario.model

/**
 * Alerta ligera de TPM/caducidad para UI reactiva.
 *
 * Esta representación es independiente de la capa de datos y puede enriquecerse
 * posteriormente con riesgo, lote y bodega sin romper la UI de fase 1.
 */
data class AlertaTPM(
    val sku: String,
    val descripcion: String,
    val lote: String? = null,
    val bodega: String? = null,
    val diasRestantes: Int? = null,
    val mensaje: String? = null
)

