package com.joseluiscruz.capturainventario

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.viewmodel.TraspasoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraspasoActivity : AppCompatActivity() {

    private val viewModel: TraspasoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!com.joseluiscruz.capturainventario.manager.AuthManager.isAdmin()) {
            android.widget.Toast.makeText(this, "Acceso restringido a administradores", android.widget.Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val operador = com.joseluiscruz.capturainventario.manager.AuthManager.currentUser.value?.username ?: "Administrador"
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

        viewModel.cargarAlmacenes()

        setContent {
            PremiumTheme {
                TraspasoScreen(
                    viewModel = viewModel,
                    operador = operador,
                    deviceId = deviceId,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}
