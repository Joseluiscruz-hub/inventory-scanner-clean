package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.data.ProductoTerminadoCatalogo
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests para ProductoTerminadoCatalogo
 * Verifica la lógica de cálculo de cajas para productos terminados
 */
class ProductoTerminadoCatalogoTest {

    @Test
    fun `SKU 445 es producto terminado con 64 cajas por pallet`() {
        assertTrue(ProductoTerminadoCatalogo.esProductoTerminado("445"))
        assertEquals(64, ProductoTerminadoCatalogo.obtenerCajasXPallet("445"))
    }

    @Test
    fun `SKU 2352 es producto terminado con 36 cajas por pallet`() {
        assertTrue(ProductoTerminadoCatalogo.esProductoTerminado("2352"))
        assertEquals(36, ProductoTerminadoCatalogo.obtenerCajasXPallet("2352"))
    }

    @Test
    fun `SKU 195051 es material tarima plastica`() {
        assertTrue(ProductoTerminadoCatalogo.esMaterial("195051"))
        assertFalse(ProductoTerminadoCatalogo.esProductoTerminado("195051"))
    }

    @Test
    fun `calcular total cajas para SKU 2352 con 64 pallets`() {
        // Ejemplo del usuario: 64 pallets del SKU 2352 (36 cajas/pallet) = 2304 cajas
        val resultado = ProductoTerminadoCatalogo.calcularInventarioPT("2352", 64)

        assertNotNull(resultado)
        assertEquals(2304, resultado!!.totalCajas)
        assertEquals(64, resultado.totalPallets)
        assertEquals(64, resultado.tarimasPlasticas)
        assertEquals("195051", resultado.tarimaPlasticaSku)
    }

    @Test
    fun `calcular total cajas para SKU 445 con 10 pallets`() {
        // 10 pallets del SKU 445 (64 cajas/pallet) = 640 cajas
        val resultado = ProductoTerminadoCatalogo.calcularInventarioPT("445", 10)

        assertNotNull(resultado)
        assertEquals(640, resultado!!.totalCajas)
        assertEquals(10, resultado.totalPallets)
        assertEquals(10, resultado.tarimasPlasticas)
    }

    @Test
    fun `SKU 171836 usa tarima plastica 195046`() {
        val config = ProductoTerminadoCatalogo.obtenerConfiguracion("171836")

        assertNotNull(config)
        assertEquals(81, config!!.cajasXPallet)
        assertEquals("195046", config.tarimaPlasticaSku)
    }

    @Test
    fun `SKU 3017 usa configuracion global estandar de 32 cajas por pallet`() {
        assertTrue(ProductoTerminadoCatalogo.esProductoTerminado("3017"))
        assertEquals(32, ProductoTerminadoCatalogo.obtenerCajasXPallet("3017"))
    }

    @Test
    fun `catalogo global prevalece sobre override dinamico para SKU estandar`() {
        ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
            listOf(
                ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                    sku = "445",
                    cajasXPallet = 10,
                    tarimaPlasticaSku = ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR
                )
            )
        )

        assertEquals(64, ProductoTerminadoCatalogo.obtenerCajasXPallet("445"))

        ProductoTerminadoCatalogo.limpiarConfiguracionesDinamicas()
    }

    @Test
    fun `override dinamico aplica para SKU no estandar`() {
        val skuNoEstandar = "888888"
        ProductoTerminadoCatalogo.cargarConfiguracionesDinamicas(
            listOf(
                ProductoTerminadoCatalogo.ProductoTerminadoConfig(
                    sku = skuNoEstandar,
                    cajasXPallet = 20,
                    tarimaPlasticaSku = ProductoTerminadoCatalogo.TARIMA_PLASTICA_ESTANDAR
                )
            )
        )

        assertTrue(ProductoTerminadoCatalogo.esProductoTerminado(skuNoEstandar))
        assertEquals(20, ProductoTerminadoCatalogo.obtenerCajasXPallet(skuNoEstandar))

        ProductoTerminadoCatalogo.limpiarConfiguracionesDinamicas()
    }

    @Test
    fun `SKU desconocido no es producto terminado ni material`() {
        assertFalse(ProductoTerminadoCatalogo.esProductoTerminado("99999"))
        assertFalse(ProductoTerminadoCatalogo.esMaterial("99999"))
        assertNull(ProductoTerminadoCatalogo.calcularInventarioPT("99999", 10))
    }

    @Test
    fun `calcular con cero pallets retorna cero cajas`() {
        val resultado = ProductoTerminadoCatalogo.calcularInventarioPT("445", 0)

        assertNotNull(resultado)
        assertEquals(0, resultado!!.totalCajas)
        assertEquals(0, resultado.tarimasPlasticas)
    }

    @Test
    fun `todos los productos terminados tienen configuracion valida`() {
        val skus = ProductoTerminadoCatalogo.obtenerTodosLosSKUs()

        assertTrue(skus.isNotEmpty())
        skus.forEach { sku ->
            val config = ProductoTerminadoCatalogo.obtenerConfiguracion(sku)
            assertNotNull("SKU $sku debe tener configuración", config)
            assertTrue("SKU $sku debe tener cajas > 0", config!!.cajasXPallet > 0)
            assertTrue("SKU $sku debe tener tarima", config.tarimaPlasticaSku.isNotBlank())
        }
    }
}
