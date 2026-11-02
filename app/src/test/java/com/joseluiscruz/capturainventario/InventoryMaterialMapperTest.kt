package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.data.repository.toMaterialItem
import org.junit.Assert.assertEquals
import org.junit.Test

class InventoryMaterialMapperTest {

    @Test
    fun `la proyeccion conserva lote y caducidad normalizada de Room`() {
        val entity = InventarioEscaneadoEntity(
            sessionId = "SESION_TEST",
            codigo = "SKU-123",
            descripcion = "Producto",
            cantidadTotal = 4,
            cantidadRestos = 2,
            fechaEtiqueta = "01/07/2026",
            fechaCaducidadEpochDay = 21_000,
            lote = "LOTE-7",
            almacen = "PT",
            ubicacion = "A-01",
            tipoTarima = "CHEP"
        )

        val material = entity.toMaterialItem()

        assertEquals("LOTE-7", material.op)
        assertEquals(21_000L, material.fechaCaducidadEpochDay)
        assertEquals("4", material.totalPallets)
        assertEquals("2", material.restos)
    }
}
