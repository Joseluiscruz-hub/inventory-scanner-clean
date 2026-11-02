package com.joseluiscruz.capturainventario.dashboard

data class ComparacionInventario(
    val sku: String,
    val descripcion: String,
    val tipoTarima: String,
    val escaneado: Int?,
    val inventario: Int?,
    val diferencia: Int?,
    val estado: String
) : java.io.Serializable
