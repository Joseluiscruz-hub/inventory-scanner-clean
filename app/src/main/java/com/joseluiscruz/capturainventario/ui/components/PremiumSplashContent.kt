package com.joseluiscruz.capturainventario.ui.components

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.joseluiscruz.capturainventario.R

@Composable
fun PremiumSplashContent(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    // Animación de pulso elegante para el resplandor de fondo
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Animación de rotación para elementos decorativos
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Estados de animación para entrada escalonada
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showFooter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showLogo = true
        delay(400)
        showText = true
        delay(600)
        showFooter = true
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Resplandor de fondo (Efecto Aura)
        Box(
            modifier = Modifier
                .size(400.dp)
                .alpha(glowAlpha)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandColors.BrandRed.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Contenedor principal con efecto Glassmorphism
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(1000)) + scaleIn(tween(1000, easing = OvershootInterpolator().toEasing())),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Círculos orbitales decorativos
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .rotate(rotation)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(-rotation * 1.5f)
                            .border(0.5.dp, BrandColors.BrandRed.copy(alpha = 0.2f), CircleShape)
                    )

                    // Tarjeta de cristal central
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_inventory),
                            contentDescription = "Logo",
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Textos Informativos escalonados
            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PLANTA DE MANUFACTURA",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandColors.BrandRed,
                        letterSpacing = 6.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "INVENTORY SCANNER PRO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        letterSpacing = 2.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Badge de Enterprise Edition
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BrandColors.BrandRed.copy(alpha = 0.1f))
                            .border(0.5.dp, BrandColors.BrandRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ENTERPRISE CORPORATE EDITION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandColors.BrandRed,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Footer con progreso y versión
            AnimatedVisibility(
                visible = showFooter,
                enter = fadeIn(tween(1000))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    PremiumLoadingIndicator()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Versión ${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.3f),
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "ACCESO RESTRINGIDO · PERSONAL AUTORIZADO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.2f),
                        letterSpacing = 1.5.sp
                    )
                }
            }
        }
    }
}

// Extensión para simplificar el uso de interpoladores en Compose
private fun android.view.animation.Interpolator.toEasing() = Easing { x -> getInterpolation(x) }

@Composable
fun PremiumLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.3f at 0
                1f at 300
                0.3f at 600
                0.3f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1Alpha"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.3f at 0
                0.3f at 300
                1f at 600
                0.3f at 900
                0.3f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2Alpha"
    )
    
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0.3f at 0
                0.3f at 600
                1f at 900
                0.3f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3Alpha"
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Punto 1
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandColors.BrandRed.copy(alpha = dot1Alpha),
                            BrandColors.BrandDarkRed.copy(alpha = dot1Alpha * 0.7f)
                        )
                    )
                )
        )
        
        // Punto 2
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandColors.BrandRed.copy(alpha = dot2Alpha),
                            BrandColors.BrandDarkRed.copy(alpha = dot2Alpha * 0.7f)
                        )
                    )
                )
        )
        
        // Punto 3
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            BrandColors.BrandRed.copy(alpha = dot3Alpha),
                            BrandColors.BrandDarkRed.copy(alpha = dot3Alpha * 0.7f)
                        )
                    )
                )
        )
    }
}
