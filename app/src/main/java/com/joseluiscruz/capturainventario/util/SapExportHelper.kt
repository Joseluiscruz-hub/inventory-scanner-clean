package com.joseluiscruz.capturainventario.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utilidad para generar archivos CSV compatibles con SAP MM para ajustes masivos de inventario.
 * 
 * Este helper genera archivos en el formato exacto que requiere SAP para cargar
 * movimientos de ajuste de inventario (Movimientos 701/702) mediante transacción MIGO
 * o carga masiva LSMW/BDC.
 * 
 * Formato de salida:
 * - SOCIEDAD: Código de sociedad PLANTA DE MANUFACTURA (ej. Demo1)
 * - ALMACEN: Código de almacén (ej. 0001)
 * - MATERIAL: SKU del material
 * - DESCRIPCION: Descripción del material
 * - TIPO_MOV: Tipo de movimiento SAP (701 = Entrada, 702 = Salida)
 * - CANTIDAD: Cantidad a ajustar (valor absoluto)
 * - UNIDAD: Unidad de medida (UN = Unidades)
 * - FECHA: Fecha del movimiento (dd.MM.yyyy)
 * 
 * Uso típico:
 * 1. El usuario finaliza el escaneo
 * 2. Se detectan diferencias (EXCESO/FALTANTE)
 * 3. Se genera CSV con SapExportHelper.generarAjusteCSV()
 * 4. Se comparte el archivo por email/Teams para carga en SAP
 */
object SapExportHelper {

    /**
     * Genera CSV compatible con carga masiva SAP MM - Movimientos 701 (entrada) / 702 (salida).
     * 
     * @param diferencias Lista de diferencias detectadas en el inventario
     * @param almacen Código de almacén SAP (por defecto "0001")
     * @param sociedad Código de sociedad SAP (por defecto "Demo1")
     * @param incluirHeader Si true, incluye la fila de encabezado (recomendado)
     * @return String con el contenido CSV formateado
     */
    fun generarAjusteCSV(
        diferencias: List<DiferenciaInventario>,
        almacen: String = "0001",
        sociedad: String = "Demo1",
        incluirHeader: Boolean = true
    ): String {
        val sb = StringBuilder()
        
        // Header SAP estándar para MIGO masivo
        if (incluirHeader) {
            sb.appendLine("SOCIEDAD;ALMACEN;MATERIAL;DESCRIPCION;TIPO_MOV;CANTIDAD;UNIDAD;FECHA")
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val fechaHoy = dateFormat.format(Date())

        diferencias
            .filter {
                it.estado == EstadoDiferencia.EXCESO ||
                    it.estado == EstadoDiferencia.FALTANTE
            }
            .sortedByDescending { it.estado == EstadoDiferencia.EXCESO }
            .forEach { item ->
                // 701 = Entrada de mercancía (cuando hay exceso)
                // 702 = Salida de mercancía (cuando hay faltante)
                val tipoMov = if (item.diferencia > 0) "701" else "702"
                val cantidad = Math.abs(item.diferencia)
                
                // Limpiamos la descripción de caracteres que puedan romper el CSV
                val descripcionLimpia = item.descripcion
                    .replace(";", ",")  // Reemplazamos ; por , para no romper el delimiter
                    .replace("\n", " ")  // Eliminamos saltos de línea
                    .replace("\r", "")   // Eliminamos carriage returns
                
                sb.appendLine(
                    "$sociedad;$almacen;${item.codigo};$descripcionLimpia;$tipoMov;$cantidad;UN;$fechaHoy"
                )
            }

        return sb.toString()
    }

    /**
     * Genera CSV con el reporte completo de inventario (incluye coincidencias).
     * Útil para auditoría y trazabilidad completa.
     */
    fun generarReporteCompletoCSV(
        diferencias: List<DiferenciaInventario>,
        operador: String = "",
        almacen: String = "0001"
    ): String {
        val sb = StringBuilder()
        
        // Header personalizado para reporte completo
        sb.appendLine("FECHA_HORA;OPERADOR;ALMACEN;MATERIAL;DESCRIPCION;CANT_SAP;CANT_FISICA;DIFERENCIA;ESTADO")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val fechaHora = dateFormat.format(Date())

        diferencias.forEach { item ->
            val descripcionLimpia = item.descripcion
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\r", "")
            
            sb.appendLine(
                "$fechaHora;$operador;$almacen;${item.codigo};$descripcionLimpia;" +
                "${item.cantidadReferencia};${item.cantidadActual};${item.diferencia};${item.estado}"
            )
        }

        return sb.toString()
    }

    /**
     * Guarda el archivo CSV y lanza el diálogo de compartir.
     * 
     * @param context Contexto de Android
     * @param contenido Contenido CSV a guardar
     * @param nombreArchivo Nombre base del archivo (sin extensión ni timestamp)
     * @param tipo Tipo de archivo: "ajuste_sap" o "reporte_completo"
     */
    fun guardarYCompartir(
        context: Context,
        contenido: String,
        nombreArchivo: String,
        tipo: String = "ajuste_sap"
    ) {
        // Directorio Documents del almacenamiento externo
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, "reports")
        documentsDir.mkdirs()
        
        // Nombre con timestamp para unicidad
        val timestamp = System.currentTimeMillis()
        val fileName = "${nombreArchivo}_${timestamp}.csv"
        
        val file = File(documentsDir, fileName)
        file.writeText(contenido, Charsets.UTF_8)

        // Generar URI compartible vía FileProvider
        // Usamos el authority definido en AndroidManifest: ${applicationId}.fileprovider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, when (tipo) {
                "ajuste_sap" -> "Ajuste SAP - $nombreArchivo"
                "reporte_completo" -> "Reporte Inventario - $nombreArchivo"
                else -> "Archivo CSV - $nombreArchivo"
            })
            putExtra(Intent.EXTRA_TEXT, when (tipo) {
                "ajuste_sap" -> "Archivo generado para carga masiva en SAP MM (Movimientos 701/702)"
                "reporte_completo" -> "Reporte completo de inventario con todas las diferencias detectadas"
                else -> "Archivo CSV generado desde EscaneoDemo"
            })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Enviar archivo CSV"))
    }

    /**
     * Obtiene la ruta del directorio de exportación para mostrar al usuario.
     */
    fun getExportDirectoryPath(context: Context): String {
        val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        return documentsDir?.absolutePath ?: context.filesDir.absolutePath
    }

    /**
     * Calcula el resumen de ajustes que se generarán.
     */
    fun calcularResumenAjustes(diferencias: List<DiferenciaInventario>): ResumenAjustes {
        val entradas = diferencias.filter { it.diferencia > 0 }
        val salidas = diferencias.filter { it.diferencia < 0 }
        
        return ResumenAjustes(
            totalAjustes = entradas.size + salidas.size,
            entradas701 = entradas.size,
            salidas702 = salidas.size,
            cantidadEntrada = entradas.sumOf { it.diferencia },
            cantidadSalida = Math.abs(salidas.sumOf { it.diferencia })
        )
    }
}

/**
 * Data class para resumen de ajustes SAP.
 */
data class ResumenAjustes(
    val totalAjustes: Int = 0,
    val entradas701: Int = 0,
    val salidas702: Int = 0,
    val cantidadEntrada: Int = 0,
    val cantidadSalida: Int = 0
)
