package com.joseluiscruz.capturainventario.ui.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

class DashboardComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme(darkTheme = true) {
                DashboardScreen()
            }
        }
    }
}
