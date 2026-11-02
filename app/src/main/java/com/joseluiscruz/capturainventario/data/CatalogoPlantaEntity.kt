package com.joseluiscruz.capturainventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * Entidad que representa un catálogo de productos específico por planta.
 * Permite tener diferentes configuraciones de estibaje y TPM según la ubicación.
 */
@Entity(
    tableName = "catalogos_planta",
    indices = [
        Index(value = ["planta", "sku"], unique = true),
        Index(value = ["sku"])
    ]
)
data class CatalogoPlantaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planta: String, // Ejemplo: "Norte"
    val sku: String,
    val descripcion: String,
    val tamanoMl: Int = 0,
    val empaque: String = "",
    val marca: String = "",
    val botellasXCaja: Int = 0,
    val segmento: String = "",
    val tpmDias: Int = 0, // Días de vida útil TPM
    val cajasEnMerc: Int = 0,
    val cajasXTarima: Int = 0, // Este es el dato crítico para conversión
    val camasXTarima: Int = 0,
    val cajasXCama: Int = 0,
    val nivelEstiba: Double = 0.0,
    val timestampActualizacion: Long = System.currentTimeMillis()
)
