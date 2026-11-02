package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Modificador que aplica una animación de "presión" premium a cualquier composable.
 *
 * Efecto: Al presionar, el elemento se encoge ligeramente (scale 0.95) con
 * un spring suave, dando feedback visual inmediato de que el toque fue registrado.
 * Al soltar, vuelve a su tamaño original con un rebote sutil.
 *
 * @param onClick Acción a ejecutar al hacer click
 * @param enabled Si el componente es interactivo
 * @param scaleOnPress Factor de escala al presionar (default 0.95 = 5% más pequeño)
 * @param hapticEnabled Si se envía feedback háptico al presionar
 */
fun Modifier.premiumClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    scaleOnPress: Float = 0.95f,
    hapticEnabled: Boolean = true
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleOnPress else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null, // Sin ripple predeterminado (usamos nuestra animación)
            enabled = enabled,
            onClick = {
                if (hapticEnabled) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            }
        )
}

/**
 * Modificador para animación de entrada escalonada (staggered).
 *
 * Cada elemento aparece con un retraso proporcional a su índice,
 * creando un efecto de cascada visual muy satisfactorio.
 *
 * @param index Índice del elemento en la lista (0, 1, 2, ...)
 * @param baseDelay Delay base entre cada elemento en ms (default 60ms)
 * @param duration Duración total de la animación por elemento
 */
fun Modifier.staggeredEntry(
    index: Int,
    baseDelay: Int = 60,
    duration: Int = 500
): Modifier = composed {
    var hasPlayed by rememberSaveable(index) { mutableStateOf(false) }
    val alpha = remember { Animatable(if (hasPlayed) 1f else 0f) }
    val offsetY = remember { Animatable(if (hasPlayed) 0f else 40f) }
    val scale = remember { Animatable(if (hasPlayed) 1f else 0.92f) }

    LaunchedEffect(index) {
        if (hasPlayed) return@LaunchedEffect
        delay((index * baseDelay).toLong())
        launch {
            alpha.animateTo(
                1f,
                tween(duration, easing = FastOutSlowInEasing)
            )
        }
        launch {
            offsetY.animateTo(
                0f,
                tween(duration, easing = FastOutSlowInEasing)
            )
        }
        launch {
            scale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        hasPlayed = true
    }

    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = offsetY.value
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * Variante simplificada para elementos que solo necesitan fade + slide.
 */
fun Modifier.fadeSlideIn(
    index: Int = 0,
    baseDelay: Int = 100,
    duration: Int = 600,
    slideOffset: Float = 30f
): Modifier = composed {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(slideOffset) }

    LaunchedEffect(Unit) {
        delay((index * baseDelay).toLong())
        launch {
            alpha.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
        }
        launch {
            offsetY.animateTo(0f, tween(duration, easing = FastOutSlowInEasing))
        }
    }

    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = offsetY.value
    }
}
