package com.joseluiscruz.capturainventario.ui.inventario.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.CatalogoPlantaEntity
import com.joseluiscruz.capturainventario.data.repository.CatalogoPlantaRepository
import com.joseluiscruz.capturainventario.domain.usecase.ImportarCatalogoPlantaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class CatalogoPlantaViewModel @Inject constructor(
    private val repository: CatalogoPlantaRepository,
    @ApplicationContext private val context: Context,
    private val importarCatalogoPlantaUseCase: ImportarCatalogoPlantaUseCase
) : ViewModel() {

    private val _plantas = MutableStateFlow<List<String>>(emptyList())
    val plantas: StateFlow<List<String>> = _plantas.asStateFlow()

    private val _catalogoActual = MutableStateFlow<List<CatalogoPlantaEntity>>(emptyList())
    val catalogoActual: StateFlow<List<CatalogoPlantaEntity>> = _catalogoActual.asStateFlow()

    private val _plantaSeleccionada = MutableStateFlow<String?>(null)
    val plantaSeleccionada: StateFlow<String?> = _plantaSeleccionada.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.getPlantasDisponibles().collect {
                    if (it.isEmpty()) {
                        _plantas.value = listOf("Norte")
                        seleccionarPlanta("Norte")
                    } else {
                        _plantas.value = it
                        if (_plantaSeleccionada.value == null) {
                            seleccionarPlanta(it.first())
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CatalogoVM", "Error inicializando plantas", e)
            }
        }
    }

    private var catalogoJob: Job? = null

    fun seleccionarPlanta(planta: String) {
        _plantaSeleccionada.value = planta
        // Guardar en preferencias para que el SharedViewModel lo use al iniciar
        val prefs = context.getSharedPreferences("app_config", 0)
        prefs.edit().putString("planta_activa", planta).apply()
        
        // Cancelar el colector anterior si existe para evitar duplicados
        catalogoJob?.cancel()
        catalogoJob = viewModelScope.launch {
            repository.getCatalogoPorPlanta(planta).collect {
                _catalogoActual.value = it
            }
        }
    }

    /**
     * Parsea el texto del catálogo pegado por el usuario.
     * Soporta tabuladores y comas.
     */
    fun importarCatalogoTexto(planta: String, texto: String) {
        viewModelScope.launch {
            importarCatalogoPlantaUseCase(planta, texto)
        }
    }

    fun eliminarPlanta(planta: String) {
        viewModelScope.launch {
            repository.eliminarCatalogoPlanta(planta)
        }
    }
}
