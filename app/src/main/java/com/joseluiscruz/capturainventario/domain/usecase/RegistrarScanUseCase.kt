package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.sync.OfflineInventorySyncScheduler
import javax.inject.Inject

class RegistrarScanUseCase @Inject constructor(
    private val repository: InventarioRepository,
    private val offlineSyncScheduler: OfflineInventorySyncScheduler
) {
    suspend operator fun invoke(codigo: String, descripcion: String, cantidad: Int = 1) {
        repository.registrarScan(codigo, descripcion, cantidad)
        // Best-effort drain: retries reuse Room operationId for idempotency.
        offlineSyncScheduler.kick("after-scan")
    }
}
