package com.joseluiscruz.capturainventario.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.AppDatabase
import kotlinx.coroutines.launch

class ResumenViewModel(application: Application) : AndroidViewModel(application) {
    private val rumbaDao = AppDatabase.getDatabase(application).rumbaDao()
    private val palletDao = AppDatabase.getDatabase(application).palletDao()

    private val _summaryList = MutableLiveData<List<Any>>()
    val summaryList: LiveData<List<Any>> = _summaryList

    private var originalList: List<Any> = emptyList()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            val rumbas = rumbaDao.getAllRumbasSync()
            val pallets = palletDao.getAllPalletsSync()
            originalList = rumbas + pallets
            _summaryList.postValue(originalList)
        }
    }

    fun search(query: String) {
        val filteredList = if (query.isBlank()) {
            originalList
        } else {
            originalList.filter {
                when (it) {
                    is com.joseluiscruz.capturainventario.model.Rumba -> it.material.contains(query, true) || it.lote.contains(query, true)
                    is com.joseluiscruz.capturainventario.model.Pallet -> it.hu.contains(query, true)
                    else -> false
                }
            }
        }
        _summaryList.postValue(filteredList)
    }
}
