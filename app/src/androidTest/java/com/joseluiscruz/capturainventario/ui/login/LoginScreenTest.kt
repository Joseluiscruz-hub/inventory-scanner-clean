package com.joseluiscruz.capturainventario.ui.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.joseluiscruz.capturainventario.ui.login.state.LoginUiState
import com.joseluiscruz.capturainventario.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test

class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_showsInitialState() {
        composeTestRule.setContent {
            AppTheme {
                LoginScreen(
                    uiState = LoginUiState(),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithText("BIENVENIDO").assertIsDisplayed()
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
        composeTestRule.onNodeWithText("INGRESAR").assertIsDisplayed()
    }

    @Test
    fun loginScreen_showsLoading() {
        composeTestRule.setContent {
            AppTheme {
                LoginScreen(
                    uiState = LoginUiState(isLoading = true),
                    onEvent = {}
                )
            }
        }

        // CircularProgressIndicator doesn't have text, but we can check if button is disabled or if we use a testTag
        // For now, let's just check if the text "INGRESAR" is NOT displayed because it's replaced by the indicator
        composeTestRule.onNodeWithText("INGRESAR").assertDoesNotExist()
    }

    @Test
    fun loginScreen_showsError() {
        val errorMessage = "Credenciales inválidas"
        composeTestRule.setContent {
            AppTheme {
                LoginScreen(
                    uiState = LoginUiState(error = errorMessage),
                    onEvent = {}
                )
            }
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
