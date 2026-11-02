package com.joseluiscruz.capturainventario.util

import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class EventoDetalleResumen(
    val material: MaterialItem,
    val lote: String,
    val fechaEtiqueta: String,
    val fechaCaducidadEpochDay: Long?,
    val diasRestantes: Int?,
    val cantidadPallets: Int,
    val almacen: String,
    val ubicacion: String,
    val riesgo: TpmRiskLevel
)

data class SkuDetalleResumen(
    val sku: String,
    val descripcion: String,
    val totalPallets: Int,
    val totalEventos: Int,
    val proximaCaducidadDias: Int?,
    val eventos: List<EventoDetalleResumen>
)

object InventarioResumenHelper {

    fun buildCsvFileName(now: Date = Date(), locale: Locale = Locale.getDefault()): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", locale).format(now)
        return "Inventario_${stamp}.csv"
    }

    fun buildEmailSubject(now: Date = Date(), locale: Locale = Locale.getDefault()): String {
        val stamp = SimpleDateFormat("dd/MM/yyyy", locale).format(now)
        return "Reporte Inventario - $stamp"
    }

    fun buildEmailBody(materiales: List<MaterialItem>, now: Date = Date(), locale: Locale = Locale.getDefault()): String {
        val resumen = StringBuilder()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
        
        // Encabezado Profesional
        resumen.append("==================================================\n")
        resumen.append("      PLANTA DE MANUFACTURA - REPORTE DE INVENTARIO      \n")
        resumen.append("==================================================\n")
        resumen.append("Fecha de Emisión: ${dateFormat.format(now)}\n")
        resumen.append("Generado por: Inventory Scanner Pro v2.0\n")
        resumen.append("--------------------------------------------------\n\n")

        // Resumen Ejecutivo
        val totalPallets = materiales.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        val totalCajas = materiales.sumOf {
            val pallets = it.totalPallets?.toIntOrNull() ?: 0
            val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(it.sku) ?: 0
            pallets * cajasXPallet
        }
        val totalSkus = materiales.map { it.sku }.distinct().size

        resumen.append("RESUMEN EJECUTIVO:\n")
        resumen.append("------------------\n")
        resumen.append("Total SKUs Escaneados: $totalSkus\n")
        resumen.append("Total Pallets: $totalPallets\n")
        resumen.append("Total Cajas Estimadas: $totalCajas\n")
        resumen.append("Total Tarimas Plásticas: $totalPallets pzs\n\n")

        // Desglose por Almacén
        resumen.append("DETALLE POR ALMACÉN:\n")
        resumen.append("---------------------\n")
        
        val agrupado = materiales.groupBy { it.almacen?.ifBlank { "Sin Almacen" } ?: "Sin Almacen" }

        agrupado.forEach { (almacen, items) ->
            val palletsAlmacen = items.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
            resumen.append("🏭 ALMACÉN: $almacen (Ocupación: $palletsAlmacen PLT)\n")
            resumen.append("  SKU     | EVENTOS | PALLETS | CAJAS   | LOTES\n")
            resumen.append("  --------|---------|---------|---------|-------\n")

            val porSku = items.groupBy { it.sku }
            porSku.forEach { (sku, lista) ->
                val totalPalletsSku = lista.sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
                val eventos = lista.size
                val cajasXPallet = ProductoTerminadoCatalogo.obtenerCajasXPallet(sku) ?: 0
                val totalCajasSku = totalPalletsSku * cajasXPallet
                
                val lotes = lista.map { inferLote(it) }.distinct().take(2).joinToString(", ")
                val lotesTxt = if (lotes.length > 10) lotes.take(7) + "..." else lotes

                resumen.append("  ${sku.padEnd(8)}| ${eventos.toString().padEnd(8)}| ${totalPalletsSku.toString().padEnd(8)}| ${totalCajasSku.toString().padEnd(8)}| $lotesTxt\n")
            }
            resumen.append("\n")
        }

        // Alertas TPM (Riesgo de Caducidad)
        val enRiesgo = materiales.filter { material ->
            TpmRiskPolicy.evaluar(
                material.fechaCaducidadEpochDay
                    ?: TpmRiskPolicy.calcularFechaCaducidadEpochDay(material.fProd, material.diasV)
            ).level == TpmRiskLevel.RIESGO_ALTO
        }

        if (enRiesgo.isNotEmpty()) {
            resumen.append("⚠️ ALERTAS DE CALIDAD (TPM < 40 DÍAS):\n")
            resumen.append("--------------------------------------\n")
            enRiesgo.groupBy { it.sku }.forEach { (sku, lista) ->
                val minDias = lista.mapNotNull { m -> 
                    TpmRiskPolicy.evaluar(
                        m.fechaCaducidadEpochDay
                            ?: TpmRiskPolicy.calcularFechaCaducidadEpochDay(m.fProd, m.diasV)
                    ).diasRestantes
                }.minOrNull()
                resumen.append("  - SKU: $sku | Próxima Caducidad: $minDias días\n")
            }
            resumen.append("\n")
        }

        resumen.append("--------------------------------------------------\n")
        resumen.append("Este reporte es de carácter informativo para uso interno\nde PLANTA DE MANUFACTURA. Los datos han sido capturados\nmediante escaneo directo en piso.\n")
        resumen.append("==================================================\n")

        return resumen.toString()
    }

    fun agruparDetallePorSku(materiales: List<MaterialItem>): List<SkuDetalleResumen> {
        val eventos = materiales.map { material ->
            val caducidad = material.fechaCaducidadEpochDay
                ?: TpmRiskPolicy.calcularFechaCaducidadEpochDay(material.fProd, material.diasV)
            val evaluacion = TpmRiskPolicy.evaluar(caducidad)
            EventoDetalleResumen(
                material = material,
                lote = inferLote(material),
                fechaEtiqueta = material.fProd.ifBlank { "N/A" },
                fechaCaducidadEpochDay = caducidad,
                diasRestantes = evaluacion.diasRestantes,
                cantidadPallets = material.totalPallets?.toIntOrNull() ?: 0,
                almacen = material.almacen?.ifBlank { "Sin almacen" } ?: "Sin almacen",
                ubicacion = material.ubicacion?.ifBlank { "N/A" } ?: "N/A",
                riesgo = evaluacion.level
            )
        }

        return eventos
            .groupBy { it.material.sku }
            .map { (sku, items) ->
                val eventosOrdenados = items.sortedWith(
                    compareBy<EventoDetalleResumen> { it.fechaCaducidadEpochDay == null }
                        .thenBy { it.fechaCaducidadEpochDay ?: Long.MAX_VALUE }
                        .thenBy { it.lote }
                )

                SkuDetalleResumen(
                    sku = sku,
                    descripcion = items.firstOrNull()?.material?.descripcion?.ifBlank { "SKU $sku" } ?: "SKU $sku",
                    totalPallets = items.sumOf { it.cantidadPallets },
                    totalEventos = items.size,
                    proximaCaducidadDias = eventosOrdenados.firstOrNull { it.diasRestantes != null }?.diasRestantes,
                    eventos = eventosOrdenados
                )
            }
            .sortedWith(
                compareBy<SkuDetalleResumen> { it.proximaCaducidadDias == null }
                    .thenBy { it.proximaCaducidadDias ?: Int.MAX_VALUE }
                    .thenBy { it.sku }
            )
    }

    private fun inferLote(material: MaterialItem): String {
        val lote = material.op.ifBlank { material.con }
        return lote.ifBlank { "Sin lote" }
    }
}

