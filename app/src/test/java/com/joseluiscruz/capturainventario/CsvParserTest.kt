package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.util.CsvParser
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager.ItemReferencia
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para CsvParser.
 *
 * Cubre los casos críticos del parsing de archivos CSV provenientes de SAP/Excel:
 * - Delimitadores mixtos (coma vs punto-y-coma)
 * - Campos entre comillas con comas internas
 * - BOM de Excel (\uFEFF)
 * - Líneas vacías y filas con columnas insuficientes
 * - Normalización uppercase de SKU
 * - Fallbacks de columnas faltantes
 */
class CsvParserTest {

    // =========================================================
    // parseCsvLineSafe — tokenizador base
    // =========================================================

    @Test
    fun `parseCsvLineSafe con coma divide correctamente`() {
        val resultado = CsvParser.parseCsvLineSafe("SKU001,PLANTA DE MANUFACTURA 600ml,10")
        assertEquals(listOf("SKU001", "PLANTA DE MANUFACTURA 600ml", "10"), resultado)
    }

    @Test
    fun `parseCsvLineSafe con punto y coma divide correctamente`() {
        val resultado = CsvParser.parseCsvLineSafe("SKU001;PLANTA DE MANUFACTURA 600ml;10", ';')
        assertEquals(listOf("SKU001", "PLANTA DE MANUFACTURA 600ml", "10"), resultado)
    }

    @Test
    fun `parseCsvLineSafe respeta campos entre comillas con coma interna`() {
        val resultado = CsvParser.parseCsvLineSafe("SKU001,\"PLANTA DE MANUFACTURA, 600ml\",10")
        assertEquals(listOf("SKU001", "PLANTA DE MANUFACTURA, 600ml", "10"), resultado)
    }

    @Test
    fun `parseCsvLineSafe devuelve lista unitaria para linea sin delimitador`() {
        val resultado = CsvParser.parseCsvLineSafe("SINDELIMITADOR")
        assertEquals(listOf("SINDELIMITADOR"), resultado.map { it.uppercase() })
    }

    @Test
    fun `parseCsvLineSafe trimea espacios en blanco`() {
        val resultado = CsvParser.parseCsvLineSafe("  SKU001  ,  Desc  ,  5  ")
        assertEquals("SKU001", resultado[0])
        assertEquals("Desc", resultado[1])
        assertEquals("5", resultado[2])
    }

    @Test
    fun `parseCsvLineSafe con campo vacio produce string vacio`() {
        val resultado = CsvParser.parseCsvLineSafe("SKU001,,10")
        assertEquals(3, resultado.size)
        assertEquals("", resultado[1])
    }

    // =========================================================
    // parseReferenciaCsvLine — parser de alto nivel
    // =========================================================

    @Test
    fun `parseReferenciaCsvLine parsea linea valida con coma`() {
        val resultado = CsvParser.parseReferenciaCsvLine("sku-001,PLANTA DE MANUFACTURA 600ml,100,24")

        assertNotNull(resultado)
        assertEquals("SKU-001", resultado!!.codigo)
        assertEquals("PLANTA DE MANUFACTURA 600ml", resultado.descripcion)
        assertEquals(100, resultado.cantidad)
        assertEquals(24, resultado.cajasXPallet)
    }

    @Test
    fun `parseReferenciaCsvLine parsea linea valida con punto y coma`() {
        val resultado = CsvParser.parseReferenciaCsvLine("SKU-002;Fanta 2L;50;12")

        assertNotNull(resultado)
        assertEquals("SKU-002", resultado!!.codigo)
        assertEquals("Fanta 2L", resultado.descripcion)
        assertEquals(50, resultado.cantidad)
        assertEquals(12, resultado.cajasXPallet)
    }

    @Test
    fun `parseReferenciaCsvLine convierte sku a uppercase`() {
        val resultado = CsvParser.parseReferenciaCsvLine("sku-lowercase,Desc,10")
        assertNotNull(resultado)
        assertEquals("SKU-LOWERCASE", resultado!!.codigo)
    }

    @Test
    fun `parseReferenciaCsvLine retorna null para linea con menos de 2 columnas`() {
        val resultado = CsvParser.parseReferenciaCsvLine("SOLOSKU")
        assertNull(resultado)
    }

    @Test
    fun `parseReferenciaCsvLine retorna null para sku en blanco`() {
        val resultado = CsvParser.parseReferenciaCsvLine(",Descripcion,10")
        assertNull(resultado)
    }

    @Test
    fun `parseReferenciaCsvLine usa cero como fallback cuando cantidad no es numerica`() {
        val resultado = CsvParser.parseReferenciaCsvLine("SKU001,Desc,N/A")
        assertNotNull(resultado)
        assertEquals(0, resultado!!.cantidad)
    }

    @Test
    fun `parseReferenciaCsvLine soporta linea de solo 2 columnas con cantidad cero`() {
        val resultado = CsvParser.parseReferenciaCsvLine("SKU999,Producto Minimo")
        assertNotNull(resultado)
        assertEquals("SKU999", resultado!!.codigo)
        assertEquals("Producto Minimo", resultado.descripcion)
        assertEquals(0, resultado.cantidad)
    }

    @Test
    fun `parseReferenciaCsvLine asigna armado desde cajasXPallet cuando armado esta en blanco`() {
        // Sin columna 4 (armado), cajasXPallet=20 → armado="20"
        val resultado = CsvParser.parseReferenciaCsvLine("SKU001,Desc,100,20")
        assertNotNull(resultado)
        assertEquals("20", resultado!!.armado)
    }

    @Test
    fun `parseReferenciaCsvLine ignora BOM de Excel al inicio`() {
        val lineaConBom = "\uFEFFSKU001,Desc,10"
        val resultado = CsvParser.parseReferenciaCsvLine(lineaConBom)
        // El SKU no debería contener el BOM
        assertNotNull(resultado)
        assertFalse(resultado!!.codigo.startsWith("\uFEFF"))
    }

    @Test
    fun `parseReferenciaCsvLine rechaza contenido binario de xlsx`() {
        val resultado = CsvParser.parseReferenciaCsvLine(
            "HF(XL_PIVOTCACHE_PIVOTCACHEDEFINITION11.XML,0,0"
        )

        assertNull(resultado)
    }

    @Test
    fun `parseReferenciaCsvLine rechaza archivos xlsx leidos como texto`() {
        val resultado = CsvParser.parseReferenciaCsvLine(
            "PK\u0003\u0004\u0014\u0006XL_WORKBOOK.XML,0,0"
        )

        assertNull(resultado)
    }

    @Test
    fun `parseReferenciaCsvLine rechaza sku con caracteres no permitidos`() {
        val resultado = CsvParser.parseReferenciaCsvLine("SKU<001>,Desc,10")

        assertNull(resultado)
    }

    @Test
    fun `isValidReferenceItem rechaza item corrupto desde nube`() {
        val item = ItemReferencia(
            codigo = "HF(XL_PIVOTCACHE_PIVOTCACHEDEFINITION11.XML",
            descripcion = "0",
            cantidad = 0
        )

        assertFalse(CsvParser.isValidReferenceItem(item))
    }
}
