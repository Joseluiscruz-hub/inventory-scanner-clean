package com.joseluiscruz.capturainventario.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Manager para feedback háptico (vibración) y sonoro.
 * Provee métodos estandarizados para notificar éxito, error y advertencia.
 */
class FeedbackManager(context: Context) {

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    /**
     * Feedback de Éxito: Vibración corta y tono agudo.
     */
    fun playSuccess() {
        vibrateSuccess()
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Feedback de Error: Vibración doble y tono grave.
     */
    fun playError() {
        vibrateError()
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Feedback de Advertencia: Vibración media.
     */
    fun playWarning() {
        vibrateWarning()
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 150)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playScanMatch() {
        vibrateOneShot(35)
        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playScanWarning() {
        vibrateOneShot(75)
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playScanError() {
        vibrateOneShot(140)
        try {
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 160)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Feedback de Click: Vibración muy ligera.
     */
    fun playClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    private fun vibrateSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(150)
        }
    }

    private fun vibrateError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }

    private fun vibrateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
    }

    private fun vibrateOneShot(durationMs: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMs)
        }
    }

    fun release() {
        toneGenerator.release()
    }
}
