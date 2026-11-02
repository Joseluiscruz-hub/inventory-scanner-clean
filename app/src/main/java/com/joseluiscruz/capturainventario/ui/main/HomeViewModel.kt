package com.joseluiscruz.capturainventario.ui.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.repository.InventorySessionStore
import com.joseluiscruz.capturainventario.util.NetworkStatusMonitor
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Indicadores del home. `null` significa "aún sin dato" (se muestra "—"), nunca un valor inventado.
 */
data class HomeKpisUiState(
    /** SKUs distintos escaneados en la toma activa. */
    val escaneados: Int? = null,
    /** Registros en cola de sincronización (PENDING + FAILED), todas las sesiones. */
    val pendientesSync: Int? = null,
    /** Parte de [pendientesSync] cuyo último intento falló; `null` si no se pudo leer. */
    val fallidosSync: Int? = null,
    /**
     * SKUs de la toma activa con caducidad a menos de [TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS] días
     * (misma regla que el resumen de inventario).
     */
    val tpmEnRiesgo: Int? = null,
    /** Estado real de red; `null` mientras se determina. */
    val online: Boolean? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    inventarioDao: InventarioDao,
    sessionStore: InventorySessionStore,
    networkStatusMonitor: NetworkStatusMonitor
) : ViewModel() {

    private val escaneados: Flow<Int?> = sessionStore.sessionId
        .flatMapLatest { inventarioDao.contarItemsEscaneados(it) }
        .safe("escaneados")

    private val pendientes: Flow<Int?> = inventarioDao.contarPendientesDeSync().safe("pendientes de sync")

    private val fallidos: Flow<Int?> = inventarioDao.contarFallidosDeSync().safe("fallidos de sync")

    // La tabla tpm_data no se alimenta en ningún flujo; el riesgo TPM real se calcula con la
    // fecha de caducidad de cada escaneo (TpmRiskPolicy), así que el KPI sale de ahí.
    private val tpm: Flow<Int?> = sessionStore.sessionId
        .flatMapLatest { sessionId ->
            val hoyEpochDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            inventarioDao.contarSkusEnRiesgoTpm(
                sessionId = sessionId,
                limiteEpochDay = hoyEpochDay + TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS
            )
        }
        .safe("TPM en riesgo")

    private val online: Flow<Boolean?> = networkStatusMonitor.isOnline
        .map<Boolean, Boolean?> { it }
        .catch { e ->
            logError("Error observando la red", e)
            emit(null)
        }

    val uiState: StateFlow<HomeKpisUiState> =
        combine(escaneados, pendientes, fallidos, tpm, online) { esc, pend, fall, tpmCount, net ->
            HomeKpisUiState(
                escaneados = esc,
                pendientesSync = pend,
                fallidosSync = fall,
                tpmEnRiesgo = tpmCount,
                online = net
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeKpisUiState()
        )

    /** Un error de Room en un KPI no debe tumbar el resto: se degrada a "sin dato". */
    private fun Flow<Int>.safe(nombre: String): Flow<Int?> =
        map<Int, Int?> { it }.catch { e ->
            logError("Error observando KPI $nombre", e)
            emit(null)
        }

    private companion object {
        const val TAG = "HomeViewModel"

        fun logError(message: String, e: Throwable) {
            try {
                Log.e(TAG, message, e)
            } catch (_: RuntimeException) {
                // android.util.Log no existe en tests JVM (stub "not mocked"); el error ya se degradó.
            }
        }
    }
}
