package com.joseluiscruz.capturainventario.ui.login.state

sealed class LoginEvent {
    data class OnLoginClick(val username: String, val password: String) : LoginEvent()
    data object OnErrorDismissed : LoginEvent()
}
