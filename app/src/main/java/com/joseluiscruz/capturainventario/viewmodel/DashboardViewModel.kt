package com.joseluiscruz.capturainventario.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * ViewModel encargado de proveer datos reactivos al Dashboard Premium.
 * Simula una carga remota y actualizaciones periódicas.
 */
class DashboardViewModel : ViewModel() {
    companion object {
        const val ERROR_REFRESH = "REFRESH_ERROR"
    }

    data class KpiState(
        val scanned: Int = 0,
        val syncOnline: Boolean = true,
        val lastUpdatedMillis: Long = 0L
    )

    data class ChartEntry(val label: String, val value: Float)

    private val _kpiState = MutableLiveData(KpiState())
    val kpiState: LiveData<KpiState> = _kpiState

    private val _chartData = MutableLiveData<List<ChartEntry>>(emptyList())
    val chartData: LiveData<List<ChartEntry>> = _chartData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        refreshData(initial = true)
    }

    fun refreshData(initial: Boolean = false) {
        if (_loading.value == true) return
        _loading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            // Simular latencia remota
            delay(if (initial) 600 else 900)
            val fail = Random.nextFloat() < 0.15f // 15% de fallo simulado
            if (fail) {
                _errorMessage.value = ERROR_REFRESH
                _loading.value = false
                return@launch
            }
            val scanned = Random.nextInt(850, 1800)
            val online = Random.nextBoolean() // mantener boolean
            val now = System.currentTimeMillis()
            _kpiState.value = KpiState(scanned, online, now)
            _chartData.value = generateChartData()
            _loading.value = false
        }
    }

    private fun generateChartData(): List<ChartEntry> {
        val labels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        return labels.map { l -> ChartEntry(l, Random.nextInt(50, 220).toFloat()) }
    }
}
