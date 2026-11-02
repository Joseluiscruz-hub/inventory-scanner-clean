package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import javax.inject.Inject

class LimpiarInventarioUseCase @Inject constructor(
    private val repository: InventarioRepository
) {
    suspend operator fun invoke() {
        repository.iniciarNuevaToma()
    }
}
