package com.joseluiscruz.capturainventario.util

import android.view.View
import android.widget.EditText

/**
 * Extension function para mostrar u ocultar una vista basada en una condición
 */
fun View.mostrarSiEs(condicion: Boolean) {
    this.visibility = if (condicion) View.VISIBLE else View.GONE
}

/**
 * Extension function para configurar un EditText con opciones de edición y visibilidad
 */
fun EditText.configurar(editable: Boolean, visible: Boolean = true) {
    this.isEnabled = editable
    this.visibility = if (visible) View.VISIBLE else View.GONE
}
