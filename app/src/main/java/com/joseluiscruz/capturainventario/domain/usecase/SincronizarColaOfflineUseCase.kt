package com.joseluiscruz.capturainventario.domain.usecase

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.data.OfflineSyncStore
import com.joseluiscruz.capturainventario.data.SyncStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Drains the local offline queue (Room events with PENDING/FAILED syncStatus)
 * into Cloud Function registerInventoryMovement using quantity deltas and the
 * stable per-event operationId for idempotent retries.
 */
class SincronizarColaOfflineUseCase @Inject constructor(
    private val offlineSyncStore: OfflineSyncStore,
    private val firebaseRepository: FirebaseRepository,
    @ApplicationContext private val appContext: Context
) {
    data class Resultado(
        val enviados: Int,
        val fallidos: Int,
        val omitidos: Int
    )

    suspend operator fun invoke(limit: Int = 50): Resultado {
        val pendientes = offlineSyncStore.obtenerPendientesDeSync(limit)
        if (pendientes.isEmpty()) {
            return Resultado(0, 0, 0)
        }

        val deviceId = Settings.Secure.getString(
            appContext.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown-device"

        var enviados = 0
        var fallidos = 0
        var omitidos = 0

        for (evento in pendientes) {
            val palletsDelta = evento.cantidadTotal
            val restosDelta = evento.cantidadRestos
            if (palletsDelta == 0 && restosDelta == 0) {
                offlineSyncStore.actualizarSyncStatus(evento.operationId, SyncStatus.SENT.name)
                omitidos++
                continue
            }

            val ok = suspendCancellableCoroutine { cont ->
                firebaseRepository.registrarMovimientoDelta(
                    operationId = evento.operationId,
                    sku = evento.codigo,
                    descripcion = evento.descripcion,
                    almacen = evento.almacen?.takeIf { it.isNotBlank() } ?: "Desconocido",
                    tipoTarima = evento.tipoTarima?.takeIf { it.isNotBlank() } ?: "N/A",
                    palletsDelta = palletsDelta,
                    restosDelta = restosDelta,
                    deviceId = deviceId,
                    tipoMovimiento = evento.tipoMovimiento,
                    callback = { success, message ->
                        if (!success) {
                            Log.w(TAG, "Sync fail ${evento.operationId}: $message")
                        }
                        if (cont.isActive) cont.resume(success)
                    }
                )
            }

            if (ok) {
                offlineSyncStore.actualizarSyncStatus(evento.operationId, SyncStatus.SENT.name)
                enviados++
            } else {
                offlineSyncStore.actualizarSyncStatus(evento.operationId, SyncStatus.FAILED.name)
                fallidos++
            }
        }

        Log.i(TAG, "Cola offline: enviados=$enviados fallidos=$fallidos omitidos=$omitidos")
        return Resultado(enviados, fallidos, omitidos)
    }

    private companion object {
        const val TAG = "OfflineSync"
    }
}
