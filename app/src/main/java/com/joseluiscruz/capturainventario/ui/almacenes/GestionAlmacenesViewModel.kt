package com.joseluiscruz.capturainventario.ui.almacenes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager
import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GestionAlmacenesUiState(
    val almacenes: List<AlmacenCapacidad> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
) {
    val totalCapacidad: Int
        get() = almacenes.sumOf { it.capacidadMaxima }

    val totalOcupado: Int
        get() = almacenes.sumOf { it.palletsEscaneados }

    val totalDisponible: Int
        get() = (totalCapacidad - totalOcupado).coerceAtLeast(0)

    val porcentajeOcupacion: Double
        get() = if (totalCapacidad > 0) {
            (totalOcupado.toDouble() / totalCapacidad.toDouble()) * 100.0
        } else {
            0.0
        }
}

internal data class AlmacenInputValidation(
    val nombre: String = "",
    val capacidad: Int = 0,
    val error: String? = null
) {
    val isValid: Boolean
        get() = error == null
}

internal object AlmacenConfigurationRules {

    fun validate(
        nombre: String,
        capacidadText: String,
        existingNames: List<String>,
        originalName: String? = null
    ): AlmacenInputValidation {
        val normalizedName = nombre.trim().replace(Regex("\\s+"), " ")
        val capacidad = capacidadText.trim().toIntOrNull()

        return when {
            normalizedName.length < 2 -> AlmacenInputValidation(
                error = "Escribe un nombre de al menos 2 caracteres"
            )
            normalizedName.length > 40 -> AlmacenInputValidation(
                error = "El nombre no puede superar 40 caracteres"
            )
            capacidad == null || capacidad <= 0 -> AlmacenInputValidation(
                error = "La capacidad debe ser un número mayor que cero"
            )
            capacidad > 1_000_000 -> AlmacenInputValidation(
                error = "La capacidad máxima permitida es 1,000,000"
            )
            existingNames.any {
                it.equals(normalizedName, ignoreCase = true) &&
                    !it.equals(originalName, ignoreCase = true)
            } -> AlmacenInputValidation(
                error = "Ya existe un almacén con ese nombre"
            )
            else -> AlmacenInputValidation(
                nombre = normalizedName,
                capacidad = capacidad
            )
        }
    }
}

class GestionAlmacenesViewModel(application: Application) : AndroidViewModel(application) {

    private val manager = AlmacenCapacidadManager(application.applicationContext)
    private val csvService = CsvInventarioService(application.applicationContext)

    private val _uiState = MutableStateFlow(GestionAlmacenesUiState())
    val uiState: StateFlow<GestionAlmacenesUiState> = _uiState.asStateFlow()

    private var refreshJob: Job? = null

    init {
        refresh()
    }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isError = false) }
            runCatching { loadWarehouses() }
                .onSuccess { almacenes ->
                    _uiState.update {
                        it.copy(
                            almacenes = almacenes,
                            isLoading = false,
                            isSaving = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSaving = false,
                            message = "No se pudo cargar la configuración: ${error.message.orEmpty()}",
                            isError = true
                        )
                    }
                }
        }
    }

    fun saveWarehouse(
        nombre: String,
        capacidadText: String,
        originalName: String? = null
    ) {
        val validation = AlmacenConfigurationRules.validate(
            nombre = nombre,
            capacidadText = capacidadText,
            existingNames = _uiState.value.almacenes.map { it.nombreAlmacen },
            originalName = originalName
        )
        if (!validation.isValid) {
            _uiState.update {
                it.copy(message = validation.error, isError = true)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isError = false) }
            runCatching {
                withContext(Dispatchers.IO) {
                    val previousCurrent = manager.obtenerAlmacenActual()
                    val pallets = csvService.obtenerTotalPalletsPorAlmacen(validation.nombre)

                    if (!originalName.isNullOrBlank() &&
                        !originalName.equals(validation.nombre, ignoreCase = true)
                    ) {
                        manager.eliminarAlmacen(originalName)
                    }

                    manager.agregarOActualizarAlmacen(
                        AlmacenCapacidad(
                            nombreAlmacen = validation.nombre,
                            capacidadMaxima = validation.capacidad,
                            palletsEscaneados = pallets
                        )
                    )

                    if (previousCurrent.equals(originalName, ignoreCase = true)) {
                        manager.establecerAlmacenActual(validation.nombre)
                    }

                    loadWarehousesOnIo()
                }
            }.onSuccess { almacenes ->
                _uiState.update {
                    it.copy(
                        almacenes = almacenes,
                        isSaving = false,
                        message = if (originalName == null) {
                            "Almacén agregado correctamente"
                        } else {
                            "Almacén actualizado correctamente"
                        },
                        isError = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "No se pudo guardar el almacén: ${error.message.orEmpty()}",
                        isError = true
                    )
                }
            }
        }
    }

    fun deleteWarehouse(warehouse: AlmacenCapacidad) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, isError = false) }
            runCatching {
                withContext(Dispatchers.IO) {
                    manager.eliminarAlmacen(warehouse.nombreAlmacen)
                    loadWarehousesOnIo()
                }
            }.onSuccess { almacenes ->
                _uiState.update {
                    it.copy(
                        almacenes = almacenes,
                        isSaving = false,
                        message = "Almacén eliminado",
                        isError = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = "No se pudo eliminar el almacén: ${error.message.orEmpty()}",
                        isError = true
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private suspend fun loadWarehouses(): List<AlmacenCapacidad> =
        withContext(Dispatchers.IO) { loadWarehousesOnIo() }

    private suspend fun loadWarehousesOnIo(): List<AlmacenCapacidad> {
        val updated = manager.obtenerAlmacenes().map { warehouse ->
            warehouse.copy(
                palletsEscaneados = csvService.obtenerTotalPalletsPorAlmacen(
                    warehouse.nombreAlmacen
                ).coerceAtLeast(0)
            )
        }
        manager.guardarAlmacenes(updated)
        return manager.obtenerAlmacenes()
    }
}
