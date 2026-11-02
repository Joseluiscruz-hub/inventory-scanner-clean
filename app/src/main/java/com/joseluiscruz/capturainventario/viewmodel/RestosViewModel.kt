package com.joseluiscruz.capturainventario.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.AppDatabase
import com.joseluiscruz.capturainventario.model.Restos
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class RestosViewModel(application: Application) : AndroidViewModel(application) {

    sealed class ScanResult {
        object Success : ScanResult()
        object InvalidFormat : ScanResult()
        object MaterialNotFound : ScanResult()
        data class Error(val message: String) : ScanResult()
    }

    private val restosDao = AppDatabase.getDatabase(application).restosDao()
    private val sharedPreferences = application.getSharedPreferences("app_config", Context.MODE_PRIVATE)

    private val _restosList = MutableLiveData<List<Restos>>()
    val restosList: LiveData<List<Restos>> = _restosList

    private val _restosCount = MutableLiveData<Int>()
    val restosCount: LiveData<Int> = _restosCount

    private val _scanResult = MutableLiveData<ScanResult>()
    val scanResult: LiveData<ScanResult> = _scanResult

    private var lastDeletedResto: Restos? = null

    init {
        loadRestos()
    }

    private fun loadRestos() {
        viewModelScope.launch {
            val list = restosDao.getAllRestos()
            _restosList.postValue(list)
            _restosCount.postValue(list.size)
        }
    }

    fun guardarResto(scannedData: String, cantidad: Int) {
        if (scannedData.length < 18 || cantidad <= 0) {
            _scanResult.postValue(ScanResult.InvalidFormat)
            return
        }

        viewModelScope.launch {
            try {
                val materialId = scannedData.substring(0, 10).trim()
                val lote = scannedData.substring(10, 18).trim()

                val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                val currentDate = sdfDate.format(Date())
                val currentTime = sdfTime.format(Date())

                val usuario = sharedPreferences.getString("usuario", "N/A") ?: "N/A"
                val almacen = sharedPreferences.getString("almacen_seleccionado", "N/A") ?: "N/A"
                val centro = sharedPreferences.getString("centro_seleccionado", "N/A") ?: "N/A"

                val resto = Restos(
                    material = materialId,
                    lote = lote,
                    cantidad = cantidad,
                    fecha = currentDate,
                    hora = currentTime,
                    usuario = usuario,
                    almacen = almacen,
                    centro = centro
                )
                restosDao.insertResto(resto)
                loadRestos()
                _scanResult.postValue(ScanResult.Success)
            } catch (e: Exception) {
                _scanResult.postValue(ScanResult.Error(e.message ?: "Error desconocido"))
            }
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val list = if (query.isBlank()) {
                restosDao.getAllRestos()
            } else {
                restosDao.searchRestos("%$query%")
            }
            _restosList.postValue(list)
            _restosCount.postValue(list.size)
        }
    }

    fun deleteResto(resto: Restos) {
        lastDeletedResto = resto
        viewModelScope.launch {
            restosDao.deleteResto(resto)
            loadRestos()
        }
    }

    fun undoDelete() {
        lastDeletedResto?.let {
            viewModelScope.launch {
                restosDao.insertResto(it)
                loadRestos()
                lastDeletedResto = null
            }
        }
    }
}
