package com.joseluiscruz.capturainventario.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TpmDao {

    @Query("SELECT * FROM tpm_data WHERE bodega = :bodega ORDER BY diasRestantes ASC")
    fun observarTPMPorBodega(bodega: String): Flow<List<TpmEntity>>

    @Query("SELECT * FROM tpm_data WHERE riesgoNivel IN ('CRITICO', 'ALERTA') ORDER BY diasRestantes ASC")
    fun observarSkusEnRiesgo(): Flow<List<TpmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTPM(items: List<TpmEntity>)
}

