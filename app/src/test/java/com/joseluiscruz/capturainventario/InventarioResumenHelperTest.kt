package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.util.InventarioResumenHelper
import com.joseluiscruz.capturainventario.util.TpmRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InventarioResumenHelperTest {

    @Test
    fun agruparDetallePorSku_ordenaEventosPorTpmYLote_ySkuSinFechaAlFinal() {
        val materiales = listOf(
            material(sku = "1002", fProd = "01/03/2026", diasV = "10", op = "L2", pallets = "2"),
            material(sku = "1001", fProd = "01/03/2026", diasV = "30", op = "L1", pallets = "1"),
            material(sku = "1001", fProd = "05/03/2026", diasV = "40", op = "L3", pallets = "1"),
            material(sku = "1003", fProd = "", diasV = "", op = "", pallets = "3")
        )

        val resultado = InventarioResumenHelper.agruparDetallePorSku(materiales)

        assertEquals(3, resultado.size)
        assertEquals("1002", resultado[0].sku)
        assertEquals("1001", resultado[1].sku)
        assertEquals("1003", resultado[2].sku)

        val eventos1001 = resultado[1].eventos
        assertEquals(2, eventos1001.size)
        assertTrue((eventos1001[0].fechaCaducidadEpochDay ?: Long.MAX_VALUE) <= (eventos1001[1].fechaCaducidadEpochDay ?: Long.MAX_VALUE))

        val skuSinFecha = resultado[2]
        assertEquals(TpmRiskLevel.SIN_FECHA, skuSinFecha.eventos.first().riesgo)
        assertEquals("Sin lote", skuSinFecha.eventos.first().lote)
    }

    private fun material(
        sku: String,
        fProd: String,
        diasV: String,
        op: String,
        pallets: String
    ): MaterialItem {
        return MaterialItem(
            sku = sku,
            descripcion = "",
            cxPal = "",
            fpc = "",
            con = "",
            centro = "",
            linea = "",
            op = op,
            fProd = fProd,
            diasV = diasV,
            ubicacion = "A1",
            totalPallets = pallets,
            restos = "0",
            tipoTarima = "",
            almacen = "MEX"
        )
    }
}

