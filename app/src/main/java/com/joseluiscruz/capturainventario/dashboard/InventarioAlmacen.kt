package com.joseluiscruz.capturainventario.dashboard

/**
 * Clase de datos que representa el inventario disponible en el almacén del sistema.
 * Se usa para comparar con el inventario escaneado.
 */
data class InventarioAlmacen(
    val sku: String,
    val descripcion: String,
    val disponible: Int,
    val centro: String
)

