package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InventarioResumenComposeActivity : ComponentActivity() {
    @Inject lateinit var csvService: CsvInventarioService
    @Inject lateinit var repository: InventarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme(darkTheme = true) {
                InventarioResumenScreen(csvService, repository)
            }
        }
    }
}
