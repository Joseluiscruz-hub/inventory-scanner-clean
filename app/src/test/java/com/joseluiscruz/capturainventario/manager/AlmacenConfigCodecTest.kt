package com.joseluiscruz.capturainventario.manager

import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import org.junit.Assert.assertEquals
import org.junit.Test

class AlmacenConfigCodecTest {

    @Test
    fun `datos legacy incompletos se limpian sin crear almacenes invalidos`() {
        val json = """
            [
              null,
              {"nombreAlmacen":null,"capacidadMaxima":120},
              {"nombreAlmacen":"   ","capacidadMaxima":80},
              {"nombreAlmacen":"Norte","capacidadMaxima":0},
              {"nombreAlmacen":" Centro ","capacidadMaxima":250,"palletsEscaneados":-4}
            ]
        """.trimIndent()

        val result = AlmacenConfigCodec.decodificar(json)

        assertEquals(
            listOf(AlmacenCapacidad("Centro", 250, 0)),
            result
        )
    }

    @Test
    fun `normalizacion elimina duplicados sin importar mayusculas`() {
        val result = AlmacenConfigCodec.normalizar(
            listOf(
                AlmacenCapacidad(" Norte ", 100, -1),
                AlmacenCapacidad("norte", 200, 20),
                AlmacenCapacidad("Sur", 300, 30)
            )
        )

        assertEquals(
            listOf(
                AlmacenCapacidad("Norte", 100, 0),
                AlmacenCapacidad("Sur", 300, 30)
            ),
            result
        )
    }
}
