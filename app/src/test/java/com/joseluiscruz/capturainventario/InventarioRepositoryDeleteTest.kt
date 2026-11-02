package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.repository.InventorySessionStore
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InventarioRepositoryDeleteTest {

    @Test
    fun `elimina de Room el grupo exacto mostrado por el resumen`() = runTest {
        val dao = mock<InventarioDao>()
        val store = mock<InventorySessionStore>()
        whenever(store.sessionId).thenReturn(MutableStateFlow("SESION_ACTIVA"))
        whenever(store.awaitSessionId()).thenReturn("SESION_ACTIVA")
        whenever(
            dao.eliminarGrupoConsolidado(
                "SESION_ACTIVA",
                "SKU-123",
                " A-01 ",
                "PT",
                "CHEP"
            )
        ).thenReturn(2)
        val repository = InventarioRepository(dao, store)

        val removed = repository.eliminarGrupoConsolidado(
            codigo = " sku-123 ",
            ubicacion = " A-01 ",
            almacen = "PT",
            tipoTarima = "CHEP"
        )

        assertEquals(2, removed)
        verify(dao).eliminarGrupoConsolidado(
            "SESION_ACTIVA",
            "SKU-123",
            " A-01 ",
            "PT",
            "CHEP"
        )
    }
}
