package com.joseluiscruz.capturainventario.ui.comparacion

import com.joseluiscruz.capturainventario.data.TpmDao
import com.joseluiscruz.capturainventario.model.AlertaTPM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Manager reactivo para alertas TPM activadas por el SKU recientemente escaneado.
 */
class TPMMonitorManager(
    private val tpmDao: TpmDao,
    private val inventarioManager: InventarioTiempoRealManager
) {

    val alertasTPMActivas: Flow<List<AlertaTPM>> = combine(
        inventarioManager.ultimoItemEscaneado,
        tpmDao.observarSkusEnRiesgo()
    ) { ultimoItem, skusRiesgo ->
        val sku = ultimoItem?.codigo ?: return@combine emptyList()

        skusRiesgo
            .asSequence()
            .filter { it.sku == sku }
            .map { tpm ->
                AlertaTPM(
                    sku = tpm.sku,
                    descripcion = tpm.descripcion,
                    lote = tpm.lote,
                    bodega = tpm.bodega,
                    diasRestantes = tpm.diasRestantes,
                    mensaje = when {
                        tpm.diasRestantes == null -> "TPM sin fecha calculada"
                        tpm.diasRestantes <= 0 -> "TPM vencido: atender de inmediato"
                        else -> "TPM en riesgo: ${tpm.diasRestantes} días restantes"
                    }
                )
            }
            .toList()
    }
}

