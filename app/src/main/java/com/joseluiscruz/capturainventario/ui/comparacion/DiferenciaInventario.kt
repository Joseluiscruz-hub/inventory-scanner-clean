package com.joseluiscruz.capturainventario.ui.comparacion

import com.joseluiscruz.capturainventario.model.EstadoColor

/**
 * Clase de datos para representar una diferencia encontrada en el inventario
 */
data class DiferenciaInventario(
    val codigo: String,
    val descripcion: String,
    val cantidadReferencia: Int,
    val cantidadActual: Int,
    val diferencia: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val estado: EstadoDiferencia = when {
        diferencia > 0 -> EstadoDiferencia.EXCESO
        diferencia < 0 -> EstadoDiferencia.FALTANTE
        else -> EstadoDiferencia.COINCIDE
    }
)

/**
 * Enum para clasificar el tipo de diferencia
 */
enum class EstadoDiferencia {
    COINCIDE,
    FALTANTE,
    EXCESO,
    /** Captura válida realizada antes de cargar una referencia SAP. */
    SIN_REFERENCIA
}

/**
 * Evita tratar un conteo ciego como exceso contra una referencia inexistente.
 */
fun calcularEstadoDiferencia(
    diferencia: Int,
    referenciaDisponible: Boolean
): EstadoDiferencia = when {
    !referenciaDisponible -> EstadoDiferencia.SIN_REFERENCIA
    diferencia > 0 -> EstadoDiferencia.EXCESO
    diferencia < 0 -> EstadoDiferencia.FALTANTE
    else -> EstadoDiferencia.COINCIDE
}

/** Mapea el estado operativo sin convertir un conteo ciego en una alerta SAP. */
fun DiferenciaInventario.toEstadoColor(): EstadoColor = when (estado) {
    EstadoDiferencia.SIN_REFERENCIA -> EstadoColor.NEUTRO
    EstadoDiferencia.COINCIDE -> EstadoColor.VERDE
    EstadoDiferencia.EXCESO,
    EstadoDiferencia.FALTANTE -> when (kotlin.math.abs(diferencia)) {
        in 0..3 -> EstadoColor.AMARILLO
        else -> EstadoColor.ROJO
    }
}
