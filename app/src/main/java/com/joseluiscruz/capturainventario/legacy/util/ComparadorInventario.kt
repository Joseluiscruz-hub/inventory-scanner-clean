package com.joseluiscruz.capturainventario.legacy.util

import android.content.Context
import android.widget.Toast
import androidx.core.content.FileProvider
import com.joseluiscruz.capturainventario.model.MaterialItem
import java.io.File

data class InventarioAlmacen(
    val sku: String,
    val descripcion: String,
    val disponible: Int,
    val centro: String
)

data class ComparacionInventario(
    val sku: String,
    val descripcion: String,
    val tipoTarima: String,
    val escaneado: Int?,
    val inventario: Int?,
    val diferencia: Int?,
    val estado: String
)

object ComparadorInventario {

    fun generarComparacion(
        escaneado: List<MaterialItem>,
        almacen: List<InventarioAlmacen>,
        baseTarimas: Map<String, String>
    ): List<ComparacionInventario> {
        val comparacion = mutableListOf<ComparacionInventario>()
        val agrupadoEscaneado = escaneado.groupBy { it.sku }.mapValues { entry ->
            entry.value.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        }
        val mapaAlmacen = almacen.associateBy { it.sku }

        for ((sku, totalEscaneado) in agrupadoEscaneado) {
            val almacenado = mapaAlmacen[sku]
            val descripcion = escaneado.find { it.sku == sku }?.descripcion ?: "-"
            val tipo = baseTarimas[sku] ?: "OTRA"

            if (almacenado != null) {
                val diferencia = totalEscaneado - almacenado.disponible
                val estado = when {
                    diferencia == 0 -> "OK"
                    diferencia > 0 -> "Sobrante ($diferencia)"
                    else -> "Faltante (${kotlin.math.abs(diferencia)})"
                }

                comparacion.add(
                    ComparacionInventario(
                        sku, descripcion, tipo,
                        escaneado = totalEscaneado,
                        inventario = almacenado.disponible,
                        diferencia = diferencia,
                        estado = estado
                    )
                )
            } else {
                comparacion.add(
                    ComparacionInventario(
                        sku, descripcion, tipo,
                        escaneado = totalEscaneado,
                        inventario = null,
                        diferencia = null,
                        estado = "Sobrante (No en almacén)"
                    )
                )
            }
        }

        // Buscar faltantes
        val escaneados = agrupadoEscaneado.keys
        val faltantes = mapaAlmacen.keys - escaneados

        for (sku in faltantes) {
            val alm = mapaAlmacen[sku]!!
            val tipo = baseTarimas[sku] ?: "OTRA"
            comparacion.add(
                ComparacionInventario(
                    sku = sku,
                    descripcion = alm.descripcion,
                    tipoTarima = tipo,
                    escaneado = null,
                    inventario = alm.disponible,
                    diferencia = null,
                    estado = "Faltante (No escaneado)"
                )
            )
        }

        return comparacion.sortedBy { it.sku }
    }

    fun exportarComparacionCSV(
        context: Context,
        comparacion: List<ComparacionInventario>,
        nombreArchivo: String = "comparacion_inventario.csv",
        onFinish: (File?) -> Unit
    ) {
        try {
            val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
            val archivo = File(reportsDir, nombreArchivo)
            archivo.bufferedWriter().use { writer ->
                // Fecha y hora de generación
                val fechaGeneracion = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())

                // Cabecera del archivo
                writer.write("COMPARACIÓN DE INVENTARIO - Generado: $fechaGeneracion\n\n")

                // Cabeceras de columnas (formato estándar para Excel)
                writer.write("SKU,Descripción,Tipo Tarima,Cantidad Escaneada,Cantidad Sistema,Diferencia,Estado\n")

                // Datos de comparación
                comparacion.forEach { fila ->
                    val linea = listOf(
                        fila.sku,
                        "\"${fila.descripcion.replace("\"", "\"\"")}\"", // Escape de comillas para CSV
                        fila.tipoTarima,
                        fila.escaneado?.toString() ?: "0",
                        fila.inventario?.toString() ?: "0",
                        fila.diferencia?.toString() ?: "0",
                        "\"${fila.estado}\""
                    ).joinToString(",")
                    writer.write("$linea\n")
                }

                // Línea en blanco antes del resumen
                writer.write("\n")

                // Resumen estadístico
                val coincidencias = comparacion.count { it.estado == "OK" }
                val faltantes = comparacion.count { it.estado.contains("Faltante") }
                val sobrantes = comparacion.count { it.estado.contains("Sobrante") }
                val totalItems = comparacion.size

                writer.write("\nRESUMEN DE COMPARACIÓN\n")
                writer.write("Total de SKUs,Coincidencias,Faltantes,Sobrantes\n")
                writer.write("$totalItems,$coincidencias,$faltantes,$sobrantes\n")

                // Totales de cantidades
                val totalEscaneado = comparacion.sumOf { it.escaneado ?: 0 }
                val totalSistema = comparacion.sumOf { it.inventario ?: 0 }
                val diferenciaTotal = totalEscaneado - totalSistema

                writer.write("\nTOTALES DE PALLETS\n")
                writer.write("Total Escaneado,Total Sistema,Diferencia Total\n")
                writer.write("$totalEscaneado,$totalSistema,$diferenciaTotal\n")
            }
            onFinish(archivo)
        } catch (e: Exception) {
            e.printStackTrace()
            onFinish(null)
        }
    }

    fun enviarPorCorreo(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Comparación de Inventario")
            putExtra(android.content.Intent.EXTRA_TEXT, "Adjunto archivo de comparación generado desde la app.")
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Enviar comparación por correo"))
    }
}
