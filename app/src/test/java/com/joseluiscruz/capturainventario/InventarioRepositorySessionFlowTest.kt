package com.joseluiscruz.capturainventario

import app.cash.turbine.test
import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.data.repository.InventorySessionStore
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class InventarioRepositorySessionFlowTest {

    @Test
    fun `los observadores cambian a la nueva sesion sin reiniciar la app`() = runTest {
        val dao = mock<InventarioDao>()
        val store = mock<InventorySessionStore>()
        val activeSession = MutableStateFlow("SESION_1")
        val session1Items = MutableStateFlow(listOf(item("SESION_1", "SKU-1")))
        val session2Items = MutableStateFlow(listOf(item("SESION_2", "SKU-2")))

        whenever(store.sessionId).thenReturn(activeSession)
        whenever(dao.observarInventarioEscaneado("SESION_1")).thenReturn(session1Items)
        whenever(dao.observarInventarioEscaneado("SESION_2")).thenReturn(session2Items)

        val repository = InventarioRepository(dao, store)

        repository.observarEscaneados().test {
            assertEquals("SKU-1", awaitItem().single().codigo)

            activeSession.value = "SESION_2"

            assertEquals("SKU-2", awaitItem().single().codigo)
        }
    }

    private fun item(sessionId: String, codigo: String) = InventarioEscaneadoEntity(
        sessionId = sessionId,
        codigo = codigo,
        descripcion = codigo,
        cantidadTotal = 1
    )
}
