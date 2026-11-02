package com.joseluiscruz.capturainventario.domain.model

/**
 * Representa los modos de escaneo disponibles en la aplicación.
 * Sellado para garantizar exhaustividad y type-safety.
 */
enum class ScanMode(
    val id: String,
    val displayName: String,
    val description: String
) {
    MANUAL(
        id = "manual",
        displayName = "Manual",
        description = "Ingrese SKU y datos manualmente"
    ),
    PALLET(
        id = "pallet",
        displayName = "Pallets",
        description = "Escaneo continuo de pallets (1:1)"
    ),
    RUMBA(
        id = "rumba",
        displayName = "Rumba",
        description = "Escaneo rápido tipo Rumba"
    ),
    DEFAULT(
        id = "default",
        displayName = "Completo",
        description = "Todos los campos habilitados"
    );

    companion object {
        fun fromString(value: String?): ScanMode {
            return when (value?.lowercase()) {
                "manual" -> MANUAL
                "pallet", "pallets" -> PALLET
                "rumba" -> RUMBA
                else -> DEFAULT
            }
        }
    }
}
