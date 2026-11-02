package com.joseluiscruz.capturainventario.ui.home.util

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.TextView

object TextAnimator {

    /**
     * Anima la entrada de varios botones con un efecto de rebote
     * @param views Los botones o vistas a animar
     */
    fun animarBotones(vararg views: View) {
        val delayBetweenItems = 150L
        
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * delayBetweenItems)
                .setDuration(500)
                .setInterpolator(OvershootInterpolator(1.2f))
                .start()
        }
    }

    /**
     * Crea un efecto de pulso continuo en una vista
     * @param view La vista a animar
     */
    fun animatePulse(view: View) {
        view.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(1000)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        animatePulse(view) // Repetir indefinidamente
                    }
                    .start()
            }
            .start()
    }

    /**
     * Escribe texto letra por letra con efecto de máquina de escribir
     * @param textView El TextView donde se mostrará el texto
     * @param texto El texto a escribir
     * @param delayMillis Retraso entre cada letra en milisegundos
     */
    fun escribirTexto(textView: TextView, texto: String, delayMillis: Long = 50) {
        val handler = Handler(Looper.getMainLooper())
        textView.text = ""
        
        for (i in texto.indices) {
            handler.postDelayed({
                textView.text = texto.substring(0, i + 1)
            }, delayMillis * i)
        }
    }
}
