package com.joseluiscruz.capturainventario.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restos")
data class Restos(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val material: String,
    val lote: String,
    val cantidad: Int,
    val fecha: String,
    val hora: String,
    val usuario: String,
    val almacen: String,
    val centro: String
)
