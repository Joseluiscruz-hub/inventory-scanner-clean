package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel

/**
 * Factory para crear el InventarioComposeViewModel con sus dependencias.
 */
class InventarioViewModelFactory(
    private val application: Application,
    private val csvService: CsvInventarioService,
    private val sharedViewModel: InventarioSharedViewModel,
    private val repository: InventarioRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioComposeViewModel::class.java)) {
            return InventarioComposeViewModel(
                application = application,
                csvService = csvService,
                sharedViewModel = sharedViewModel,
                repository = repository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
