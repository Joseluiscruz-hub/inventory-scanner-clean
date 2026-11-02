package com.joseluiscruz.capturainventario.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: String = "OPERADOR"
)
