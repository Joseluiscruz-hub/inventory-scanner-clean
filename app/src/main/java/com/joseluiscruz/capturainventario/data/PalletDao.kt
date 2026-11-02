package com.joseluiscruz.capturainventario.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.joseluiscruz.capturainventario.model.Pallet

@Dao
interface PalletDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPallet(pallet: Pallet)

    @Query("SELECT * FROM pallets ORDER BY id DESC")
    fun getAllPallets(): LiveData<List<Pallet>>

    @Query("SELECT * FROM pallets")
    suspend fun getAllPalletsSync(): List<Pallet>

    @Delete
    suspend fun deletePallet(pallet: Pallet)

    @Query("SELECT * FROM pallets WHERE hu LIKE :query ORDER BY id DESC")
    fun searchPallets(query: String): LiveData<List<Pallet>>
}
