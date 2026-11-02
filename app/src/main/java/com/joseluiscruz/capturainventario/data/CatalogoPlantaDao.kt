package com.joseluiscruz.capturainventario.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogoPlantaDao {
    @Query("SELECT * FROM catalogos_planta WHERE planta = :planta ORDER BY sku ASC")
    fun getCatalogoPorPlanta(planta: String): Flow<List<CatalogoPlantaEntity>>

    @Query("SELECT DISTINCT planta FROM catalogos_planta")
    fun getPlantasDisponibles(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CatalogoPlantaEntity>)

    @Query("DELETE FROM catalogos_planta WHERE planta = :planta")
    suspend fun eliminarCatalogoPlanta(planta: String)

    @Query("SELECT * FROM catalogos_planta WHERE sku = :sku AND planta = :planta LIMIT 1")
    suspend fun getConfiguracionSku(sku: String, planta: String): CatalogoPlantaEntity?

    @Query("SELECT * FROM catalogos_planta WHERE sku = :sku LIMIT 1")
    suspend fun getAnyConfiguracionSku(sku: String): CatalogoPlantaEntity?
}
