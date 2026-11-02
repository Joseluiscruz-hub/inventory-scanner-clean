package com.joseluiscruz.capturainventario.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.AppDatabase
import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.data.TraspasoEntity
import com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager
import com.joseluiscruz.capturainventario.model.MaterialItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class TraspasoState {
    object Idle : TraspasoState()
    object Loading : TraspasoState()
    data class Success(val mensaje: String) : TraspasoState()
    data class Error(val mensaje: String) : TraspasoState()
}

@HiltViewModel
class TraspasoViewModel @Inject constructor(
    private val repo: FirebaseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<TraspasoState>(TraspasoState.Idle)
    val state: StateFlow<TraspasoState> = _state

    private val _inventarioOrigen = MutableStateFlow<List<MaterialItem>>(emptyList())
    val inventarioOrigen: StateFlow<List<MaterialItem>> = _inventarioOrigen

    private val _almacenes = MutableStateFlow<List<String>>(emptyList())
    val almacenes: StateFlow<List<String>> = _almacenes

    fun cargarAlmacenes() {
        val manager = AlmacenCapacidadManager(context)
        _almacenes.value = manager.obtenerNombresAlmacenes()
    }

    fun cargarInventario(almacen: String) {
        _state.value = TraspasoState.Loading
        repo.descargarInventario { lista, msg ->
            if (lista != null) {
                _inventarioOrigen.value = lista.filter {
                    it.almacen.equals(almacen, ignoreCase = true)
                }
                _state.value = TraspasoState.Idle
            } else {
                _state.value = TraspasoState.Error(msg)
            }
        }
    }

    fun ejecutarTraspaso(
        item: MaterialItem,
        almacenDestino: String,
        palletsMover: Int,
        restosMover: Int,
        operador: String,
        deviceId: String
    ) {
        _state.value = TraspasoState.Loading
        repo.ejecutarTraspaso(
            sku = item.sku,
            tipoTarima = item.tipoTarima ?: "N/A",
            almacenOrigen = item.almacen ?: "",
            almacenDestino = almacenDestino,
            palletsMover = palletsMover,
            restosMover = restosMover,
            operador = operador,
            deviceId = deviceId,
            callback = { success, mensaje ->
                if (success) {
                    viewModelScope.launch {
                        guardarTraspasoEnRoom(item, almacenDestino, palletsMover, restosMover, operador)
                    }
                    sincronizarInventarioYAlmacenes()
                    _state.value = TraspasoState.Success(mensaje)
                } else {
                    _state.value = TraspasoState.Error(mensaje)
                }
            }
        )
    }

    private suspend fun guardarTraspasoEnRoom(
        item: MaterialItem,
        almacenDestino: String,
        palletsMover: Int,
        restosMover: Int,
        operador: String
    ) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.traspasoDao().insertar(
                TraspasoEntity(
                    firestoreId = "${item.sku}_${System.currentTimeMillis()}",
                    sku = item.sku,
                    descripcion = item.descripcion,
                    almacenOrigen = item.almacen ?: "",
                    almacenDestino = almacenDestino,
                    pallets = palletsMover,
                    restos = restosMover,
                    tipoTarima = item.tipoTarima ?: "",
                    operador = operador,
                    fechaHora = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                    estado = "COMPLETADO",
                    sincronizado = true
                )
            )
        } catch (e: Exception) {
            Log.e("TraspasoVM", "Error guardando traspaso en Room: ${e.message}")
        }
    }

    private fun sincronizarInventarioYAlmacenes() {
        repo.descargarInventario { lista, msg ->
            if (lista != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        // No sobrescribir el inventario local: puede contener escaneos pendientes.
                        actualizarPalletsEnAlmacenes(lista)
                    } catch (e: Exception) {
                        Log.e("TraspasoVM", "Error sincronizando post-traspaso: ${e.message}")
                    }
                }
            }
        }
    }

    private fun actualizarPalletsEnAlmacenes(lista: List<MaterialItem>) {
        val manager = AlmacenCapacidadManager(context)
        val almacenes = manager.obtenerAlmacenes().toMutableList()

        for (i in almacenes.indices) {
            val nombre = almacenes[i].nombreAlmacen
            val palletsReales = lista
                .filter { it.almacen.equals(nombre, ignoreCase = true) }
                .sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
            almacenes[i] = almacenes[i].copy(palletsEscaneados = palletsReales)
        }
        manager.guardarAlmacenes(almacenes)
    }

    fun resetState() {
        _state.value = TraspasoState.Idle
    }
}
