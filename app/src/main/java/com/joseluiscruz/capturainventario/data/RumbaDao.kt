package com.joseluiscruz.capturainventario.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.joseluiscruz.capturainventario.model.Rumba

@Dao
interface RumbaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRumba(rumba: Rumba)

    @Query("SELECT * FROM rumbas ORDER BY id DESC")
    fun getAllRumbas(): LiveData<List<Rumba>>

    @Query("SELECT * FROM rumbas")
    suspend fun getAllRumbasSync(): List<Rumba>

    @Delete
    suspend fun deleteRumba(rumba: Rumba)

    @Query("SELECT * FROM rumbas WHERE material LIKE :query ORDER BY id DESC")
    fun searchRumbas(query: String): LiveData<List<Rumba>>
}
