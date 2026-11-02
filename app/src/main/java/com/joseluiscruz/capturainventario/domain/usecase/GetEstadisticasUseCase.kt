package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.viewmodel.EstadisticasSesion
import javax.inject.Inject

class GetEstadisticasUseCase @Inject constructor() {
    operator fun invoke(
        lista: List<DiferenciaInventario>,
        totalSistema: Int
    ): EstadisticasSesion {
        val totalSKUs = lista.size
        val coincidencias = lista.count { it.estado == EstadoDiferencia.COINCIDE }
        val faltantes = lista.count { it.estado == EstadoDiferencia.FALTANTE }
        val excesos = lista.count { it.estado == EstadoDiferencia.EXCESO }
        val efectividad = if (totalSKUs > 0) {
            (coincidencias * 100) / totalSKUs
        } else 0

        // Calculamos items escaneados vs total del sistema
        val itemsEscaneados = lista.count { it.cantidadActual > 0 }
        val skusPendientes = totalSistema - itemsEscaneados

        return EstadisticasSesion(
            totalSKUs = totalSKUs,
            coincidencias = coincidencias,
            faltantes = faltantes,
            excesos = excesos,
            efectividad = efectividad,
            itemsEscaneados = itemsEscaneados,
            totalSistema = totalSistema,
            skusPendientes = skusPendientes,
            timestampCalculo = System.currentTimeMillis()
        )
    }
}
