package com.joseluiscruz.capturainventario.ui.login.viewmodel

import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.joseluiscruz.capturainventario.ui.login.state.LoginEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelStateTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        authRepository = FakeAuthRepository()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `credenciales vacias muestran error sin invocar login del repositorio`() {
        viewModel.onEvent(LoginEvent.OnLoginClick("", ""))

        assertEquals("Por favor completa todos los campos", viewModel.uiState.value.error)
        assertEquals(0, authRepository.loginCalls)
    }

    @Test
    fun `OnErrorDismissed limpia el error actual`() {
        viewModel.onEvent(LoginEvent.OnLoginClick("", ""))
        viewModel.onEvent(LoginEvent.OnErrorDismissed)

        assertNull(viewModel.uiState.value.error)
    }

    private class FakeAuthRepository : AuthRepository {
        override val currentUser: StateFlow<User?> = MutableStateFlow(null)
        var loginCalls: Int = 0

        override suspend fun login(username: String, password: String): Result<User> {
            loginCalls += 1
            return Result.failure(UnsupportedOperationException("No se usa en esta prueba"))
        }

        override suspend fun logout() = Unit

        override suspend fun isUserLoggedIn(): Boolean = false
    }
}
