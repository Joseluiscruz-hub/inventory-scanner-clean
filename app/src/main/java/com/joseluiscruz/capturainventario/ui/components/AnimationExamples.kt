package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

/**
 * Ejemplos de cómo usar animaciones Lottie e iconos en tu app
 */

@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("loading.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(100.dp)
    )
}

@Composable
fun ScanningAnimation(
    modifier: Modifier = Modifier
) {
    // Usando tu animación existente
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("logo_animation.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(150.dp)
    )
}

@Composable
fun IconExample() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Usando iconos Material Design
        Icon(
            painter = painterResource(id = android.R.drawable.ic_menu_search),
            contentDescription = "Buscar",
            modifier = Modifier.size(24.dp)
        )

        // Si descargas iconos personalizados, los usarías así:
        // Icon(
        //     painter = painterResource(id = R.drawable.inventory_icon),
        //     contentDescription = "Inventario"
        // )
    }
}

@Preview
@Composable
fun AnimationPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Animación de Carga")
        LoadingAnimation()

        Text("Animación de Escaneo")
        ScanningAnimation()

        Text("Iconos")
        IconExample()
    }
}
