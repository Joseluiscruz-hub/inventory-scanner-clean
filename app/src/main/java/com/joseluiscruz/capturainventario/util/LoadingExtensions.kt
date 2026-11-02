package com.joseluiscruz.capturainventario.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Extension function para mostrar/ocultar estado de carga
 */
fun AppCompatActivity.mostrarCargando(mostrar: Boolean, loadingOverlay: View, loadingAnimation: com.airbnb.lottie.LottieAnimationView) {
    if (mostrar) {
        loadingOverlay.visibility = View.VISIBLE
        loadingAnimation.playAnimation()
    } else {
        loadingAnimation.pauseAnimation()
        loadingOverlay.visibility = View.GONE
    }
}

/**
 * Extension function para ejecutar operaciones asíncronas con manejo de loading
 */
fun <T> AppCompatActivity.executeWithLoading(
    loadingOverlay: View,
    loadingAnimation: com.airbnb.lottie.LottieAnimationView,
    backgroundTask: suspend () -> T,
    onResult: (T) -> Unit
) {
    mostrarCargando(true, loadingOverlay, loadingAnimation)
    
    lifecycleScope.launch {
        try {
            val resultado = withContext(Dispatchers.IO) {
                backgroundTask()
            }
            
            // Volver al hilo principal para actualizar la UI
            mostrarCargando(false, loadingOverlay, loadingAnimation)
            onResult(resultado)
        } catch (e: Exception) {
            mostrarCargando(false, loadingOverlay, loadingAnimation)
            // Manejar error si es necesario
        }
    }
}
