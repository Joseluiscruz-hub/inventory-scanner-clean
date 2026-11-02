package com.joseluiscruz.capturainventario.model

/**
 * Métricas operativas para seguimiento de cierre de inventario en tiempo real.
 */
data class ProgresoCierre(
    val porcentajePallets: Float = 0f,
    val palletsRestantes: Int = 0,
    val skusPendientes: Int = 0,
    val skusCriticos: Int = 0,
    val skusTPMRiesgo: Int = 0
)

