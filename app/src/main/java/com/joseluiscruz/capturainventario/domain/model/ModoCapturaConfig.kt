package com.joseluiscruz.capturainventario.domain.model

import android.widget.EditText
import androidx.annotation.StringRes

/**
 * Data class que define la configuración para cada modo de captura
 */
data class ModoCapturaConfig(
    @StringRes val titulo: Int,
    val camposVisibles: List<EditText> = emptyList(),
    val camposEditables: List<EditText> = emptyList(),
    val mostrarBotonEscaneo: Boolean = true,
    val mostrarBotonGuardar: Boolean = true,
    val validacionEspecial: Boolean = false
)
