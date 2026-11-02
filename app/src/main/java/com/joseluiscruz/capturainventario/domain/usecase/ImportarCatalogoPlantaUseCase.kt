package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.data.CatalogoPlantaEntity
import com.joseluiscruz.capturainventario.data.repository.CatalogoPlantaRepository
import javax.inject.Inject

class ImportarCatalogoPlantaUseCase @Inject constructor(
    private val repository: CatalogoPlantaRepository
) {
    suspend operator fun invoke(planta: String, texto: String) {
        val lineas = texto.lines().filter { it.isNotBlank() }
        if (lineas.isEmpty()) return

        val items = mutableListOf<CatalogoPlantaEntity>()
        
        lineas.forEach { linea ->
            val separador = if (linea.contains('\t')) '\t' else ','
            val cols = linea.split(separador).map { it.trim() }
            
            if (cols.size >= 2) {
                try {
                    val skuIndex = if (cols[0].length < 4 && cols[0].toIntOrNull() != null) 1 else 0
                    val sku = cols.getOrNull(skuIndex) ?: return@forEach
                    if (sku.equals("Sku", ignoreCase = true)) return@forEach

                    items.add(
                        CatalogoPlantaEntity(
                            planta = planta,
                            sku = sku,
                            descripcion = cols.getOrNull(skuIndex + 1) ?: "",
                            tamanoMl = cols.getOrNull(skuIndex + 2)?.filter { it.isDigit() }?.toIntOrNull() ?: 0,
                            empaque = cols.getOrNull(skuIndex + 3) ?: "",
                            marca = cols.getOrNull(skuIndex + 4) ?: "",
                            botellasXCaja = cols.getOrNull(skuIndex + 5)?.toIntOrNull() ?: 0,
                            segmento = cols.getOrNull(skuIndex + 6) ?: "",
                            tpmDias = cols.getOrNull(skuIndex + 7)?.toIntOrNull() ?: 0,
                            cajasEnMerc = cols.getOrNull(skuIndex + 8)?.toIntOrNull() ?: 0,
                            cajasXTarima = cols.getOrNull(skuIndex + 9)?.toIntOrNull() ?: 0,
                            camasXTarima = cols.getOrNull(skuIndex + 10)?.toIntOrNull() ?: 0,
                            cajasXCama = cols.getOrNull(skuIndex + 11)?.toIntOrNull() ?: 0,
                            nivelEstiba = cols.getOrNull(skuIndex + 12)?.toDoubleOrNull() ?: 0.0
                        )
                    )
                } catch (e: Exception) {
                    // Log error if needed
                }
            }
        }

        if (items.isNotEmpty()) {
            repository.insertCatalogo(items)
        }
    }
}
