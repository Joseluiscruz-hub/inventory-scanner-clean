package com.joseluiscruz.capturainventario.util

import com.joseluiscruz.capturainventario.model.InventarioItem
import com.joseluiscruz.capturainventario.model.MaterialItem

/**
 * Utilidades para convertir entre diferentes tipos de items de inventario.
 */
object InventarioConverter {

    /**
     * Convierte una lista de MaterialItem a un mapa de InventarioItem agrupado por SKU.
     * Acumula las cantidades si hay múltiples entries del mismo SKU.
     */
    fun convertirMaterialItemsAInventario(materiales: List<MaterialItem>): Map<String, InventarioItem> {
        val inventario = mutableMapOf<String, InventarioItem>()

        materiales.forEach { material ->
            val inventarioItem = InventarioItem(material)
            val existing = inventario[inventarioItem.sku]

            if (existing != null) {
                // Acumular cantidades si ya existe el SKU
                inventario[inventarioItem.sku] = existing.copy(
                    totalPallets = existing.totalPallets + inventarioItem.totalPallets,
                    // Mantener la información más completa
                    descripcion = if (existing.descripcion.isBlank()) inventarioItem.descripcion else existing.descripcion,
                    tipoTarima = if (existing.tipoTarima.isBlank()) inventarioItem.tipoTarima else existing.tipoTarima,
                    ubicacion = if (existing.ubicacion.isBlank()) inventarioItem.ubicacion else existing.ubicacion,
                    centro = if (existing.centro.isBlank()) inventarioItem.centro else existing.centro,
                    linea = if (existing.linea.isBlank()) inventarioItem.linea else existing.linea
                )
            } else {
                inventario[inventarioItem.sku] = inventarioItem
            }
        }

        return inventario
    }

    /**
     * Convierte datos de CSV del sistema a InventarioItem.
     * Formato esperado: SKU, Descripción, LibrUtiliz, TipoTarima, Ubicación, Centro, Línea
     */
    fun convertirCSVSistemaAInventario(csvData: List<List<String>>): Map<String, InventarioItem> {
        val inventario = mutableMapOf<String, InventarioItem>()

        csvData.forEach { fila ->
            if (fila.size >= 3) {
                val sku = fila[0].trim()
                val descripcion = fila.getOrNull(1)?.trim() ?: ""
                val librUtiliz = fila.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
                val tipoTarima = fila.getOrNull(3)?.trim() ?: ""
                val ubicacion = fila.getOrNull(4)?.trim() ?: ""
                val centro = fila.getOrNull(5)?.trim() ?: ""
                val linea = fila.getOrNull(6)?.trim() ?: ""

                if (sku.isNotBlank()) {
                    val inventarioItem = InventarioItem(
                        sku = sku,
                        descripcion = descripcion,
                        totalPallets = librUtiliz,
                        pallets = librUtiliz,
                        tipoTarima = tipoTarima,
                        ubicacion = ubicacion,
                        centro = centro,
                        linea = linea
                    )

                    val existing = inventario[sku]
                    if (existing != null) {
                        // Acumular si ya existe
                        inventario[sku] = existing.copy(
                            totalPallets = existing.totalPallets + librUtiliz,
                            pallets = existing.pallets + librUtiliz
                        )
                    } else {
                        inventario[sku] = inventarioItem
                    }
                }
            }
        }

        return inventario
    }

    /**
     * Convierte datos de CSV de inventario escaneado a InventarioItem.
     * Formato esperado: SKU, Descripción, Pallets, Ubicación, etc.
     */
    fun convertirCSVEscaneadoAInventario(csvData: List<List<String>>): Map<String, InventarioItem> {
        val inventario = mutableMapOf<String, InventarioItem>()

        csvData.forEach { fila ->
            if (fila.size >= 3) {
                val sku = fila[0].trim()
                val descripcion = fila.getOrNull(1)?.trim() ?: ""
                val pallets = fila.getOrNull(2)?.trim()?.toIntOrNull() ?: 0
                val ubicacion = fila.getOrNull(3)?.trim() ?: ""

                if (sku.isNotBlank()) {
                    val inventarioItem = InventarioItem(
                        sku = sku,
                        descripcion = descripcion,
                        totalPallets = pallets,
                        pallets = pallets,
                        ubicacion = ubicacion
                    )

                    val existing = inventario[sku]
                    if (existing != null) {
                        // Acumular si ya existe
                        inventario[sku] = existing.copy(
                            totalPallets = existing.totalPallets + pallets,
                            pallets = existing.pallets + pallets
                        )
                    } else {
                        inventario[sku] = inventarioItem
                    }
                }
            }
        }

        return inventario
    }

    /**
     * Valida el formato del CSV del sistema.
     * Headers esperados: SKU, Descripción, LibrUtiliz, TipoTarima, etc.
     */
    fun validarFormatoCSVSistema(headers: List<String>): Boolean {
        if (headers.size < 3) return false

        val requiredHeaders = listOf("sku", "descripcion", "librutiliz")
        return requiredHeaders.all { required ->
            headers.any { header ->
                header.lowercase().contains(required) ||
                header.lowercase().replace(" ", "").contains(required)
            }
        }
    }

    /**
     * Valida el formato del CSV de inventario escaneado.
     * Headers esperados: SKU, Descripción, Pallets, etc.
     */
    fun validarFormatoCSVEscaneado(headers: List<String>): Boolean {
        if (headers.size < 3) return false

        val requiredHeaders = listOf("sku", "pallets")
        return requiredHeaders.all { required ->
            headers.any { header ->
                header.lowercase().contains(required) ||
                header.lowercase().replace(" ", "").contains(required)
            }
        }
    }

    /**
     * Convierte InventarioItem a MaterialItem para compatibilidad.
     */
    fun convertirInventarioAMaterialItem(inventarioItem: InventarioItem): MaterialItem {
        return MaterialItem(
            sku = inventarioItem.sku,
            descripcion = inventarioItem.descripcion,
            cxPal = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            fpc = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            con = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            centro = inventarioItem.centro,
            linea = inventarioItem.linea,
            op = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            fProd = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            diasV = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            ubicacion = inventarioItem.ubicacion,
            totalPallets = inventarioItem.totalPallets.toString(),
            restos = "", // Campo no disponible en InventarioItem, se proporciona valor por defecto
            tipoTarima = inventarioItem.tipoTarima
        )
    }

    /**
     * Genera un resumen estadístico de un inventario.
     */
    fun generarResumenInventario(inventario: Map<String, InventarioItem>): String {
        if (inventario.isEmpty()) {
            return "Inventario vacío"
        }

        val totalItems = inventario.size
        val totalPallets = inventario.values.sumOf { it.totalPallets }
        val centros = inventario.values.map { it.centro }.filter { it.isNotBlank() }.distinct()
        val ubicaciones = inventario.values.map { it.ubicacion }.filter { it.isNotBlank() }.distinct()

        return buildString {
            appendLine("📦 Resumen del Inventario:")
            appendLine("• Total de SKUs: $totalItems")
            appendLine("• Total de pallets: $totalPallets")
            if (centros.isNotEmpty()) {
                appendLine("• Centros: ${centros.size} (${centros.take(3).joinToString(", ")}${if (centros.size > 3) "..." else ""})")
            }
            if (ubicaciones.isNotEmpty()) {
                appendLine("• Ubicaciones: ${ubicaciones.size}")
            }
        }
    }
}
