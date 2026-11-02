package com.joseluiscruz.capturainventario.ui.extensions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Modificador premium que combina clickable con retroalimentación háptica y ripple.
 * Proporciona una experiencia táctil de alta calidad al tocar elementos.
 */
fun Modifier.premiumClickable(
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState().value

    // Ejecutar haptic feedback cuando se presiona
    if (isPressed) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    this.then(
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(
                color = androidx.compose.ui.graphics.Color(0xFFE31837).copy(alpha = 0.1f)
            ),
            onClick = onClick
        )
    )
}
