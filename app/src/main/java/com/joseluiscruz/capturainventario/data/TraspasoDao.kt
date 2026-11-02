package com.joseluiscruz.capturainventario.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TraspasoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(traspaso: TraspasoEntity)

    @Query("SELECT * FROM traspasos ORDER BY fechaHora DESC")
    fun obtenerTodos(): LiveData<List<TraspasoEntity>>

    @Query("SELECT * FROM traspasos WHERE sku = :sku ORDER BY fechaHora DESC")
    fun obtenerPorSku(sku: String): LiveData<List<TraspasoEntity>>

    @Query("SELECT COUNT(*) FROM traspasos WHERE almacenOrigen = :almacen OR almacenDestino = :almacen")
    suspend fun contarMovimientosAlmacen(almacen: String): Int
}

