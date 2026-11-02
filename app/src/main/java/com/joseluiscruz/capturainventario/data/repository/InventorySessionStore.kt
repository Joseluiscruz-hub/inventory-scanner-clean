package com.joseluiscruz.capturainventario.data.repository

import android.content.Context
import com.joseluiscruz.capturainventario.data.InventarioDao
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Fuente persistente del identificador de la toma activa.
 *
 * En la primera ejecución después de actualizar la aplicación se adopta la
 * sesión más reciente guardada en Room. De esta forma un inventario iniciado
 * con una versión anterior no desaparece al volver a abrir la app.
 */
interface InventorySessionStore {
    /** Emite únicamente cuando la sesión persistida o legacy ya fue resuelta. */
    val sessionId: Flow<String>

    /** Espera la adopción inicial sin bloquear el hilo principal. */
    suspend fun awaitSessionId(): String

    /** Persiste y publica una sesión nueva de forma síncrona. */
    suspend fun startNewSession(): String
}

class SharedPreferencesInventorySessionStore internal constructor(
    context: Context,
    private val dao: InventarioDao,
    private val ioDispatcher: CoroutineDispatcher
) : InventorySessionStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val lock = Any()
    private val persistedSessionId = preferences.getString(KEY_ACTIVE_SESSION_ID, null)
        ?.takeIf(String::isNotBlank)
    private val mutableSessionId = MutableStateFlow(persistedSessionId)
    private val initialization = CompletableDeferred<String>()
    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    override val sessionId: Flow<String> = mutableSessionId.filterNotNull()

    init {
        if (persistedSessionId != null) {
            initialization.complete(persistedSessionId)
        } else {
            scope.launch {
                try {
                    val resolvedSessionId = dao.obtenerSesionMasReciente()
                        ?.takeIf(String::isNotBlank)
                        ?: createSessionId()
                    synchronized(lock) {
                        persist(resolvedSessionId)
                        mutableSessionId.value = resolvedSessionId
                    }
                    initialization.complete(resolvedSessionId)
                } catch (error: Exception) {
                    initialization.completeExceptionally(error)
                }
            }
        }
    }

    override suspend fun awaitSessionId(): String {
        initialization.await()
        return checkNotNull(mutableSessionId.value) {
            "La sesión activa no quedó disponible después de inicializarse"
        }
    }

    override suspend fun startNewSession(): String {
        awaitSessionId()
        return withContext(ioDispatcher) {
            synchronized(lock) {
                val newSessionId = createSessionId()
                persist(newSessionId)
                mutableSessionId.value = newSessionId
                newSessionId
            }
        }
    }

    private fun persist(sessionId: String) {
        check(
            preferences.edit()
                .putString(KEY_ACTIVE_SESSION_ID, sessionId)
                .commit()
        ) {
            "No se pudo persistir la sesión activa de inventario"
        }
    }

    private fun createSessionId(): String =
        "SESION_${System.currentTimeMillis()}_${UUID.randomUUID()}"

    private companion object {
        const val PREFERENCES_NAME = "inventory_session"
        const val KEY_ACTIVE_SESSION_ID = "active_session_id"
    }
}
