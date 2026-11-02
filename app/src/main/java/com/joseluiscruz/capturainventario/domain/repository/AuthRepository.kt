package com.joseluiscruz.capturainventario.domain.repository

import com.joseluiscruz.capturainventario.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    suspend fun isUserLoggedIn(): Boolean
}
