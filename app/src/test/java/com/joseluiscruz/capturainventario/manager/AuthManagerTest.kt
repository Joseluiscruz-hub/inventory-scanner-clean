package com.joseluiscruz.capturainventario.manager

import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthManagerTest {

    @Test
    fun `init expone el usuario actual del repositorio compartido`() {
        val user = User(id = "1", username = "operador", email = "operador@example.com")
        AuthManager.init(FakeAuthRepository(user))

        assertEquals(user, AuthManager.currentUser.value)
    }

    @Test
    fun `isAdmin refleja el rol actual del usuario autenticado`() {
        AuthManager.init(FakeAuthRepository(User("1", "admin", "admin@example.com", role = "ADMIN")))
        assertTrue(AuthManager.isAdmin())

        AuthManager.init(FakeAuthRepository(User("2", "operador", "operador@example.com", role = "OPERADOR")))
        assertFalse(AuthManager.isAdmin())
    }

    private class FakeAuthRepository(initialUser: User?) : AuthRepository {
        private val state = MutableStateFlow(initialUser)

        override val currentUser: StateFlow<User?> = state

        override suspend fun login(username: String, password: String): Result<User> {
            return Result.failure(UnsupportedOperationException("No se usa en esta prueba"))
        }

        override suspend fun logout() {
            state.value = null
        }

        override suspend fun isUserLoggedIn(): Boolean = state.value != null
    }
}
