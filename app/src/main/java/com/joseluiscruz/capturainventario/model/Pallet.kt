package com.joseluiscruz.capturainventario.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pallets")
data class Pallet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hu: String,
    val cantidad: Int = 1
)
