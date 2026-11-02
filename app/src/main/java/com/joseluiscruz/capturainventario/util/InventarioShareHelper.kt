package com.joseluiscruz.capturainventario.util

import android.content.Context
import android.content.Intent
import android.widget.Toast

object InventarioShareHelper {
    fun sendEmailReport(context: Context, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            context.startActivity(Intent.createChooser(intent, "Enviar reporte por correo..."))
        } catch (_: Exception) {
            Toast.makeText(context, "No hay aplicacion de correo instalada", Toast.LENGTH_SHORT).show()
        }
    }
}

