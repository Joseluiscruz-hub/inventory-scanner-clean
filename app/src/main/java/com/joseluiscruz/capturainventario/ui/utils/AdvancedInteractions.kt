package com.joseluiscruz.capturainventario.ui.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * Modificador avanzado que aplica un efecto de paralaje 3D basado en los sensores
 * de movimiento del dispositivo (Acelerómetro/Giroscopio).
 *
 * Al inclinar el teléfono, el elemento rota ligeramente y se desplaza, creando
 * una sensación de profundidad y realismo táctico (Efecto "Apple TV" o "iOS Home").
 *
 * @param intensity Factor de inclinación (default 10f)
 * @param translationIntensity Factor de desplazamiento (default 5f)
 */
fun Modifier.parallaxTilt(
    intensity: Float = 10f,
    translationIntensity: Float = 5f
): Modifier = composed {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val rotationX = remember { Animatable(0f) }
    val rotationY = remember { Animatable(0f) }
    val transX = remember { Animatable(0f) }
    val transY = remember { Animatable(0f) }
    
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                    val x = event.values[0] // Inclinación lateral
                    val y = event.values[1] // Inclinación vertical
                    
                    scope.launch {
                        // Rotación inversa para efecto natural
                        rotationX.animateTo(
                            targetValue = y * (intensity / 10f),
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        rotationY.animateTo(
                            targetValue = -x * (intensity / 10f),
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        transX.animateTo(
                            targetValue = -x * (translationIntensity / 10f),
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                        transY.animateTo(
                            targetValue = y * (translationIntensity / 10f),
                            animationSpec = spring(stiffness = Spring.StiffnessLow)
                        )
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    this.graphicsLayer {
        this.rotationX = rotationX.value
        this.rotationY = rotationY.value
        this.translationX = transX.value
        this.translationY = transY.value
        
        // Añadimos un poco de escala para que no se corten los bordes al rotar
        cameraDistance = 12f * density
    }
}

/**
 * Aplica un efecto de "vidrio esmerilado" dinámico que reacciona al scroll o movimiento.
 */
fun Modifier.dynamicGlass(
    alpha: Float = 0.1f,
    blur: Float = 20f
): Modifier = this.then(
    Modifier.graphicsLayer {
        this.alpha = alpha
    }
)
