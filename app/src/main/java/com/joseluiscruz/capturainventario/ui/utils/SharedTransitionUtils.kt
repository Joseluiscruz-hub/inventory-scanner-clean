package com.joseluiscruz.capturainventario.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * Shared Transition Utilities
 */

// ==================== ANIMACIONES DE ENTRADA/SALIDA ====================

fun fadeThroughAnimation(): EnterTransition = fadeIn(
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + scaleIn(
    animationSpec = tween(300, easing = FastOutSlowInEasing),
    initialScale = 0.95f
)

fun fadeThroughExit(): ExitTransition = fadeOut(
    animationSpec = tween(300, easing = FastOutSlowInEasing)
) + scaleOut(
    animationSpec = tween(300, easing = FastOutSlowInEasing),
    targetScale = 0.95f
)

fun slideThroughEnter(slideDirection: SlideDirection = SlideDirection.Left): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        initialOffsetX = { if (slideDirection == SlideDirection.Left) it else -it }
    ) + fadeIn(animationSpec = tween(350))

fun slideThroughExit(slideDirection: SlideDirection = SlideDirection.Left): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        targetOffsetX = { if (slideDirection == SlideDirection.Left) -it else it }
    ) + fadeOut(animationSpec = tween(350))

fun sharedAxisEnter(isForward: Boolean): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        initialOffsetX = { if (isForward) it / 2 else -it / 2 }
    ) + fadeIn(animationSpec = tween(300))

fun sharedAxisExit(isForward: Boolean): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        targetOffsetX = { if (isForward) -it / 2 else it / 2 }
    ) + fadeOut(animationSpec = tween(300))

// ==================== COMPONENTES CON TRANSICIÓN COMPARTIDA ====================

@Composable
fun SharedImageCard(
    imagePainter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    transitionKey: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label = "cardScale"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = null
            )
    ) {
        androidx.compose.foundation.Image(
            painter = imagePainter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
        )
        
        if (title != null || subtitle != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                if (title != null) {
                    androidx.compose.material3.Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        color = BrandColors.BrandBlack
                    )
                }
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.material3.Text(
                        text = subtitle,
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                        color = BrandColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SharedContainer(
    key: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

// ==================== ANIMACIONES DE LISTA ====================

@Composable
fun <T> AnimatedList(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    content: @Composable ColumnScope.(index: Int, item: T) -> Unit
) {
    Column {
        items.forEachIndexed { index, item ->
            AnimatedListItem(
                index = index,
                key = key?.invoke(item) ?: index,
                content = { content(index, item) }
            )
        }
    }
}

@Composable
fun AnimatedListItem(
    index: Int,
    key: Any,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(key) {
        visible = true
    }
    
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else 50f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = index * 50,
            easing = FastOutSlowInEasing
        ),
        label = "offset"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            delayMillis = index * 50,
            easing = FastOutSlowInEasing
        ),
        label = "alpha"
    )
    
    Box(
        modifier = Modifier
            .offset { androidx.compose.ui.unit.IntOffset(0, offset.toInt()) }
            .alpha(alpha)
    ) {
        content()
    }
}

// ==================== ANIMACIONES DE CONTENIDO ====================

@Composable
fun AnimatedContentVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            animationSpec = tween(300),
            initialScale = 0.95f
        ),
        exit = fadeOut(animationSpec = tween(300)) + scaleOut(
            animationSpec = tween(300),
            targetScale = 0.95f
        ),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun ExpandableContent(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(300)),
        modifier = modifier
    ) {
        content()
    }
}

// ==================== TRANSICIONES PERSONALIZADAS ====================

fun bounceEnter(): EnterTransition {
    val springSpec = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessLow
    )
    
    return scaleIn(animationSpec = springSpec, initialScale = 0.5f) +
            fadeIn(animationSpec = tween(300))
}

fun slideUpFadeEnter(): EnterTransition = slideInVertically(
    animationSpec = tween(400, easing = FastOutSlowInEasing),
    initialOffsetY = { it }
) + fadeIn(animationSpec = tween(400))

fun slideUpFadeExit(): ExitTransition = slideOutVertically(
    animationSpec = tween(300, easing = FastOutSlowInEasing),
    targetOffsetY = { it / 4 }
) + fadeOut(animationSpec = tween(300))

fun zoomEnter(): EnterTransition = scaleIn(
    animationSpec = tween(400, easing = FastOutSlowInEasing),
    initialScale = 0.5f
) + fadeIn(animationSpec = tween(400))

fun zoomExit(): ExitTransition = scaleOut(
    animationSpec = tween(300, easing = FastOutSlowInEasing),
    targetScale = 1.1f
) + fadeOut(animationSpec = tween(300))

enum class SlideDirection { Left, Right }
