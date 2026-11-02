package com.joseluiscruz.capturainventario.manager

import android.content.Context
import android.content.SharedPreferences
import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

/**
 * Manager para gestionar la persistencia de los almacenes configurados
 */
class AlmacenCapacidadManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("almacen_capacidad", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_ALMACENES = "almacenes_configurados"
        private const val KEY_ALMACEN_ACTUAL = "almacen_actual_seleccionado"
    }

    /**
     * Guarda la lista de almacenes configurados
     */
    fun guardarAlmacenes(almacenes: List<AlmacenCapacidad>) {
        val json = gson.toJson(AlmacenConfigCodec.normalizar(almacenes))
        prefs.edit().putString(KEY_ALMACENES, json).apply()
    }

    /**
     * Obtiene la lista de almacenes configurados
     */
    fun obtenerAlmacenes(): List<AlmacenCapacidad> {
        val json = prefs.getString(KEY_ALMACENES, null) ?: return emptyList()
        return try {
            AlmacenConfigCodec.decodificar(json, gson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Agrega un nuevo almacén o actualiza uno existente
     */
    fun agregarOActualizarAlmacen(almacen: AlmacenCapacidad) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val almacenNormalizado = almacen.copy(
            nombreAlmacen = almacen.nombreAlmacen.trim(),
            capacidadMaxima = almacen.capacidadMaxima.coerceAtLeast(1),
            palletsEscaneados = almacen.palletsEscaneados.coerceAtLeast(0)
        )
        val index = almacenes.indexOfFirst {
            it.nombreAlmacen.equals(almacenNormalizado.nombreAlmacen, ignoreCase = true)
        }

        if (index >= 0) {
            almacenes[index] = almacenNormalizado
        } else {
            almacenes.add(almacenNormalizado)
        }

        guardarAlmacenes(almacenes)
    }

    /**
     * Elimina un almacén por nombre
     */
    fun eliminarAlmacen(nombreAlmacen: String) {
        val almacenes = obtenerAlmacenes().toMutableList()
        almacenes.removeAll { it.nombreAlmacen.equals(nombreAlmacen, ignoreCase = true) }
        guardarAlmacenes(almacenes)
        if (obtenerAlmacenActual().equals(nombreAlmacen, ignoreCase = true)) {
            prefs.edit().remove(KEY_ALMACEN_ACTUAL).apply()
        }
    }

    /**
     * Actualiza los pallets escaneados de un almacén específico
     */
    fun actualizarPalletsEscaneados(nombreAlmacen: String, palletsEscaneados: Int) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val index = almacenes.indexOfFirst {
            it.nombreAlmacen.equals(nombreAlmacen, ignoreCase = true)
        }

        if (index >= 0) {
            val almacenActualizado = almacenes[index].copy(palletsEscaneados = palletsEscaneados)
            almacenes[index] = almacenActualizado
            guardarAlmacenes(almacenes)
        }
    }

    /**
     * Actualiza los pallets escaneados de todos los almacenes basado en el inventario total
     */
    fun actualizarPalletsDesdeInventario(totalPalletsEscaneados: Int) {
        val almacenes = obtenerAlmacenes().toMutableList()
        val almacenActual = obtenerAlmacenActual()

        if (almacenActual != null && almacenes.isNotEmpty()) {
            val index = almacenes.indexOfFirst {
                it.nombreAlmacen.equals(almacenActual, ignoreCase = true)
            }
            if (index >= 0) {
                val almacenActualizado = almacenes[index].copy(palletsEscaneados = totalPalletsEscaneados)
                almacenes[index] = almacenActualizado
                guardarAlmacenes(almacenes)
            }
        }
    }

    /**
     * Establece el almacén actual donde se está escaneando
     */
    fun establecerAlmacenActual(nombreAlmacen: String) {
        prefs.edit().putString(KEY_ALMACEN_ACTUAL, nombreAlmacen.trim()).apply()
    }

    /**
     * Obtiene el nombre del almacén actual
     */
    fun obtenerAlmacenActual(): String? {
        return prefs.getString(KEY_ALMACEN_ACTUAL, null)
    }

    /**
     * Obtiene un almacén específico por nombre
     */
    fun obtenerAlmacenPorNombre(nombreAlmacen: String): AlmacenCapacidad? {
        return obtenerAlmacenes().find {
            it.nombreAlmacen.equals(nombreAlmacen, ignoreCase = true)
        }
    }

    /**
     * Obtiene solo los nombres de los almacenes configurados.
     * Fuente de verdad centralizada para toda la app.
     * @return Lista de nombres de almacenes (sin "Seleccionar Almacén")
     */
    fun obtenerNombresAlmacenes(): List<String> {
        return obtenerAlmacenes().map { it.nombreAlmacen }
    }

    /**
     * Calcula el resumen total de todos los almacenes
     */
    fun obtenerResumenTotal(): ResumenAlmacenes {
        val almacenes = obtenerAlmacenes()
        val totalPalletsEscaneados = almacenes.sumOf { it.palletsEscaneados }
        val totalCapacidad = almacenes.sumOf { it.capacidadMaxima }
        val saturacionPromedio = if (totalCapacidad > 0) {
            (totalPalletsEscaneados.toDouble() / totalCapacidad) * 100
        } else 0.0

        return ResumenAlmacenes(
            totalAlmacenes = almacenes.size,
            totalPalletsEscaneados = totalPalletsEscaneados,
            totalCapacidadConfigurada = totalCapacidad,
            saturacionPromedio = saturacionPromedio
        )
    }
}

/**
 * Codec tolerante a datos legacy o parcialmente corruptos de SharedPreferences.
 * Gson puede crear campos nulos aunque el modelo Kotlin sea no-null; por eso se
 * deserializa primero a un DTO nullable y sólo después se crea el modelo seguro.
 */
internal object AlmacenConfigCodec {

    private data class AlmacenPersistido(
        val nombreAlmacen: String? = null,
        val capacidadMaxima: Int? = null,
        val palletsEscaneados: Int? = null
    )

    fun decodificar(json: String, gson: Gson = Gson()): List<AlmacenCapacidad> {
        val type = object : TypeToken<List<AlmacenPersistido?>>() {}.type
        val persisted: List<AlmacenPersistido?> = gson.fromJson(json, type) ?: emptyList()
        return persisted.mapNotNull { raw ->
            val nombre = raw?.nombreAlmacen?.trim().orEmpty()
            val capacidad = raw?.capacidadMaxima ?: 0
            if (nombre.isBlank() || capacidad <= 0) {
                null
            } else {
                AlmacenCapacidad(
                    nombreAlmacen = nombre,
                    capacidadMaxima = capacidad,
                    palletsEscaneados = (raw?.palletsEscaneados ?: 0).coerceAtLeast(0)
                )
            }
        }.distinctBy { it.nombreAlmacen.uppercase(Locale.ROOT) }
            .sortedBy { it.nombreAlmacen.lowercase(Locale.ROOT) }
    }

    fun normalizar(almacenes: List<AlmacenCapacidad>): List<AlmacenCapacidad> =
        almacenes.mapNotNull { almacen ->
            val nombre = almacen.nombreAlmacen.trim()
            if (nombre.isBlank() || almacen.capacidadMaxima <= 0) {
                null
            } else {
                almacen.copy(
                    nombreAlmacen = nombre,
                    palletsEscaneados = almacen.palletsEscaneados.coerceAtLeast(0)
                )
            }
        }.distinctBy { it.nombreAlmacen.uppercase(Locale.ROOT) }
            .sortedBy { it.nombreAlmacen.lowercase(Locale.ROOT) }
}

/**
 * Data class para el resumen total de almacenes
 */
data class ResumenAlmacenes(
    val totalAlmacenes: Int,
    val totalPalletsEscaneados: Int,
    val totalCapacidadConfigurada: Int,
    val saturacionPromedio: Double
) {
    val saturacionPromedioFormateada: String
        get() = String.format("%.1f%%", saturacionPromedio)
}
