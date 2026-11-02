package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.viewmodel.EstadisticasSesion
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para el cálculo de EstadisticasSesion.
 *
 * La lógica de las estadísticas vive en el map{} del StateFlow de InventarioSharedViewModel.
 * Este test valida la lógica pura de agregación sin necesidad de un ViewModel real.
 */
class EstadisticasSesionTest {

    // Función pura extraída de la lógica del ViewModel (sin dependencias de Android)
    private fun calcularEstadisticas(
        lista: List<DiferenciaInventario>,
        totalSistema: Int = lista.size
    ): EstadisticasSesion {
        val totalSKUs = lista.size
        val coincidencias = lista.count { it.estado == EstadoDiferencia.COINCIDE }
        val faltantes = lista.count { it.estado == EstadoDiferencia.FALTANTE }
        val excesos = lista.count { it.estado == EstadoDiferencia.EXCESO }
        val efectividad = if (totalSKUs > 0) (coincidencias * 100) / totalSKUs else 0
        val itemsEscaneados = lista.count { it.cantidadActual > 0 }
        val skusPendientes = totalSistema - itemsEscaneados

        return EstadisticasSesion(
            totalSKUs = totalSKUs,
            coincidencias = coincidencias,
            faltantes = faltantes,
            excesos = excesos,
            efectividad = efectividad,
            itemsEscaneados = itemsEscaneados,
            totalSistema = totalSistema,
            skusPendientes = skusPendientes
        )
    }

    private fun diferencia(
        codigo: String,
        estado: EstadoDiferencia,
        cantidadReferencia: Int = 10,
        cantidadActual: Int = 10,
    ) = DiferenciaInventario(
        codigo = codigo,
        descripcion = "Desc $codigo",
        cantidadReferencia = cantidadReferencia,
        cantidadActual = cantidadActual,
        diferencia = cantidadActual - cantidadReferencia,
        estado = estado,
        timestamp = System.currentTimeMillis()
    )

    // =========================================================
    // Lista vacía
    // =========================================================

    @Test
    fun `estadisticas para lista vacia son todos ceros`() {
        val stats = calcularEstadisticas(emptyList(), totalSistema = 0)

        assertEquals(0, stats.totalSKUs)
        assertEquals(0, stats.coincidencias)
        assertEquals(0, stats.faltantes)
        assertEquals(0, stats.excesos)
        assertEquals(0, stats.efectividad)
        assertEquals(0, stats.itemsEscaneados)
        assertEquals(0, stats.skusPendientes)
    }

    // =========================================================
    // Efectividad al 100%
    // =========================================================

    @Test
    fun `efectividad es 100 cuando todos coinciden`() {
        val lista = listOf(
            diferencia("A", EstadoDiferencia.COINCIDE),
            diferencia("B", EstadoDiferencia.COINCIDE),
            diferencia("C", EstadoDiferencia.COINCIDE)
        )
        val stats = calcularEstadisticas(lista)

        assertEquals(100, stats.efectividad)
        assertEquals(3, stats.coincidencias)
        assertEquals(0, stats.faltantes)
        assertEquals(0, stats.excesos)
    }

    // =========================================================
    // Efectividad al 0%
    // =========================================================

    @Test
    fun `efectividad es 0 cuando ningun SKU coincide`() {
        val lista = listOf(
            diferencia("A", EstadoDiferencia.FALTANTE, cantidadActual = 0),
            diferencia("B", EstadoDiferencia.EXCESO, cantidadActual = 20)
        )
        val stats = calcularEstadisticas(lista)

        assertEquals(0, stats.efectividad)
        assertEquals(0, stats.coincidencias)
    }

    // =========================================================
    // Efectividad parcial (entero, sin decimales)
    // =========================================================

    @Test
    fun `efectividad parcial se calcula correctamente como entero`() {
        // 2 coincidencias de 4 total = 50%
        val lista = listOf(
            diferencia("A", EstadoDiferencia.COINCIDE),
            diferencia("B", EstadoDiferencia.COINCIDE),
            diferencia("C", EstadoDiferencia.FALTANTE, cantidadActual = 0),
            diferencia("D", EstadoDiferencia.EXCESO, cantidadActual = 20)
        )
        val stats = calcularEstadisticas(lista)

        assertEquals(50, stats.efectividad)
        assertEquals(2, stats.coincidencias)
        assertEquals(1, stats.faltantes)
        assertEquals(1, stats.excesos)
    }

    // =========================================================
    // Items escaneados vs pendientes
    // =========================================================

    @Test
    fun `items escaneados cuenta solo SKUs con cantidad mayor a cero`() {
        val lista = listOf(
            diferencia("A", EstadoDiferencia.COINCIDE, cantidadActual = 10),
            diferencia("B", EstadoDiferencia.FALTANTE, cantidadActual = 0), // No escaneado
            diferencia("C", EstadoDiferencia.EXCESO, cantidadActual = 5)
        )
        val stats = calcularEstadisticas(lista, totalSistema = 5)

        assertEquals(2, stats.itemsEscaneados)  // A y C
        assertEquals(5, stats.totalSistema)
        assertEquals(3, stats.skusPendientes)   // 5 - 2 = 3
    }

    // =========================================================
    // Conteo de categorías
    // =========================================================

    @Test
    fun `conteo de excesos faltantes y coincidencias es preciso`() {
        val lista = listOf(
            diferencia("1", EstadoDiferencia.COINCIDE),
            diferencia("2", EstadoDiferencia.COINCIDE),
            diferencia("3", EstadoDiferencia.FALTANTE, cantidadActual = 0),
            diferencia("4", EstadoDiferencia.EXCESO, cantidadActual = 15),
            diferencia("5", EstadoDiferencia.EXCESO, cantidadActual = 30)
        )
        val stats = calcularEstadisticas(lista)

        assertEquals(5, stats.totalSKUs)
        assertEquals(2, stats.coincidencias)
        assertEquals(1, stats.faltantes)
        assertEquals(2, stats.excesos)
    }

    // =========================================================
    // Valor predeterminado de EstadisticasSesion
    // =========================================================

    @Test
    fun `EstadisticasSesion default tiene todos los campos en cero`() {
        val stats = EstadisticasSesion()

        assertEquals(0, stats.totalSKUs)
        assertEquals(0, stats.coincidencias)
        assertEquals(0, stats.faltantes)
        assertEquals(0, stats.excesos)
        assertEquals(0, stats.efectividad)
        assertEquals(0, stats.itemsEscaneados)
        assertEquals(0, stats.totalSistema)
        assertEquals(0, stats.skusPendientes)
    }
}
