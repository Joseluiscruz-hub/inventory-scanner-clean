package com.joseluiscruz.capturainventario.manager

import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * AuthManager — Punto de acceso global al estado de autenticación.
 *
 * Se inicializa desde Application/Activity con la misma instancia de [AuthRepository]
 * que consume la capa de login para mantener una sola fuente de verdad de la sesión.
 * El flujo [currentUser] es reactivo; el rol se obtiene dinámicamente desde
 * Firebase Custom Claims en vez de inferirlo del prefijo del correo.
 */
object AuthManager {
    private var _authRepository: AuthRepository? = null

    /**
     * Inicializa AuthManager con el repositorio de autenticación.
     * Debe llamarse una vez desde la Activity/Application principal.
     */
    fun init(authRepository: AuthRepository) {
        _authRepository = authRepository
    }

    private val authRepository: AuthRepository
        get() = checkNotNull(_authRepository) {
            "AuthManager no fue inicializado. Llama AuthManager.init() desde Application."
        }

    val currentUser: StateFlow<User?> get() = authRepository.currentUser

    fun isAdmin(): Boolean {
        return currentUser.value?.role == "ADMIN"
    }
}
