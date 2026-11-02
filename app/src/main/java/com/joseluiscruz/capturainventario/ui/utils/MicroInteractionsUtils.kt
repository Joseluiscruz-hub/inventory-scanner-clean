package com.joseluiscruz.capturainventario.ui.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

// ==================== HAPTIC FEEDBACK ====================

fun View.performHapticFeedbackSafe(type: Int = HapticFeedbackConstants.CONFIRM) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        performHapticFeedback(type)
    } else {
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}

fun View.performLightHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}

fun View.performMediumHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
    } else {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

fun View.performStrongHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
    } else {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
}

fun View.performSuccessHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    } else {
        performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        postDelayed({
            performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }, 100)
    }
}

fun View.performErrorHapticFeedback() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        performHapticFeedback(HapticFeedbackConstants.REJECT)
    } else {
        performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

// ==================== MODIFIERS DE ANIMACIÓN ====================

fun Modifier.clickableWithScale(
    scalePressed: Float = 0.95f,
    scaleNormal: Float = 1f,
    animationSpec: AnimationSpec<Float> = tween(150, easing = FastOutSlowInEasing),
    onClick: () -> Unit
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else scaleNormal,
        animationSpec = animationSpec,
        label = "clickScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    LaunchedEffect(isPressedState) {
        isPressed = isPressedState
    }
    
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) {
            onClick()
        }
}

// ==================== BOTONES PREMIUM ====================

@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true,
    hapticType: Int = HapticFeedbackConstants.CONFIRM,
    scalePressed: Float = 0.95f,
    containerColor: Color = BrandColors.BrandRed,
    contentColor: Color = Color.White,
    elevation: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "buttonScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    LaunchedEffect(isPressedState) {
        isPressed = isPressedState
    }
    
    Button(
        onClick = {
            onClick()
            if (hapticFeedback) {
                view.performHapticFeedbackSafe(hapticType)
            }
        },
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = elevation / 2
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun PremiumOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true,
    scalePressed: Float = 0.95f,
    borderColor: Color = BrandColors.BrandRed,
    contentColor: Color = BrandColors.BrandRed,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "outlinedButtonScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    LaunchedEffect(isPressedState) {
        isPressed = isPressedState
    }
    
    OutlinedButton(
        onClick = {
            onClick()
            if (hapticFeedback) {
                view.performLightHapticFeedback()
            }
        },
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor
        ),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun PremiumIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true,
    scalePressed: Float = 0.9f,
    containerColor: Color = Color.Transparent,
    contentColor: Color = BrandColors.BrandRed,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = tween(100, easing = FastOutSlowInEasing),
        label = "iconButtonScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    LaunchedEffect(isPressedState) {
        isPressed = isPressedState
    }
    
    androidx.compose.material3.IconButton(
        onClick = {
            onClick()
            if (hapticFeedback) {
                view.performLightHapticFeedback()
            }
        },
        modifier = modifier.scale(scale),
        interactionSource = interactionSource,
        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        content()
    }
}

// ==================== COMPONENTES INTERACTIVOS ====================

@Composable
fun InteractiveCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true,
    scalePressed: Float = 0.98f,
    elevation: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "cardScale"
    )
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    
    LaunchedEffect(isPressedState) {
        isPressed = isPressedState
    }
    
    androidx.compose.material3.Card(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
                if (hapticFeedback) {
                    view.performLightHapticFeedback()
                }
            },
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = if (isPressed) elevation / 2 else elevation
        ),
        shape = shape
    ) {
        content()
    }
}

@Composable
fun PremiumSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true
) {
    val view = LocalView.current
    
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = {
            onCheckedChange(it)
            if (hapticFeedback) {
                view.performLightHapticFeedback()
            }
        },
        modifier = modifier,
        colors = androidx.compose.material3.SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = BrandColors.BrandRed,
            uncheckedThumbColor = Color.Gray,
            uncheckedTrackColor = Color.LightGray
        )
    )
}

@Composable
fun PremiumCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true
) {
    val view = LocalView.current
    
    androidx.compose.material3.Checkbox(
        checked = checked,
        onCheckedChange = {
            onCheckedChange(it)
            if (hapticFeedback) {
                view.performLightHapticFeedback()
            }
        },
        modifier = modifier,
        colors = androidx.compose.material3.CheckboxDefaults.colors(
            checkedColor = BrandColors.BrandRed,
            uncheckedColor = Color.Gray
        )
    )
}

@Composable
fun PremiumRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hapticFeedback: Boolean = true
) {
    val view = LocalView.current
    
    androidx.compose.material3.RadioButton(
        selected = selected,
        onClick = {
            onClick()
            if (hapticFeedback) {
                view.performLightHapticFeedback()
            }
        },
        modifier = modifier,
        colors = androidx.compose.material3.RadioButtonDefaults.colors(
            selectedColor = BrandColors.BrandRed,
            unselectedColor = Color.Gray
        )
    )
}
