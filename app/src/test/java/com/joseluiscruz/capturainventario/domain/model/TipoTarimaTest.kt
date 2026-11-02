package com.joseluiscruz.capturainventario.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TipoTarimaTest {

    @Test
    fun `la lista unificada conserva el orden y los valores que ya se guardaban`() {
        assertEquals(
            listOf("PLANTA DE MANUFACTURA", "SAMS", "IEQSA", "CHEP", "PLASTICO", "OTRA"),
            TipoTarima.etiquetas
        )
    }

    @Test
    fun `incluye todos los tipos del dialogo legacy de la Activity`() {
        assertTrue(TipoTarima.etiquetas.containsAll(listOf("PLANTA DE MANUFACTURA", "SAMS", "IEQSA", "CHEP")))
    }

    @Test
    fun `las etiquetas no se repiten`() {
        assertEquals(TipoTarima.etiquetas.size, TipoTarima.etiquetas.toSet().size)
    }

    @Test
    fun `fromEtiqueta ignora mayusculas y espacios`() {
        assertEquals(TipoTarima.DEMO_CORP, TipoTarima.fromEtiqueta("  planta de manufactura "))
        assertEquals(TipoTarima.CHEP, TipoTarima.fromEtiqueta("CHEP"))
    }

    @Test
    fun `fromEtiqueta devuelve null para valores historicos no listados`() {
        assertNull(TipoTarima.fromEtiqueta("RESTO"))
        assertNull(TipoTarima.fromEtiqueta(null))
    }
}
