package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.domain.model.ValidationResult
import com.joseluiscruz.capturainventario.model.MaterialItem

/**
 * Use case para validar un MaterialItem
 */
class ValidateMaterialUseCase {
    
    /**
     * Valida un material y retorna el resultado de la validación
     */
    fun execute(material: MaterialItem): ValidationResult {
        return when {
            material.sku.isBlank() -> ValidationResult.Error("El SKU no puede estar vacío")
            material.totalPallets.isNullOrBlank() -> ValidationResult.Error("La cantidad de pallets es obligatoria")
            material.totalPallets?.toIntOrNull() == null -> ValidationResult.Error("La cantidad debe ser un número")
            material.totalPallets?.toIntOrNull() ?: 0 <= 0 -> ValidationResult.Error("La cantidad debe ser mayor a cero")
            material.descripcion.isBlank() -> ValidationResult.Error("La descripción no puede estar vacía")
            material.ubicacion.isNullOrBlank() -> ValidationResult.Error("La ubicación es obligatoria")
            else -> ValidationResult.Success
        }
    }
}
