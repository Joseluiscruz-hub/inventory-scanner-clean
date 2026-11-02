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

class InventarioRepositoryResetTest {

    @Test
    fun `nueva toma elimina eventos y publica una sesion diferente`() = runTest {
        val dao = mock<InventarioDao>()
        val sessionStore = mock<InventorySessionStore>()
        whenever(sessionStore.sessionId).thenReturn(MutableStateFlow("SESION_ANTERIOR"))
        whenever(sessionStore.awaitSessionId()).thenReturn("SESION_ANTERIOR")
        whenever(sessionStore.startNewSession()).thenReturn("SESION_NUEVA")
        val repository = InventarioRepository(dao, sessionStore)

        val sessionId = repository.iniciarNuevaToma()

        verify(dao).limpiarTodosLosInventarios()
        verify(sessionStore).startNewSession()
        assertEquals("SESION_NUEVA", sessionId)
    }
}
