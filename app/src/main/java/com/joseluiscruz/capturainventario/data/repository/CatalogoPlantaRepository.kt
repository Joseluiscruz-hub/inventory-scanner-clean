package com.joseluiscruz.capturainventario.data.repository

import com.joseluiscruz.capturainventario.data.CatalogoPlantaDao
import com.joseluiscruz.capturainventario.data.CatalogoPlantaEntity
import kotlinx.coroutines.flow.Flow

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogoPlantaRepository @Inject constructor(private val dao: CatalogoPlantaDao) {
    fun getCatalogoPorPlanta(planta: String): Flow<List<CatalogoPlantaEntity>> = 
        dao.getCatalogoPorPlanta(planta)

    fun getPlantasDisponibles(): Flow<List<String>> = 
        dao.getPlantasDisponibles()

    suspend fun insertCatalogo(items: List<CatalogoPlantaEntity>) {
        dao.insertAll(items)
    }

    suspend fun eliminarCatalogoPlanta(planta: String) {
        dao.eliminarCatalogoPlanta(planta)
    }

    suspend fun getConfiguracionSku(sku: String, planta: String): CatalogoPlantaEntity? =
        dao.getConfiguracionSku(sku, planta)
}
