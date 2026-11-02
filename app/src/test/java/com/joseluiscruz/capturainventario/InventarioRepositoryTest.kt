package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.dashboard.ComparadorInventario
import com.joseluiscruz.capturainventario.dashboard.InventarioAlmacen
import com.joseluiscruz.capturainventario.model.MaterialItem
import org.junit.Assert.assertEquals
import org.junit.Test

class InventarioRepositoryTest {
    @Test
    fun `comparador agrupa pallets repetidos por sku antes de comparar`() {
        val escaneado = listOf(
            MaterialItem("A1", "Producto A", "1", "", "", "C1", "", "", "", "", totalPallets = "2"),
            MaterialItem("A1", "Producto A", "1", "", "", "C1", "", "", "", "", totalPallets = "3")
        )
        val almacen = listOf(
            InventarioAlmacen(sku = "A1", descripcion = "Producto A", disponible = 4, centro = "C1")
        )

        val resultado = ComparadorInventario.generarComparacion(escaneado, almacen, baseTarimas = emptyMap())
        val comparacion = resultado.single { it.sku == "A1" }

        assertEquals(5, comparacion.escaneado)
        assertEquals(1, comparacion.diferencia)
        assertEquals("Sobrante (1)", comparacion.estado)
    }

    @Test
    fun `comparacion basica de inventario - caso OK y sobrante`() {
        val escaneado = listOf(
            MaterialItem(
                sku = "A1",
                descripcion = "Producto A",
                cxPal = "1",
                fpc = "",
                con = "",
                centro = "C1",
                linea = "",
                op = "",
                fProd = "",
                diasV = "",
                totalPallets = "10"
            ),
            MaterialItem(
                sku = "B2",
                descripcion = "Producto B",
                cxPal = "1",
                fpc = "",
                con = "",
                centro = "C1",
                linea = "",
                op = "",
                fProd = "",
                diasV = "",
                totalPallets = "5"
            )
        )
        val almacen = listOf(
            InventarioAlmacen(sku = "A1", descripcion = "Producto A", disponible = 10, centro = "C1"),
            InventarioAlmacen(sku = "C3", descripcion = "Producto C", disponible = 7, centro = "C1")
        )
        val baseTarimas = mapOf("A1" to "T1", "B2" to "T2", "C3" to "T3")

        val resultado = ComparadorInventario.generarComparacion(escaneado, almacen, baseTarimas)

        // SKU A1 debe estar OK
        val a1 = resultado.find { it.sku == "A1" }
        assertEquals("OK", a1?.estado)
        assertEquals(10, a1?.escaneado)
        assertEquals(10, a1?.inventario)
        // SKU B2 debe estar como sobrante
        val b2 = resultado.find { it.sku == "B2" }
        assertEquals("Sobrante (No en almacén)", b2?.estado)
        // SKU C3 debe estar como faltante
        val c3 = resultado.find { it.sku == "C3" }
        assertEquals("Faltante (No escaneado)", c3?.estado)
    }
}
