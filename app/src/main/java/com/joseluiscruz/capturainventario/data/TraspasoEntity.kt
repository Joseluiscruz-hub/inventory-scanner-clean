package com.joseluiscruz.capturainventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "traspasos")
data class TraspasoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firestoreId: String,
    val sku: String,
    val descripcion: String,
    val almacenOrigen: String,
    val almacenDestino: String,
    val pallets: Int,
    val restos: Int,
    val tipoTarima: String,
    val operador: String,
    val fechaHora: String,
    val estado: String = "COMPLETADO",
    val sincronizado: Boolean = true
)

