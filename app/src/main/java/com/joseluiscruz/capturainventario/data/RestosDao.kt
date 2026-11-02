package com.joseluiscruz.capturainventario.data

import androidx.room.*
import com.joseluiscruz.capturainventario.model.Restos

@Dao
interface RestosDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResto(resto: Restos)

    @Query("SELECT * FROM restos ORDER BY id DESC")
    suspend fun getAllRestos(): List<Restos>

    @Delete
    suspend fun deleteResto(resto: Restos)

    @Query("SELECT * FROM restos WHERE material LIKE :query OR lote LIKE :query ORDER BY id DESC")
    suspend fun searchRestos(query: String): List<Restos>
}
