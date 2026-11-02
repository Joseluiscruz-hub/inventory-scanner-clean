package com.joseluiscruz.capturainventario.dashboard

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.joseluiscruz.capturainventario.model.MaterialItem
import java.io.File

object ComparadorInventario {
    fun generarComparacion(
        escaneado: List<MaterialItem>,
        almacen: List<InventarioAlmacen>,
        baseTarimas: Map<String, String>
    ): List<ComparacionInventario> {
        val comparacion = mutableListOf<ComparacionInventario>()
        val agrupado =
            escaneado.groupBy { it.sku }.mapValues { it.value.sumOf { m -> m.totalPallets?.toIntOrNull() ?: 0 } }
        val mapaAlmacen = almacen.associateBy { it.sku }

        for ((sku, esc) in agrupado) {
            val almacenado = mapaAlmacen[sku]
            val descripcion = escaneado.find { it.sku == sku }?.descripcion ?: "-"
            val tipo = baseTarimas[sku] ?: "OTRA"

            if (almacenado != null) {
                val diferencia = esc - almacenado.disponible
                val estado = when {
                    diferencia == 0 -> "OK"
                    diferencia > 0 -> "Sobrante ($diferencia)"
                    else -> "Faltante (${kotlin.math.abs(diferencia)})"
                }
                comparacion.add(
                    ComparacionInventario(
                        sku,
                        descripcion,
                        tipo,
                        esc,
                        almacenado.disponible,
                        diferencia,
                        estado
                    )
                )
            } else {
                comparacion.add(
                    ComparacionInventario(
                        sku,
                        descripcion,
                        tipo,
                        esc,
                        null,
                        null,
                        "Sobrante (No en almacén)"
                    )
                )
            }
        }

        // Detectar faltantes
        val faltantes = mapaAlmacen.keys - agrupado.keys
        for (sku in faltantes) {
            val a = mapaAlmacen[sku]!!
            comparacion.add(
                ComparacionInventario(
                    sku,
                    a.descripcion,
                    baseTarimas[sku] ?: "OTRA",
                    null,
                    a.disponible,
                    null,
                    "Faltante (No escaneado)"
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
                writer.write("SKU,Descripción,Tarima,Escaneado,Inventario,Diferencia,Estado\n")
                comparacion.forEach {
                    val linea = listOf(
                        it.sku,
                        "\"${it.descripcion}\"",
                        it.tipoTarima,
                        it.escaneado?.toString() ?: "-",
                        it.inventario?.toString() ?: "-",
                        it.diferencia?.toString() ?: "-",
                        "\"${it.estado}\""
                    ).joinToString(",")
                    writer.write("$linea\n")
                }
            }
            onFinish(archivo)
        } catch (e: Exception) {
            e.printStackTrace()
            onFinish(null)
        }
    }

    fun enviarPorCorreo(context: Context, archivo: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Comparación de Inventario")
            putExtra(Intent.EXTRA_TEXT, "Adjunto el reporte de comparación generado por la app.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Enviar comparación por correo"))
    }
}
