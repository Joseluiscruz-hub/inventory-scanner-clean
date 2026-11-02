package com.joseluiscruz.capturainventario.model

data class AlmacenCapacidad(
    val nombreAlmacen: String,
    val capacidadMaxima: Int,
    val palletsEscaneados: Int = 0
) {
    val porcentajeSaturacion: Double
        get() = if (capacidadMaxima > 0) (palletsEscaneados.toDouble() / capacidadMaxima) * 100 else 0.0

    val porcentajeSaturacionFormateado: String
        get() = String.format("%.1f%%", porcentajeSaturacion)

    val estadoSaturacion: EstadoSaturacion
        get() = when {
            porcentajeSaturacion >= 90 -> EstadoSaturacion.CRITICO
            porcentajeSaturacion >= 75 -> EstadoSaturacion.ALTO
            porcentajeSaturacion >= 50 -> EstadoSaturacion.MEDIO
            else -> EstadoSaturacion.BAJO
        }
}

enum class EstadoSaturacion(val color: String, val descripcion: String) {
    BAJO("#4CAF50", "Capacidad óptima"),
    MEDIO("#FF9800", "Capacidad moderada"),
    ALTO("#FF5722", "Capacidad alta"),
    CRITICO("#F44336", "Capacidad crítica")
}
