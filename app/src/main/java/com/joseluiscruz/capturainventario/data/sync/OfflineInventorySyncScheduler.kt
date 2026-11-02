package com.joseluiscruz.capturainventario.data.sync

import android.util.Log
import com.joseluiscruz.capturainventario.domain.usecase.SincronizarColaOfflineUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Best-effort background drain of the Room offline queue.
 * Retries reuse the same operationId (Room PK) for server idempotency.
 */
@Singleton
class OfflineInventorySyncScheduler @Inject constructor(
    private val sincronizarColaOfflineUseCase: SincronizarColaOfflineUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    fun kick(reason: String = "manual") {
        scope.launch {
            mutex.withLock {
                try {
                    val result = sincronizarColaOfflineUseCase()
                    Log.d(
                        TAG,
                        "kick($reason) enviados=${result.enviados} fallidos=${result.fallidos} omitidos=${result.omitidos}"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "kick($reason) failed: ${e.message}", e)
                }
            }
        }
    }

    private companion object {
        const val TAG = "OfflineSyncScheduler"
    }
}
