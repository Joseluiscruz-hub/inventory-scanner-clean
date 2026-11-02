package com.joseluiscruz.capturainventario.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rumbas")
data class Rumba(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val material: String,
    val lote: String,
    val cantidad: Int = 1
)
