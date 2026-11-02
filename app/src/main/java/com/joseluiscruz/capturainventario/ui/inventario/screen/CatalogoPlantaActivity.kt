package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CatalogoPlantaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme {
                CatalogoPlantaScreen(
                    onBack = { finish() }
                )
            }
        }
    }
}
