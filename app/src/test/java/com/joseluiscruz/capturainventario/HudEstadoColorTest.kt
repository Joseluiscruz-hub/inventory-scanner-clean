package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.model.EstadoColor
import com.joseluiscruz.capturainventario.ui.comparacion.DiferenciaInventario
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.comparacion.toEstadoColor
import org.junit.Assert.assertEquals
import org.junit.Test

class HudEstadoColorTest {

    @Test
    fun `conteo sin referencia siempre usa estado visual neutro`() {
        val item = diferencia(99, EstadoDiferencia.SIN_REFERENCIA)

        assertEquals(EstadoColor.NEUTRO, item.toEstadoColor())
    }

    @Test
    fun `diferencias reales conservan severidad amarilla y roja`() {
        assertEquals(
            EstadoColor.AMARILLO,
            diferencia(2, EstadoDiferencia.EXCESO).toEstadoColor()
        )
        assertEquals(
            EstadoColor.ROJO,
            diferencia(-4, EstadoDiferencia.FALTANTE).toEstadoColor()
        )
        assertEquals(
            EstadoColor.VERDE,
            diferencia(0, EstadoDiferencia.COINCIDE).toEstadoColor()
        )
    }

    private fun diferencia(delta: Int, estado: EstadoDiferencia) = DiferenciaInventario(
        codigo = "SKU",
        descripcion = "Producto",
        cantidadReferencia = 0,
        cantidadActual = delta.coerceAtLeast(0),
        diferencia = delta,
        estado = estado
    )
}
