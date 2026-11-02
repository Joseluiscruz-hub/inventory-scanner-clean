package com.joseluiscruz.capturainventario.ui.almacenes

import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlmacenConfigurationRulesTest {

    @Test
    fun `entrada valida normaliza espacios y capacidad`() {
        val result = AlmacenConfigurationRules.validate(
            nombre = "  Almacén   Centro  ",
            capacidadText = " 450 ",
            existingNames = emptyList()
        )

        assertTrue(result.isValid)
        assertEquals("Almacén Centro", result.nombre)
        assertEquals(450, result.capacidad)
    }

    @Test
    fun `rechaza nombres duplicados sin importar mayusculas`() {
        val result = AlmacenConfigurationRules.validate(
            nombre = "CENTRO",
            capacidadText = "100",
            existingNames = listOf("Centro")
        )

        assertFalse(result.isValid)
        assertEquals("Ya existe un almacén con ese nombre", result.error)
    }

    @Test
    fun `permite guardar el mismo nombre durante edicion`() {
        val result = AlmacenConfigurationRules.validate(
            nombre = "Centro",
            capacidadText = "600",
            existingNames = listOf("Centro"),
            originalName = "Centro"
        )

        assertTrue(result.isValid)
        assertEquals(600, result.capacidad)
    }

    @Test
    fun `resumen nunca muestra disponibilidad negativa`() {
        val state = GestionAlmacenesUiState(
            almacenes = listOf(AlmacenCapacidad("Norte", 100, 125)),
            isLoading = false
        )

        assertEquals(0, state.totalDisponible)
        assertEquals(125.0, state.porcentajeOcupacion, 0.001)
    }
}
