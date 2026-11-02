package com.joseluiscruz.capturainventario.ui.login.viewmodel

import app.cash.turbine.test
import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.joseluiscruz.capturainventario.ui.login.state.LoginEvent
import com.joseluiscruz.capturainventario.ui.login.state.LoginUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val authRepository: AuthRepository = mock()
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `login with empty fields should show error`() = runTest {
        viewModel.uiState.test {
            assertEquals(LoginUiState(), awaitItem()) // Initial state

            viewModel.onEvent(LoginEvent.OnLoginClick("", ""))

            val state = awaitItem()
            assertEquals("Por favor completa todos los campos", state.error)
            assertFalse(state.isLoading)
        }
    }

    @Test
    fun `login with valid credentials should succeed`() = runTest {
        val user = User("1", "admin", "admin@example.com")
        whenever(authRepository.login("admin", "1234")).thenReturn(Result.success(user))

        viewModel.uiState.test {
            assertEquals(LoginUiState(), awaitItem()) // Initial state

            viewModel.onEvent(LoginEvent.OnLoginClick("admin", "1234"))

            // First state: loading
            var state = awaitItem()
            assertTrue(state.isLoading)

            // Second state: success
            state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.loginSuccess)
            assertNull(state.error)
        }
    }

    @Test
    fun `login with invalid credentials should show error`() = runTest {
        whenever(authRepository.login("wrong", "wrong")).thenReturn(Result.failure(Exception("Credenciales inválidas")))

        viewModel.uiState.test {
            assertEquals(LoginUiState(), awaitItem()) // Initial state

            viewModel.onEvent(LoginEvent.OnLoginClick("wrong", "wrong"))

            // First state: loading
            var state = awaitItem()
            assertTrue(state.isLoading)

            // Second state: error
            state = awaitItem()
            assertFalse(state.isLoading)
            assertFalse(state.loginSuccess)
            assertEquals("Credenciales inválidas", state.error)
        }
    }
}
