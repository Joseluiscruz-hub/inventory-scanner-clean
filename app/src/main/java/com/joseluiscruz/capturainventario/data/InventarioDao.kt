package com.joseluiscruz.capturainventario.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class InventarioResumenSku(
    val codigo: String,
    val descripcion: String,
    val totalCantidad: Int,
    val totalRestos: Int,
    val totalEventos: Int,
    val proximaCaducidadEpochDay: Long?,
    val ultimoScanTimestamp: Long
)

data class InventarioMetricasGlobales(
    val totalSkus: Int,
    val totalEventos: Int,
    val cantidadTotal: Int,
    val restosTotal: Int,
    val proximaCaducidadGlobalEpochDay: Long?
)

/**
 * Acceso al almacén local de eventos. Las lecturas operativas siempre quedan
 * acotadas a una sesión para evitar mezclar inventarios de turnos distintos.
 */
@Dao
interface InventarioDao {

    /**
     * Consolida por SKU, almacén y ubicación. Los metadatos provienen completos
     * del evento más reciente, en lugar de combinar MAX() de filas diferentes.
     */
    @Query(
        """
        SELECT
            latest.operationId AS operationId,
            totals.sessionId AS sessionId,
            totals.codigo AS codigo,
            latest.descripcion AS descripcion,
            totals.cantidadTotal AS cantidadTotal,
            totals.cantidadRestos AS cantidadRestos,
            latest.fechaEtiqueta AS fechaEtiqueta,
            totals.fechaCaducidadEpochDay AS fechaCaducidadEpochDay,
            latest.lote AS lote,
            latest.almacen AS almacen,
            latest.ubicacion AS ubicacion,
            latest.tipoTarima AS tipoTarima,
            latest.origenFecha AS origenFecha,
            latest.tipoMovimiento AS tipoMovimiento,
            totals.syncStatus AS syncStatus,
            latest.createdAt AS createdAt,
            totals.ultimoScanTimestamp AS ultimoScanTimestamp
        FROM (
            SELECT
                sessionId,
                codigo,
                IFNULL(almacen, '') AS almacenKey,
                IFNULL(ubicacion, '') AS ubicacionKey,
                IFNULL(tipoTarima, '') AS tipoTarimaKey,
                SUM(cantidadTotal) AS cantidadTotal,
                SUM(cantidadRestos) AS cantidadRestos,
                MIN(fechaCaducidadEpochDay) AS fechaCaducidadEpochDay,
                CASE
                    WHEN SUM(CASE WHEN syncStatus = 'FAILED' THEN 1 ELSE 0 END) > 0
                        THEN 'FAILED'
                    WHEN SUM(CASE WHEN syncStatus != 'SENT' THEN 1 ELSE 0 END) > 0
                        THEN 'PENDING'
                    ELSE 'SENT'
                END AS syncStatus,
                MAX(ultimoScanTimestamp) AS ultimoScanTimestamp
            FROM inventario_escaneado
            WHERE sessionId = :sessionId
            GROUP BY sessionId, codigo, IFNULL(almacen, ''), IFNULL(ubicacion, ''), IFNULL(tipoTarima, '')
        ) AS totals
        JOIN inventario_escaneado AS latest
          ON latest.operationId = (
              SELECT e2.operationId
              FROM inventario_escaneado AS e2
              WHERE e2.sessionId = totals.sessionId
                AND e2.codigo = totals.codigo
                AND IFNULL(e2.almacen, '') = totals.almacenKey
                AND IFNULL(e2.ubicacion, '') = totals.ubicacionKey
                AND IFNULL(e2.tipoTarima, '') = totals.tipoTarimaKey
              ORDER BY e2.ultimoScanTimestamp DESC, e2.createdAt DESC, e2.operationId DESC
              LIMIT 1
          )
        ORDER BY totals.ultimoScanTimestamp DESC
        """
    )
    fun observarInventarioEscaneado(sessionId: String): Flow<List<InventarioEscaneadoEntity>>

    /**
     * Devuelve el total del SKU dentro de la sesión y toma los metadatos del
     * último evento real de ese SKU.
     */
    @Query(
        """
        SELECT
            latest.operationId AS operationId,
            totals.sessionId AS sessionId,
            totals.codigo AS codigo,
            latest.descripcion AS descripcion,
            totals.cantidadTotal AS cantidadTotal,
            totals.cantidadRestos AS cantidadRestos,
            latest.fechaEtiqueta AS fechaEtiqueta,
            totals.fechaCaducidadEpochDay AS fechaCaducidadEpochDay,
            latest.lote AS lote,
            latest.almacen AS almacen,
            latest.ubicacion AS ubicacion,
            latest.tipoTarima AS tipoTarima,
            latest.origenFecha AS origenFecha,
            latest.tipoMovimiento AS tipoMovimiento,
            totals.syncStatus AS syncStatus,
            latest.createdAt AS createdAt,
            totals.ultimoScanTimestamp AS ultimoScanTimestamp
        FROM (
            SELECT
                sessionId,
                codigo,
                SUM(cantidadTotal) AS cantidadTotal,
                SUM(cantidadRestos) AS cantidadRestos,
                MIN(fechaCaducidadEpochDay) AS fechaCaducidadEpochDay,
                CASE
                    WHEN SUM(CASE WHEN syncStatus = 'FAILED' THEN 1 ELSE 0 END) > 0
                        THEN 'FAILED'
                    WHEN SUM(CASE WHEN syncStatus != 'SENT' THEN 1 ELSE 0 END) > 0
                        THEN 'PENDING'
                    ELSE 'SENT'
                END AS syncStatus,
                MAX(ultimoScanTimestamp) AS ultimoScanTimestamp
            FROM inventario_escaneado
            WHERE sessionId = :sessionId AND codigo = :codigo
            GROUP BY sessionId, codigo
        ) AS totals
        JOIN inventario_escaneado AS latest
          ON latest.operationId = (
              SELECT e2.operationId
              FROM inventario_escaneado AS e2
              WHERE e2.sessionId = totals.sessionId
                AND e2.codigo = totals.codigo
              ORDER BY e2.ultimoScanTimestamp DESC, e2.createdAt DESC, e2.operationId DESC
              LIMIT 1
          )
        LIMIT 1
        """
    )
    fun observarItemPorCodigo(
        sessionId: String,
        codigo: String
    ): Flow<InventarioEscaneadoEntity?>

    @Query(
        """
        SELECT
            latest.operationId AS operationId,
            totals.sessionId AS sessionId,
            totals.codigo AS codigo,
            latest.descripcion AS descripcion,
            totals.cantidadTotal AS cantidadTotal,
            totals.cantidadRestos AS cantidadRestos,
            latest.fechaEtiqueta AS fechaEtiqueta,
            totals.fechaCaducidadEpochDay AS fechaCaducidadEpochDay,
            latest.lote AS lote,
            latest.almacen AS almacen,
            latest.ubicacion AS ubicacion,
            latest.tipoTarima AS tipoTarima,
            latest.origenFecha AS origenFecha,
            latest.tipoMovimiento AS tipoMovimiento,
            totals.syncStatus AS syncStatus,
            latest.createdAt AS createdAt,
            totals.ultimoScanTimestamp AS ultimoScanTimestamp
        FROM (
            SELECT
                sessionId,
                codigo,
                IFNULL(almacen, '') AS almacenKey,
                IFNULL(ubicacion, '') AS ubicacionKey,
                IFNULL(tipoTarima, '') AS tipoTarimaKey,
                SUM(cantidadTotal) AS cantidadTotal,
                SUM(cantidadRestos) AS cantidadRestos,
                MIN(fechaCaducidadEpochDay) AS fechaCaducidadEpochDay,
                CASE
                    WHEN SUM(CASE WHEN syncStatus = 'FAILED' THEN 1 ELSE 0 END) > 0
                        THEN 'FAILED'
                    WHEN SUM(CASE WHEN syncStatus != 'SENT' THEN 1 ELSE 0 END) > 0
                        THEN 'PENDING'
                    ELSE 'SENT'
                END AS syncStatus,
                MAX(ultimoScanTimestamp) AS ultimoScanTimestamp
            FROM inventario_escaneado
            WHERE sessionId = :sessionId
            GROUP BY sessionId, codigo, IFNULL(almacen, ''), IFNULL(ubicacion, ''), IFNULL(tipoTarima, '')
        ) AS totals
        JOIN inventario_escaneado AS latest
          ON latest.operationId = (
              SELECT e2.operationId
              FROM inventario_escaneado AS e2
              WHERE e2.sessionId = totals.sessionId
                AND e2.codigo = totals.codigo
                AND IFNULL(e2.almacen, '') = totals.almacenKey
                AND IFNULL(e2.ubicacion, '') = totals.ubicacionKey
                AND IFNULL(e2.tipoTarima, '') = totals.tipoTarimaKey
              ORDER BY e2.ultimoScanTimestamp DESC, e2.createdAt DESC, e2.operationId DESC
              LIMIT 1
          )
        ORDER BY totals.ultimoScanTimestamp DESC
        """
    )
    suspend fun obtenerTodosLosEscaneados(sessionId: String): List<InventarioEscaneadoEntity>

    @Query(
        """
        SELECT
            latest.operationId AS operationId,
            totals.sessionId AS sessionId,
            totals.codigo AS codigo,
            latest.descripcion AS descripcion,
            totals.cantidadTotal AS cantidadTotal,
            totals.cantidadRestos AS cantidadRestos,
            latest.fechaEtiqueta AS fechaEtiqueta,
            totals.fechaCaducidadEpochDay AS fechaCaducidadEpochDay,
            latest.lote AS lote,
            latest.almacen AS almacen,
            latest.ubicacion AS ubicacion,
            latest.tipoTarima AS tipoTarima,
            latest.origenFecha AS origenFecha,
            latest.tipoMovimiento AS tipoMovimiento,
            totals.syncStatus AS syncStatus,
            latest.createdAt AS createdAt,
            totals.ultimoScanTimestamp AS ultimoScanTimestamp
        FROM (
            SELECT
                sessionId,
                codigo,
                SUM(cantidadTotal) AS cantidadTotal,
                SUM(cantidadRestos) AS cantidadRestos,
                MIN(fechaCaducidadEpochDay) AS fechaCaducidadEpochDay,
                CASE
                    WHEN SUM(CASE WHEN syncStatus = 'FAILED' THEN 1 ELSE 0 END) > 0
                        THEN 'FAILED'
                    WHEN SUM(CASE WHEN syncStatus != 'SENT' THEN 1 ELSE 0 END) > 0
                        THEN 'PENDING'
                    ELSE 'SENT'
                END AS syncStatus,
                MAX(ultimoScanTimestamp) AS ultimoScanTimestamp
            FROM inventario_escaneado
            WHERE sessionId = :sessionId AND codigo = :codigo
            GROUP BY sessionId, codigo
        ) AS totals
        JOIN inventario_escaneado AS latest
          ON latest.operationId = (
              SELECT e2.operationId
              FROM inventario_escaneado AS e2
              WHERE e2.sessionId = totals.sessionId
                AND e2.codigo = totals.codigo
              ORDER BY e2.ultimoScanTimestamp DESC, e2.createdAt DESC, e2.operationId DESC
              LIMIT 1
          )
        LIMIT 1
        """
    )
    suspend fun obtenerItemPorCodigo(
        sessionId: String,
        codigo: String
    ): InventarioEscaneadoEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarOActualizar(item: InventarioEscaneadoEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarLista(items: List<InventarioEscaneadoEntity>)

    @Query(
        """
        SELECT *
        FROM inventario_escaneado
        WHERE sessionId = :sessionId AND codigo = :codigo
        ORDER BY
            CASE WHEN fechaCaducidadEpochDay IS NULL THEN 1 ELSE 0 END,
            fechaCaducidadEpochDay ASC,
            ultimoScanTimestamp DESC
        """
    )
    fun observarDetallePorCodigo(
        sessionId: String,
        codigo: String
    ): Flow<List<InventarioEscaneadoEntity>>

    @Query(
        """
        SELECT
            codigo,
            (
                SELECT e2.descripcion
                FROM inventario_escaneado AS e2
                WHERE e2.sessionId = :sessionId
                  AND e2.codigo = inventario_escaneado.codigo
                ORDER BY e2.ultimoScanTimestamp DESC, e2.createdAt DESC
                LIMIT 1
            ) AS descripcion,
            SUM(cantidadTotal) AS totalCantidad,
            SUM(cantidadRestos) AS totalRestos,
            COUNT(*) AS totalEventos,
            MIN(fechaCaducidadEpochDay) AS proximaCaducidadEpochDay,
            MAX(ultimoScanTimestamp) AS ultimoScanTimestamp
        FROM inventario_escaneado
        WHERE sessionId = :sessionId
        GROUP BY codigo
        ORDER BY
            CASE WHEN MIN(fechaCaducidadEpochDay) IS NULL THEN 1 ELSE 0 END,
            MIN(fechaCaducidadEpochDay) ASC,
            MAX(ultimoScanTimestamp) DESC
        """
    )
    fun observarResumenPorSku(sessionId: String): Flow<List<InventarioResumenSku>>

    @Query(
        """
        SELECT MIN(fechaCaducidadEpochDay)
        FROM inventario_escaneado
        WHERE sessionId = :sessionId AND codigo = :codigo
        """
    )
    fun observarProximaCaducidadPorSku(
        sessionId: String,
        codigo: String
    ): Flow<Long?>

    @Query(
        """
        SELECT
            COUNT(DISTINCT codigo) AS totalSkus,
            COUNT(*) AS totalEventos,
            COALESCE(SUM(cantidadTotal), 0) AS cantidadTotal,
            COALESCE(SUM(cantidadRestos), 0) AS restosTotal,
            MIN(fechaCaducidadEpochDay) AS proximaCaducidadGlobalEpochDay
        FROM inventario_escaneado
        WHERE sessionId = :sessionId
        """
    )
    fun observarMetricasGlobales(sessionId: String): Flow<InventarioMetricasGlobales>

    /**
     * Permite adoptar la toma más reciente al instalar una versión que empieza
     * a persistir explícitamente el identificador de sesión.
     */
    @Query(
        """
        SELECT sessionId
        FROM inventario_escaneado
        ORDER BY ultimoScanTimestamp DESC, createdAt DESC
        LIMIT 1
        """
    )
    suspend fun obtenerSesionMasReciente(): String?

    /**
     * Cierra operativamente la sesión sin destruir el historial de otros turnos.
     */
    @Query("DELETE FROM inventario_escaneado WHERE sessionId = :sessionId")
    suspend fun limpiarInventario(sessionId: String)

    /**
     * Elimina los eventos de todas las sesiones guardadas en el dispositivo.
     * Se usa únicamente cuando el operador confirma que iniciará una toma desde cero.
     */
    @Query("DELETE FROM inventario_escaneado")
    suspend fun limpiarTodosLosInventarios()

    /** Total de eventos guardados en el dispositivo (todas las sesiones), para confirmar "Nueva toma". */
    @Query("SELECT COUNT(*) FROM inventario_escaneado")
    suspend fun contarTodosLosEventos(): Int

    /** Todos los eventos de todas las sesiones: respaldo exacto de lo que borra "Nueva toma". */
    @Query("SELECT * FROM inventario_escaneado ORDER BY createdAt ASC")
    suspend fun obtenerEventosDeTodasLasSesiones(): List<InventarioEscaneadoEntity>

    /** Eventos que aún no llegan a la nube (PENDING/FAILED), todas las sesiones. */
    @Query("SELECT COUNT(*) FROM inventario_escaneado WHERE syncStatus != 'SENT'")
    suspend fun contarEventosSinSincronizar(): Int

    @Query(
        "DELETE FROM inventario_escaneado WHERE sessionId = :sessionId AND codigo = :codigo"
    )
    suspend fun eliminarItemPorCodigo(sessionId: String, codigo: String)

    /**
     * Elimina exactamente el grupo consolidado que se muestra en el resumen.
     * Room sigue siendo la fuente de verdad; el CSV se reconstruye después.
     */
    @Query(
        """
        DELETE FROM inventario_escaneado
        WHERE sessionId = :sessionId
          AND codigo = :codigo
          AND IFNULL(ubicacion, '') = :ubicacion
          AND IFNULL(almacen, '') = :almacen
          AND IFNULL(tipoTarima, '') = :tipoTarima
        """
    )
    suspend fun eliminarGrupoConsolidado(
        sessionId: String,
        codigo: String,
        ubicacion: String,
        almacen: String,
        tipoTarima: String
    ): Int

    @Query(
        "SELECT COUNT(DISTINCT codigo) FROM inventario_escaneado WHERE sessionId = :sessionId"
    )
    fun contarItemsEscaneados(sessionId: String): Flow<Int>

    @Query(
        "SELECT COUNT(DISTINCT codigo) FROM inventario_escaneado WHERE sessionId = :sessionId"
    )
    suspend fun contarItemsEscaneadosSincrono(sessionId: String): Int

    /**
     * Registros de la cola de sincronización que aún no llegan a la nube
     * (PENDING + FAILED), de todas las sesiones. Alimenta el KPI "Pend. sync" del home.
     */
    @Query(
        "SELECT COUNT(*) FROM inventario_escaneado WHERE syncStatus IN ('PENDING', 'FAILED')"
    )
    fun contarPendientesDeSync(): Flow<Int>

    /** Subconjunto de [contarPendientesDeSync] cuyo último intento de subida falló. */
    @Query("SELECT COUNT(*) FROM inventario_escaneado WHERE syncStatus = 'FAILED'")
    fun contarFallidosDeSync(): Flow<Int>

    /**
     * SKUs distintos de la sesión con riesgo TPM: caducidad antes de [limiteEpochDay]
     * (= hoy + umbral de [com.joseluiscruz.capturainventario.util.TpmRiskPolicy]).
     * Misma regla que el resumen de inventario (`diasRestantes < umbral`). Los registros sin
     * fecha de caducidad no cuentan como riesgo.
     */
    @Query(
        """
        SELECT COUNT(DISTINCT codigo) FROM inventario_escaneado
        WHERE sessionId = :sessionId
          AND fechaCaducidadEpochDay IS NOT NULL
          AND fechaCaducidadEpochDay < :limiteEpochDay
        """
    )
    fun contarSkusEnRiesgoTpm(sessionId: String, limiteEpochDay: Long): Flow<Int>
}
