package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.domain.model.ScanMode
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun `ScanMode fromString reconoce alias y fallback por defecto`() {
        assertEquals(ScanMode.MANUAL, ScanMode.fromString("manual"))
        assertEquals(ScanMode.PALLET, ScanMode.fromString("pallets"))
        assertEquals(ScanMode.RUMBA, ScanMode.fromString("RUMBA"))
        assertEquals(ScanMode.DEFAULT, ScanMode.fromString("desconocido"))
        assertEquals(ScanMode.DEFAULT, ScanMode.fromString(null))
    }
}

