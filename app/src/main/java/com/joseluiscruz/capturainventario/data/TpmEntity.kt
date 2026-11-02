package com.joseluiscruz.capturainventario.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.joseluiscruz.capturainventario.model.RiesgoTPM

@Entity(tableName = "tpm_data")
data class TpmEntity(
    @PrimaryKey val sku: String,
    val descripcion: String,
    val fechaEtiqueta: Long?,
    val fechaCalculada: Long?,
    val diasRestantes: Int?,
    val riesgoNivel: RiesgoTPM,
    val lote: String?,
    val bodega: String
)

