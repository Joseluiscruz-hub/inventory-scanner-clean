package com.joseluiscruz.capturainventario.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

/**
 * Extension function para mostrar mensajes con Snackbar
 */
fun AppCompatActivity.mostrarMensaje(mensaje: String, esError: Boolean = false) {
    val rootView = findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(rootView, mensaje, Snackbar.LENGTH_LONG)
    
    if (esError) {
        // Usar colores del tema si están disponibles, sino usar colores por defecto
        try {
            snackbar.setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        } catch (e: Exception) {
            // Fallback a colores por defecto si hay problemas
        }
    }
    
    snackbar.show()
}

/**
 * Extension function para mostrar mensajes con Snackbar desde cualquier View
 */
fun View.mostrarMensaje(mensaje: String, esError: Boolean = false) {
    val snackbar = Snackbar.make(this, mensaje, Snackbar.LENGTH_LONG)
    
    if (esError) {
        try {
            snackbar.setBackgroundTint(ContextCompat.getColor(context, android.R.color.holo_red_dark))
            snackbar.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        } catch (e: Exception) {
            // Fallback a colores por defecto si hay problemas
        }
    }
    
    snackbar.show()
}
