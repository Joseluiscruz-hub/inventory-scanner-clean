package com.joseluiscruz.capturainventario

import android.content.Context
import android.content.SharedPreferences
import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.repository.SharedPreferencesInventorySessionStore
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InventorySessionStoreTest {

    @Test
    fun `restaura la sesion persistida sin consultar Room`() = runTest {
        val fixture = fixture(persistedSession = "SESION_PERSISTIDA")

        val store = SharedPreferencesInventorySessionStore(
            fixture.context,
            fixture.dao,
            StandardTestDispatcher(testScheduler)
        )

        assertEquals("SESION_PERSISTIDA", store.awaitSessionId())
        verify(fixture.dao, never()).obtenerSesionMasReciente()
    }

    @Test
    fun `adopta la sesion mas reciente de Room durante la primera actualizacion`() = runTest {
        val fixture = fixture(persistedSession = null)
        whenever(fixture.dao.obtenerSesionMasReciente()).thenReturn("SESION_LEGACY")

        val store = SharedPreferencesInventorySessionStore(
            fixture.context,
            fixture.dao,
            StandardTestDispatcher(testScheduler)
        )

        assertEquals("SESION_LEGACY", store.awaitSessionId())
        verify(fixture.editor).putString(any(), eq("SESION_LEGACY"))
        verify(fixture.editor).commit()
    }

    @Test
    fun `crear una toma nueva persiste y publica otro identificador`() = runTest {
        val fixture = fixture(persistedSession = "SESION_ANTERIOR")
        val store = SharedPreferencesInventorySessionStore(
            fixture.context,
            fixture.dao,
            StandardTestDispatcher(testScheduler)
        )

        val nueva = store.startNewSession()

        assertNotEquals("SESION_ANTERIOR", nueva)
        assertEquals(nueva, store.awaitSessionId())
        verify(fixture.editor).putString(any(), eq(nueva))
        verify(fixture.editor).commit()
    }

    private fun fixture(persistedSession: String?): Fixture {
        val context = mock<Context>()
        val preferences = mock<SharedPreferences>()
        val editor = mock<SharedPreferences.Editor>()
        val dao = mock<InventarioDao>()

        whenever(context.getSharedPreferences(any(), eq(Context.MODE_PRIVATE)))
            .thenReturn(preferences)
        whenever(preferences.getString(any(), isNull())).thenReturn(persistedSession)
        whenever(preferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.commit()).thenReturn(true)

        return Fixture(context, dao, editor)
    }

    private data class Fixture(
        val context: Context,
        val dao: InventarioDao,
        val editor: SharedPreferences.Editor
    )
}
