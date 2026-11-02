package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.util.SapPortalUrlValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SapPortalUrlValidatorTest {

    @Test
    fun `agrega https a una liga empresarial sin esquema`() {
        assertEquals(
            "https://sap.empresa.com/reporte",
            SapPortalUrlValidator.normalize("sap.empresa.com/reporte")
        )
    }

    @Test
    fun `rechaza http y ligas sin host`() {
        assertNull(SapPortalUrlValidator.normalize("http://sap.empresa.com"))
        assertNull(SapPortalUrlValidator.normalize("https:///reporte"))
        assertNull(SapPortalUrlValidator.normalize(""))
    }
}
