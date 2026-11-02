package com.joseluiscruz.capturainventario.domain.model

/**
 * Sealed class que representa el resultado de una validación
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val mensaje: String) : ValidationResult()
}
