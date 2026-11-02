package com.joseluiscruz.capturainventario.ui.opciones

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

@AndroidEntryPoint
class MasOpcionesActivity : ComponentActivity() {

    private val viewModel: InventarioSharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme(darkTheme = true) {
                MasOpcionesScreen(viewModel)
            }
        }
    }
}
