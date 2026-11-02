package com.joseluiscruz.capturainventario.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.UnknownHostException

class MensajesUsuarioTest {

    @Test
    fun `confirmacion muestra cuantos registros siguen sin sincronizar`() {
        val texto = MensajesUsuario.confirmacionNuevaToma(totalRegistros = 25, sinSincronizar = 3)
        assertTrue(texto.contains("25 registros"))
        assertTrue(texto.contains("3 registros siguen sin sincronizar"))
        assertTrue(texto.contains("Exporta un CSV"))
    }

    @Test
    fun `confirmacion usa singular con un solo registro`() {
        val texto = MensajesUsuario.confirmacionNuevaToma(totalRegistros = 1, sinSincronizar = 1)
        assertTrue(texto.contains("1 registro "))
        assertTrue(texto.contains("1 registro sigue sin sincronizar"))
    }

    @Test
    fun `confirmacion indica cuando todo ya esta sincronizado`() {
        val texto = MensajesUsuario.confirmacionNuevaToma(totalRegistros = 10, sinSincronizar = 0)
        assertTrue(texto.contains("ya están sincronizados"))
        assertFalse(texto.contains("se perderán"))
    }

    @Test
    fun `si no se pudo contar recomienda exportar`() {
        val texto = MensajesUsuario.confirmacionNuevaToma(totalRegistros = null, sinSincronizar = null)
        assertTrue(texto.contains("exporta un CSV"))
    }

    @Test
    fun `sin capturas no alarma`() {
        val texto = MensajesUsuario.confirmacionNuevaToma(totalRegistros = 0, sinSincronizar = 0)
        assertTrue(texto.contains("No hay capturas"))
    }

    @Test
    fun `los mensajes nunca exponen el mensaje tecnico de la excepcion`() {
        val tecnico = "SQLITE_FULL: database or disk is full (code 13)"
        assertFalse(MensajesUsuario.guardarEscaneo(IOException(tecnico)).contains(tecnico))
        assertFalse(MensajesUsuario.guardarEscaneo(IllegalStateException(tecnico)).contains(tecnico))
        assertFalse(MensajesUsuario.cargarReferenciaSap(IllegalStateException(tecnico)).contains(tecnico))
    }

    @Test
    fun `referencia SAP distingue problema de red incluso envuelto`() {
        val red = MensajesUsuario.cargarReferenciaSap(RuntimeException(UnknownHostException("host")))
        assertTrue(red.contains("conexión"))
        val otro = MensajesUsuario.cargarReferenciaSap(null)
        assertTrue(otro.contains("archivo de referencia"))
    }
}
