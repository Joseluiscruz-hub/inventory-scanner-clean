package com.joseluiscruz.capturainventario.data

import android.database.Cursor
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Offline queue accessors without adding a second Room @Dao to AppDatabase.
 * Uses RoomDatabase.query against the shared inventario_escaneado table.
 */
@Singleton
class OfflineSyncStore @Inject constructor(
    private val db: AppDatabase
) {
    suspend fun obtenerPendientesDeSync(limit: Int): List<InventarioEscaneadoEntity> =
        withContext(Dispatchers.IO) {
            val query = SimpleSQLiteQuery(
                "SELECT * FROM inventario_escaneado WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt ASC LIMIT ?",
                arrayOf(limit)
            )
            db.query(query).use { cursor -> cursor.toEntities() }
        }

    suspend fun actualizarSyncStatus(operationId: String, status: String) =
        withContext(Dispatchers.IO) {
            db.openHelper.writableDatabase.execSQL(
                "UPDATE inventario_escaneado SET syncStatus = ? WHERE operationId = ?",
                arrayOf(status, operationId)
            )
            // execSQL directo no agenda la revisión de invalidación de Room: sin esto, los Flows
            // observados (p. ej. KPI "Pend. sync" del home) no verían el cambio de estado.
            db.invalidationTracker.refreshAsync()
        }

    private fun Cursor.toEntities(): List<InventarioEscaneadoEntity> {
        val out = mutableListOf<InventarioEscaneadoEntity>()
        val idxOp = getColumnIndexOrThrow("operationId")
        val idxSession = getColumnIndexOrThrow("sessionId")
        val idxCodigo = getColumnIndexOrThrow("codigo")
        val idxDesc = getColumnIndexOrThrow("descripcion")
        val idxCant = getColumnIndexOrThrow("cantidadTotal")
        val idxRestos = getColumnIndex("cantidadRestos")
        val idxFechaEtiqueta = getColumnIndex("fechaEtiqueta")
        val idxFechaCad = getColumnIndex("fechaCaducidadEpochDay")
        val idxLote = getColumnIndex("lote")
        val idxAlmacen = getColumnIndex("almacen")
        val idxUbic = getColumnIndex("ubicacion")
        val idxTipo = getColumnIndex("tipoTarima")
        val idxOrigen = getColumnIndex("origenFecha")
        val idxTipoMov = getColumnIndex("tipoMovimiento")
        val idxSync = getColumnIndexOrThrow("syncStatus")
        val idxCreated = getColumnIndex("createdAt")
        val idxUltimo = getColumnIndexOrThrow("ultimoScanTimestamp")
        while (moveToNext()) {
            out.add(
                InventarioEscaneadoEntity(
                    operationId = getString(idxOp),
                    sessionId = getString(idxSession),
                    codigo = getString(idxCodigo),
                    descripcion = getString(idxDesc),
                    cantidadTotal = getInt(idxCant),
                    cantidadRestos = if (idxRestos >= 0 && !isNull(idxRestos)) getInt(idxRestos) else 0,
                    fechaEtiqueta = if (idxFechaEtiqueta >= 0 && !isNull(idxFechaEtiqueta)) getString(idxFechaEtiqueta) else null,
                    fechaCaducidadEpochDay = if (idxFechaCad >= 0 && !isNull(idxFechaCad)) getLong(idxFechaCad) else null,
                    lote = if (idxLote >= 0 && !isNull(idxLote)) getString(idxLote) else null,
                    almacen = if (idxAlmacen >= 0 && !isNull(idxAlmacen)) getString(idxAlmacen) else null,
                    ubicacion = if (idxUbic >= 0 && !isNull(idxUbic)) getString(idxUbic) else null,
                    tipoTarima = if (idxTipo >= 0 && !isNull(idxTipo)) getString(idxTipo) else null,
                    origenFecha = if (idxOrigen >= 0 && !isNull(idxOrigen)) getString(idxOrigen) else FechaOrigen.UNKNOWN.name,
                    tipoMovimiento = if (idxTipoMov >= 0 && !isNull(idxTipoMov)) getString(idxTipoMov) else TipoMovimiento.SCAN.name,
                    syncStatus = getString(idxSync),
                    createdAt = if (idxCreated >= 0 && !isNull(idxCreated)) getLong(idxCreated) else 0L,
                    ultimoScanTimestamp = getLong(idxUltimo)
                )
            )
        }
        return out
    }
}
