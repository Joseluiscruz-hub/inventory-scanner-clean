package com.joseluiscruz.capturainventario.ui.home.util

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

object WelcomeAnimator {

    fun animateLogo(view: View) {
        view.alpha = 0f
        view.translationY = -50f
        view.scaleX = 0.8f
        view.scaleY = 0.8f

        view.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setStartDelay(200)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Pulso suave posterior
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    fun pulseLoop(view: View) {
        view.animate()
            .scaleX(1.02f)
            .scaleY(1.02f)
            .setDuration(1000)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .withEndAction {
                        pulseLoop(view) // Bucle continuo
                    }
                    .start()
            }
            .start()
    }
}

