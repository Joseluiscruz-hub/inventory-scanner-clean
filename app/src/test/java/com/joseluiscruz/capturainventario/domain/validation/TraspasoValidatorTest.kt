package com.joseluiscruz.capturainventario.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TraspasoValidatorTest {

    @Test
    fun validTransferHasNoError() {
        assertNull(
            TraspasoValidator.validate(
                sku = "1001",
                tipoTarima = "PLASTICA",
                almacenOrigen = "A",
                almacenDestino = "B",
                palletsMover = 1,
                restosMover = 0
            )
        )
    }

    @Test
    fun rejectsNegativeQuantities() {
        assertEquals(
            "Las cantidades no pueden ser negativas",
            TraspasoValidator.validate("1001", "PLASTICA", "A", "B", -1, 0)
        )
    }

    @Test
    fun rejectsSameWarehouse() {
        assertEquals(
            "El almacén destino debe ser distinto al origen",
            TraspasoValidator.validate("1001", "PLASTICA", "A", "a", 1, 0)
        )
    }

    @Test
    fun rejectsEmptyMovement() {
        assertEquals(
            "Indica al menos una cantidad para traspasar",
            TraspasoValidator.validate("1001", "PLASTICA", "A", "B", 0, 0)
        )
    }
}
