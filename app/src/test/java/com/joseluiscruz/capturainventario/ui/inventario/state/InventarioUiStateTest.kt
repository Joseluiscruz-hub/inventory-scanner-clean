package com.joseluiscruz.capturainventario.ui.inventario.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventarioUiStateTest {

    @Test
    fun `GUARDAR se habilita con SKU escaneado aunque el manual este vacio (RUMBA)`() {
        val state = InventarioUiState(skuManual = "", scannedData = ScannedData(sku = "SKU123"))
        assertTrue(state.isGuardarButtonEnabled)
    }

    @Test
    fun `GUARDAR se habilita con SKU manual`() {
        assertTrue(InventarioUiState(skuManual = "SKU123").isGuardarButtonEnabled)
    }

    @Test
    fun `GUARDAR deshabilitado sin ningun SKU`() {
        assertFalse(InventarioUiState().isGuardarButtonEnabled)
    }

    @Test
    fun `GUARDAR deshabilitado mientras guarda o carga`() {
        val conSku = ScannedData(sku = "SKU123")
        assertFalse(InventarioUiState(scannedData = conSku, isSaving = true).isGuardarButtonEnabled)
        assertFalse(InventarioUiState(scannedData = conSku, isLoading = true).isGuardarButtonEnabled)
    }
}
