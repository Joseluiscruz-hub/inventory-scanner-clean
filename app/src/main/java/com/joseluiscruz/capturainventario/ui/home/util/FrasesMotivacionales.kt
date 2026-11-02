package com.joseluiscruz.capturainventario.ui.home.util

object FrasesMotivacionales {
    private val frases = listOf(
        "Cada pallet cuenta.",
        "Captura con precisión. Cierra con confianza.",
        "Tu inventario, tu control.",
        "Donde hay orden, hay productividad.",
        "El control empieza por el escaneo.",
        "Precisión hoy. Resultados mañana.",
        "No escaneas… no controlas."
    )

    fun random(): String = frases.random()

    fun all(): List<String> = frases
}
