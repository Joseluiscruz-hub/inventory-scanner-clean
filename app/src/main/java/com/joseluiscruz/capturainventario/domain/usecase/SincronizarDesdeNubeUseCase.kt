package com.joseluiscruz.capturainventario.domain.usecase

import com.joseluiscruz.capturainventario.data.FirebaseRepository
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Pulls cloud inventory into the local CSV used as reference.
 *
 * M1 (best-effort): does **not** blindly overwrite a non-empty local CSV.
 * Instead merges by SKU+almacen+tipoTarima (cloud wins on conflict), keeps
 * local-only rows, and writes a `.bak` of the previous file for audit.
 */
class SincronizarDesdeNubeUseCase @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val filesDir: File
) {
    suspend operator fun invoke(): Result<String> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            firebaseRepository.descargarInventario { lista, mensaje ->
                if (lista != null) {
                    try {
                        val file = File(filesDir, Constants.INVENTORY_FILE_NAME)
                        val previousLines = if (file.exists() && file.length() > 0L) {
                            file.readLines()
                        } else {
                            emptyList()
                        }

                        if (previousLines.isNotEmpty()) {
                            File(filesDir, Constants.INVENTORY_FILE_NAME + ".bak")
                                .writeText(previousLines.joinToString("\n"))
                        }

                        val localByKey = parseLocalRows(previousLines)
                        val cloudByKey = lista.associateBy { mergeKey(it) }.toMutableMap()

                        // Cloud wins on key collision; keep local-only rows.
                        for ((key, localItem) in localByKey) {
                            if (!cloudByKey.containsKey(key)) {
                                cloudByKey[key] = localItem
                            }
                        }

                        val lines = mutableListOf<String>()
                        lines.add(Constants.CSV_HEADER)
                        for (item in cloudByKey.values.sortedBy { it.sku }) {
                            lines.add(toCsvRow(item))
                        }
                        file.writeText(lines.joinToString("\n"))

                        val keptLocalOnly = localByKey.keys.count { it !in lista.map { c -> mergeKey(c) }.toSet() }
                        val summary =
                            "$mensaje | merge: cloud=${lista.size}, localOnlyKept=$keptLocalOnly, total=${cloudByKey.size}"
                        continuation.resume(Result.success(summary))
                    } catch (e: Exception) {
                        continuation.resume(Result.failure(e))
                    }
                } else {
                    continuation.resume(Result.failure(Exception(mensaje)))
                }
            }
        }
    }

    private fun mergeKey(item: MaterialItem): String {
        return listOf(
            item.sku.trim(),
            (item.almacen ?: "").trim(),
            (item.tipoTarima ?: "").trim()
        ).joinToString("|")
    }

    private fun toCsvRow(item: MaterialItem): String {
        return listOf(
            item.sku,
            item.descripcion,
            item.cxPal,
            item.fpc,
            item.con,
            item.centro,
            item.linea,
            item.op,
            item.fProd,
            item.diasV,
            item.ubicacion ?: "",
            item.totalPallets ?: "0",
            item.tipoTarima ?: "",
            item.almacen ?: "",
            item.restos ?: "0"
        ).joinToString(",")
    }

    private fun parseLocalRows(lines: List<String>): Map<String, MaterialItem> {
        if (lines.size <= 1) return emptyMap()
        val result = linkedMapOf<String, MaterialItem>()
        for (line in lines.drop(1)) {
            if (line.isBlank()) continue
            val cols = line.split(",")
            if (cols.isEmpty() || cols[0].isBlank()) continue
            val item = MaterialItem(
                sku = cols.getOrElse(0) { "" },
                descripcion = cols.getOrElse(1) { "" },
                cxPal = cols.getOrElse(2) { "" },
                fpc = cols.getOrElse(3) { "" },
                con = cols.getOrElse(4) { "" },
                centro = cols.getOrElse(5) { "" },
                linea = cols.getOrElse(6) { "" },
                op = cols.getOrElse(7) { "" },
                fProd = cols.getOrElse(8) { "" },
                diasV = cols.getOrElse(9) { "" },
                ubicacion = cols.getOrElse(10) { "" },
                totalPallets = cols.getOrElse(11) { "0" },
                tipoTarima = cols.getOrElse(12) { "" },
                almacen = cols.getOrElse(13) { "" },
                restos = cols.getOrElse(14) { "0" }
            )
            result[mergeKey(item)] = item
        }
        return result
    }
}
