package com.joseluiscruz.capturainventario.model

/**
 * Model class representing an inventory item for comparison purposes.
 * Used specifically for comparing system inventory vs scanned inventory.
 */
data class InventarioItem(
    val sku: String,
    val descripcion: String = "",
    val librUtiliz: Int = 0, // Cantidad en el sistema (libros utilizados)
    val totalPallets: Int = 0, // Cantidad total (para acumular)
    val pallets: Int = 0, // Cantidad específica para comparación
    val tipoTarima: String = "",
    val ubicacion: String = "",
    val centro: String = "",
    val linea: String = ""
) {
    /**
     * Constructor conveniente para crear desde MaterialItem (inventario escaneado)
     */
    constructor(materialItem: MaterialItem) : this(
        sku = materialItem.sku,
        descripcion = materialItem.descripcion,
        librUtiliz = 0, // No aplica para inventario escaneado
        totalPallets = materialItem.totalPallets?.toIntOrNull() ?: 0,
        pallets = materialItem.totalPallets?.toIntOrNull() ?: 0,
        tipoTarima = materialItem.tipoTarima ?: "",
        ubicacion = materialItem.ubicacion ?: "",
        centro = materialItem.centro,
        linea = materialItem.linea
    )

    /**
     * Constructor para crear desde datos del sistema (CSV de inventario del sistema)
     */
    constructor(
        sku: String,
        descripcion: String,
        cantidadSistema: Int,
        tipoTarima: String = "",
        ubicacion: String = "",
        centro: String = "",
        linea: String = ""
    ) : this(
        sku = sku,
        descripcion = descripcion,
        librUtiliz = cantidadSistema,
        totalPallets = cantidadSistema,
        pallets = cantidadSistema,
        tipoTarima = tipoTarima,
        ubicacion = ubicacion,
        centro = centro,
        linea = linea
    )
}
