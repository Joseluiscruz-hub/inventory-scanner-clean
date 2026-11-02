package com.joseluiscruz.capturainventario.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Mensajes claros en español para el operador. El detalle técnico (e.message, stacktrace)
 * se loguea aparte; nunca se muestra crudo en Toast/Snackbar.
 */
object MensajesUsuario {

    private fun esProblemaDeRed(e: Throwable?): Boolean {
        var actual = e
        while (actual != null) {
            if (actual is UnknownHostException || actual is SocketTimeoutException) return true
            actual = actual.cause
        }
        return false
    }

    fun guardarEscaneo(e: Throwable?): String =
        if (e is IOException && !esProblemaDeRed(e)) {
            "No se pudo guardar la captura: revisa el espacio libre del dispositivo. Tus datos siguen en pantalla."
        } else {
            "No se pudo guardar la captura en el dispositivo. Tus datos siguen en pantalla."
        }

    fun cargarReferenciaSap(e: Throwable?): String =
        if (esProblemaDeRed(e)) {
            "No se pudo cargar la referencia SAP: sin conexión con el servidor. Revisa tu red."
        } else {
            "No se pudo cargar la referencia SAP. Verifica que exista un archivo de referencia importado."
        }

    const val QR_NO_RECONOCIDO =
        "No se reconoció el código. Verifica que sea una etiqueta de pallet y vuelve a escanear."

    const val SKU_VACIO = "Captura o escanea un SKU antes de guardar."

    const val PREPARAR_COMPARACION = "No se pudo preparar la comparación con SAP."

    const val EXPORTAR_CSV = "No se pudo exportar el CSV. Elige otra ubicación e inténtalo de nuevo."

    const val COPIA_CSV_PENDIENTE = "La copia CSV no se actualizó; la captura sí quedó guardada."

    /**
     * Texto de confirmación para "Nueva toma". Hace explícito cuántos registros se borran y
     * cuántos aún no llegan a la nube.
     */
    fun confirmacionNuevaToma(totalRegistros: Int?, sinSincronizar: Int?): String {
        if (totalRegistros == null || sinSincronizar == null) {
            return "Se eliminarán todas las capturas guardadas en este dispositivo y la referencia SAP. " +
                "No se pudo verificar cuántas faltan por sincronizar: exporta un CSV antes de continuar."
        }
        if (totalRegistros == 0) {
            return "No hay capturas guardadas en este dispositivo. Se limpiará la referencia SAP."
        }
        val base = "Se eliminarán $totalRegistros ${plural(totalRegistros, "registro", "registros")} " +
            "de este dispositivo y la referencia SAP. Esta acción no se puede deshacer."
        return if (sinSincronizar > 0) {
            "$base\n\n⚠ $sinSincronizar ${plural(sinSincronizar, "registro sigue", "registros siguen")} " +
                "sin sincronizar con la nube y se perderán. Exporta un CSV antes de borrar."
        } else {
            "$base\n\nTodos los registros ya están sincronizados con la nube."
        }
    }

    private fun plural(n: Int, singular: String, plural: String) = if (n == 1) singular else plural
}
