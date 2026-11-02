package com.joseluiscruz.capturainventario.util

import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager.ItemReferencia

/**
 * Utilidad estática para parsear archivos CSV provenientes de SAP/Excel.
 *
 * Extraída del InventarioSharedViewModel para ser:
 * 1. Reutilizable sin instanciar un ViewModel
 * 2. Testeable sin dependencias de Android/Coroutines
 */
object CsvParser {
    private val referenceSkuPattern = Regex("^[A-Z0-9][A-Z0-9._/-]{0,63}$")
    private val spreadsheetBinaryMarkers = listOf(
        "PK\u0003\u0004",
        "[Content_Types]",
        "XL/",
        "XL_",
        "PIVOTCACHE",
        ".XML",
        ".EMF"
    )

    /**
     * Tokenizador CSV defensivo. Soporta:
     * - Campos entre comillas (con comas/punto-y-coma internas)
     * - Trim automático de espacios
     * - Delimitador configurable (coma por defecto)
     */
    fun parseCsvLineSafe(line: String, delimiter: Char = ','): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == delimiter && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    /**
     * Parsea una línea de CSV de referencia SAP y la convierte en [ItemReferencia].
     *
     * Formato esperado (flexible, soporta 2-6 columnas):
     *   SKU, Descripcion, Cantidad, CajasXPallet, Armado, TarimaPlasticaSku
     *
     * Retorna null si la línea no tiene al menos 2 columnas o si el SKU está vacío.
     */
    fun parseReferenciaCsvLine(line: String): ItemReferencia? {
        // Limpiar BOM de Excel si existe
        val lineaLimpia = line.removePrefix("\uFEFF")
        if (looksLikeSpreadsheetBinary(lineaLimpia)) return null

        val delimiter = if (lineaLimpia.contains(';')) ';' else ','
        val columns = parseCsvLineSafe(lineaLimpia, delimiter)
        if (columns.size < 2) return null

        val sku = columns.getOrNull(0)?.trim()?.uppercase().orEmpty()
        if (!isValidReferenceSku(sku)) return null

        val descripcion = columns.getOrNull(1)?.trim().orEmpty()
        val cantidad = columns.getOrNull(2)?.trim()?.toIntOrNull()
            ?: columns.getOrNull(3)?.trim()?.toIntOrNull()
            ?: 0
        val cajasXPallet = columns.getOrNull(3)?.trim()?.toIntOrNull()
            ?: columns.getOrNull(4)?.trim()?.toIntOrNull()
            ?: 0
        val armado = columns.getOrNull(4)?.trim().orEmpty()
            .ifBlank { cajasXPallet.takeIf { it > 0 }?.toString().orEmpty() }
        val tarimaPlasticaSku = columns.getOrNull(5)?.trim().orEmpty()
            .ifBlank { ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR }

        return ItemReferencia(
            codigo = sku,
            descripcion = descripcion,
            cantidad = cantidad,
            cajasXPallet = cajasXPallet,
            tarimaPlasticaSku = tarimaPlasticaSku,
            armado = armado
        )
    }

    fun isValidReferenceItem(item: ItemReferencia): Boolean =
        isValidReferenceSku(item.codigo) &&
            !looksLikeSpreadsheetBinary(item.descripcion) &&
            !looksLikeSpreadsheetBinary(item.armado) &&
            !looksLikeSpreadsheetBinary(item.tarimaPlasticaSku)

    fun isValidReferenceSku(sku: String): Boolean {
        val normalized = sku.trim().uppercase()
        return normalized.isNotBlank() &&
            normalized.length <= 64 &&
            normalized.matches(referenceSkuPattern) &&
            !normalized.any { it.isISOControl() } &&
            !looksLikeSpreadsheetBinary(normalized)
    }

    fun looksLikeSpreadsheetBinary(value: String): Boolean {
        val uppercase = value.uppercase()
        return value.any { it.isISOControl() && it != '\t' } ||
            spreadsheetBinaryMarkers.any { marker -> uppercase.contains(marker) }
    }
}
