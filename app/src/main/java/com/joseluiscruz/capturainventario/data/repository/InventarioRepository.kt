package com.joseluiscruz.capturainventario.data.repository

import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.data.InventarioMetricasGlobales
import com.joseluiscruz.capturainventario.data.InventarioResumenSku
import com.joseluiscruz.capturainventario.data.TipoMovimiento
import com.joseluiscruz.capturainventario.data.FechaOrigen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.UUID

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio único para operaciones de inventario.
 * AHORA BASADO EN EVENTOS (Append-Only)
 */
@Singleton
class InventarioRepository @Inject constructor(
    private val dao: InventarioDao,
    private val sessionStore: InventorySessionStore
) {

    data class ScanMetadata(
        val fechaEtiqueta: String? = null,
        val fechaCaducidadEpochDay: Long? = null,
        val lote: String? = null,
        val almacen: String? = null,
        val ubicacion: String? = null,
        val tipoTarima: String? = null,
        val origenFecha: FechaOrigen = FechaOrigen.UNKNOWN
    )

    data class AjusteInput(
        val codigo: String,
        val descripcion: String,
        val cantidadDelta: Int,
        val restosDelta: Int = 0,
        val metadata: ScanMetadata = ScanMetadata()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarEscaneados(): Flow<List<InventarioEscaneadoEntity>> =
        sessionStore.sessionId.flatMapLatest(dao::observarInventarioEscaneado)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarItemPorCodigo(codigo: String): Flow<InventarioEscaneadoEntity?> =
        sessionStore.sessionId.flatMapLatest { sessionId ->
            dao.observarItemPorCodigo(sessionId, codigo.uppercase().trim())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun contarItemsEscaneados(): Flow<Int> =
        sessionStore.sessionId.flatMapLatest(dao::contarItemsEscaneados)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarResumenPorSku(): Flow<List<InventarioResumenSku>> =
        sessionStore.sessionId.flatMapLatest(dao::observarResumenPorSku)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarDetallePorCodigo(codigo: String): Flow<List<InventarioEscaneadoEntity>> =
        sessionStore.sessionId.flatMapLatest { sessionId ->
            dao.observarDetallePorCodigo(sessionId, codigo.uppercase().trim())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarProximaCaducidadPorSku(codigo: String): Flow<Long?> =
        sessionStore.sessionId.flatMapLatest { sessionId ->
            dao.observarProximaCaducidadPorSku(sessionId, codigo.uppercase().trim())
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observarMetricasGlobales(): Flow<InventarioMetricasGlobales> =
        sessionStore.sessionId.flatMapLatest(dao::observarMetricasGlobales)

    /**
     * Registra un nuevo escaneo como un EVENTO ÚNICO.
     * Ya no lee la base de datos para sumar; solo inserta la acción.
     */
    suspend fun registrarScan(
        codigo: String,
        descripcion: String,
        cantidad: Int = 1,
        restos: Int = 0,
        metadata: ScanMetadata = ScanMetadata(),
        sessionId: String? = null
    ) {
        require(codigo.isNotBlank()) { "El SKU no puede estar vacío" }
        require(cantidad >= 0 && restos >= 0) { "Las cantidades no pueden ser negativas" }
        require(cantidad > 0 || restos > 0) { "El escaneo debe contener pallets o restos" }

        val targetSessionId = sessionId ?: sessionStore.awaitSessionId()
        val now = System.currentTimeMillis()
        val codigoNormalizado = codigo.uppercase().trim()
        val item = InventarioEscaneadoEntity(
            operationId = UUID.randomUUID().toString(),
            sessionId = targetSessionId,
            codigo = codigoNormalizado,
            descripcion = descripcion,
            cantidadTotal = cantidad, // Se inserta solo el incremento (ej. 1, 5, o 300)
            cantidadRestos = restos,
            fechaEtiqueta = metadata.fechaEtiqueta,
            fechaCaducidadEpochDay = metadata.fechaCaducidadEpochDay,
            lote = metadata.lote,
            almacen = metadata.almacen,
            ubicacion = metadata.ubicacion,
            tipoTarima = metadata.tipoTarima,
            origenFecha = metadata.origenFecha.name,
            tipoMovimiento = TipoMovimiento.SCAN.name,
            createdAt = now,
            ultimoScanTimestamp = now
        )
        
        // Insertamos el nuevo evento
        dao.insertarOActualizar(item)
    }

    /**
     * Registra un conjunto de ajustes en una sola transacción Room.
     */
    suspend fun registrarAjustes(
        ajustes: List<AjusteInput>,
        sessionId: String? = null
    ) {
        require(ajustes.isNotEmpty()) { "No hay ajustes para registrar" }
        val targetSessionId = sessionId ?: sessionStore.awaitSessionId()
        val now = System.currentTimeMillis()
        val entities = ajustes.mapIndexed { index, ajuste ->
            require(ajuste.codigo.isNotBlank()) { "El SKU no puede estar vacío" }
            InventarioEscaneadoEntity(
                operationId = UUID.randomUUID().toString(),
                sessionId = targetSessionId,
                codigo = ajuste.codigo.uppercase().trim(),
                descripcion = ajuste.descripcion,
                cantidadTotal = ajuste.cantidadDelta,
                cantidadRestos = ajuste.restosDelta,
                fechaEtiqueta = ajuste.metadata.fechaEtiqueta,
                fechaCaducidadEpochDay = ajuste.metadata.fechaCaducidadEpochDay,
                lote = ajuste.metadata.lote,
                almacen = ajuste.metadata.almacen,
                ubicacion = ajuste.metadata.ubicacion,
                tipoTarima = ajuste.metadata.tipoTarima,
                origenFecha = ajuste.metadata.origenFecha.name,
                tipoMovimiento = TipoMovimiento.ADJUSTMENT.name,
                createdAt = now + index,
                ultimoScanTimestamp = now + index
            )
        }
        dao.insertarLista(entities)
    }

    suspend fun registrarAjuste(
        codigo: String,
        descripcion: String,
        cantidadDelta: Int,
        restosDelta: Int = 0,
        metadata: ScanMetadata = ScanMetadata(),
        sessionId: String? = null
    ) {
        require(codigo.isNotBlank()) { "El SKU no puede estar vacío" }

        val targetSessionId = sessionId ?: sessionStore.awaitSessionId()
        val now = System.currentTimeMillis()
        val codigoNormalizado = codigo.uppercase().trim()
        val item = InventarioEscaneadoEntity(
            operationId = UUID.randomUUID().toString(),
            sessionId = targetSessionId,
            codigo = codigoNormalizado,
            descripcion = descripcion,
            cantidadTotal = cantidadDelta,
            cantidadRestos = restosDelta,
            fechaEtiqueta = metadata.fechaEtiqueta,
            fechaCaducidadEpochDay = metadata.fechaCaducidadEpochDay,
            lote = metadata.lote,
            almacen = metadata.almacen,
            ubicacion = metadata.ubicacion,
            tipoTarima = metadata.tipoTarima,
            origenFecha = metadata.origenFecha.name,
            tipoMovimiento = TipoMovimiento.ADJUSTMENT.name,
            createdAt = now,
            ultimoScanTimestamp = now
        )
        dao.insertarOActualizar(item)
    }

    suspend fun registrarLote(items: List<InventarioEscaneadoEntity>) {
        dao.insertarLista(items)
    }

    suspend fun limpiarInventario() {
        dao.limpiarInventario(sessionStore.awaitSessionId())
    }

    /**
     * Inicia una toma realmente nueva: borra las capturas confirmadas y cambia
     * el identificador persistente para que todos los Flows observen la sesión
     * recién creada sin reiniciar la aplicación.
     */
    /** Resumen de lo que borraría [iniciarNuevaToma]: total de eventos y cuántos no se han sincronizado. */
    data class ResumenNuevaToma(val totalRegistros: Int, val sinSincronizar: Int)

    /** Registros que borraría [iniciarNuevaToma] (todas las sesiones), para exportarlos antes. */
    suspend fun obtenerEventosParaNuevaToma(): List<InventarioEscaneadoEntity> =
        dao.obtenerEventosDeTodasLasSesiones()

    suspend fun resumenNuevaToma(): ResumenNuevaToma =
        ResumenNuevaToma(
            totalRegistros = dao.contarTodosLosEventos(),
            sinSincronizar = dao.contarEventosSinSincronizar()
        )

    suspend fun iniciarNuevaToma(): String {
        sessionStore.awaitSessionId()
        dao.limpiarTodosLosInventarios()
        return sessionStore.startNewSession()
    }

    suspend fun eliminarItem(codigo: String) {
        dao.eliminarItemPorCodigo(sessionStore.awaitSessionId(), codigo.uppercase().trim())
    }

    suspend fun eliminarGrupoConsolidado(
        codigo: String,
        ubicacion: String?,
        almacen: String?,
        tipoTarima: String?
    ): Int = dao.eliminarGrupoConsolidado(
        sessionId = sessionStore.awaitSessionId(),
        codigo = codigo.uppercase().trim(),
        // Las dimensiones forman la clave exacta del grupo mostrado por Room.
        // No se recortan: así también se pueden corregir datos legacy con espacios.
        ubicacion = ubicacion.orEmpty(),
        almacen = almacen.orEmpty(),
        tipoTarima = tipoTarima.orEmpty()
    )

    suspend fun obtenerTodosLosEscaneados(): List<InventarioEscaneadoEntity> =
        dao.obtenerTodosLosEscaneados(sessionStore.awaitSessionId())

    suspend fun obtenerItemPorCodigo(codigo: String): InventarioEscaneadoEntity? =
        dao.obtenerItemPorCodigo(sessionStore.awaitSessionId(), codigo.uppercase().trim())

    suspend fun contarItemsEscaneadosSincrono(): Int =
        dao.contarItemsEscaneadosSincrono(sessionStore.awaitSessionId())
}
