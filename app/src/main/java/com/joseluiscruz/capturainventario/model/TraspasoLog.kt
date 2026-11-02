package com.joseluiscruz.capturainventario.model

data class TraspasoLog(
    val id: String = "",
    val sku: String = "",
    val descripcion: String = "",
    val almacenOrigen: String = "",
    val almacenDestino: String = "",
    val pallets: Int = 0,
    val restos: Int = 0,
    val tipoTarima: String = "",
    val operador: String = "",
    val deviceId: String = "",
    val fechaHora: String = "",
    val estado: String = "COMPLETADO" // COMPLETADO | REVERTIDO
)

