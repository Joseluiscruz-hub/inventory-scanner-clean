package com.joseluiscruz.capturainventario.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Estado de conectividad del dispositivo expuesto como Flow. */
interface NetworkStatusMonitor {
    /** Emite `true` cuando la red por defecto tiene Internet validado; `false` en caso contrario. */
    val isOnline: Flow<Boolean>
}

/**
 * Implementación basada en [ConnectivityManager.NetworkCallback] sobre la red por defecto.
 *
 * Se considera "en línea" cuando la red tiene NET_CAPABILITY_INTERNET y NET_CAPABILITY_VALIDATED
 * (Android verificó que realmente hay salida a Internet, no solo Wi‑Fi asociado).
 */
class ConnectivityNetworkStatusMonitor(context: Context) : NetworkStatusMonitor {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    override val isOnline: Flow<Boolean> = callbackFlow {
        val cm = connectivityManager
        if (cm == null) {
            trySend(false)
            awaitClose { }
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.isUsableInternet())
            }

            override fun onLost(network: Network) {
                // En un cambio Wi‑Fi↔datos puede llegar onLost de la red anterior: se reevalúa la
                // red por defecto actual en vez de asumir "sin conexión".
                trySend(cm.currentlyOnline())
            }

            override fun onUnavailable() {
                trySend(false)
            }
        }

        // Valor inicial inmediato: el callback solo llega si hay una red por defecto.
        trySend(cm.currentlyOnline())

        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (e: RuntimeException) {
            // SecurityException sin ACCESS_NETWORK_STATE o límite de callbacks alcanzado.
            Log.w(TAG, "No se pudo registrar el NetworkCallback", e)
        }

        awaitClose {
            try {
                cm.unregisterNetworkCallback(callback)
            } catch (_: IllegalArgumentException) {
                // El callback nunca quedó registrado.
            }
        }
    }.distinctUntilChanged()

    private fun ConnectivityManager.currentlyOnline(): Boolean = try {
        val network = activeNetwork
        network != null && getNetworkCapabilities(network)?.isUsableInternet() == true
    } catch (e: RuntimeException) {
        Log.w(TAG, "No se pudo leer el estado de red", e)
        false
    }

    private fun NetworkCapabilities.isUsableInternet(): Boolean =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    private companion object {
        const val TAG = "NetworkStatusMonitor"
    }
}
