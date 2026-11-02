package com.joseluiscruz.capturainventario.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Enum para controlar el estado de sincronización con Firebase (Cola local).
 */
enum class SyncStatus {
    PENDING, // Listo para subir
    SENT,    // Ya en Firebase
    FAILED   // Error al subir (requiere reintento)
}

/**
 * Origen del dato de fecha para TPM/caducidad.
 */
enum class FechaOrigen {
    UNKNOWN,
    LABEL,
    CALCULATED
}

/**
 * Tipo de movimiento en el inventario por eventos.
 */
enum class TipoMovimiento {
    SCAN,
    ADJUSTMENT
}

/**
 * Entidad Room para almacenar los items escaneados.
 * AHORA BASADO EN EVENTOS (Append-Only). Cada escaneo es un registro único.
 */
@Entity(
    tableName = "inventario_escaneado",
    indices = [
        Index(value = ["sessionId"]), // Acelera las búsquedas por sesión
        Index(value = ["codigo"]),    // Acelera las agrupaciones por SKU
        Index(value = ["codigo", "fechaCaducidadEpochDay"]), // Búsquedas TPM por SKU
        Index(value = ["codigo", "almacen", "ubicacion"])   // Trazabilidad logística
    ]
)
data class InventarioEscaneadoEntity(
    @PrimaryKey
    val operationId: String = UUID.randomUUID().toString(), // UUID v4 único por evento
    
    val sessionId: String, // Ej. "MEX_20260412_TURNO1"
    
    val codigo: String, // El SKU o GroupKey (Ya NO es la Primary Key)
    val descripcion: String,
    val cantidadTotal: Int, // Puede ser positiva (suma) o negativa (corrección)
    val cantidadRestos: Int = 0,

    // Fecha original capturada de la etiqueta (sin normalizar)
    val fechaEtiqueta: String? = null,

    // Fecha normalizada para comparaciones (epoch day) y cálculo de próxima caducidad
    val fechaCaducidadEpochDay: Long? = null,

    // Metadatos de trazabilidad por evento
    val lote: String? = null,
    val almacen: String? = null,
    val ubicacion: String? = null,
    val tipoTarima: String? = null,
    val origenFecha: String = FechaOrigen.UNKNOWN.name,
    val tipoMovimiento: String = TipoMovimiento.SCAN.name,

    val syncStatus: String = SyncStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis(),
    val ultimoScanTimestamp: Long = System.currentTimeMillis()
)
