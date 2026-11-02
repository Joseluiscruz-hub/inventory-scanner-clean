package com.joseluiscruz.capturainventario.ui.splash

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import com.joseluiscruz.capturainventario.ui.login.LoginActivity
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private var navigationStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            PremiumTheme(darkTheme = true) {
                InventoryCodeSplash(onFinished = ::navigateToLogin)
            }
        }
    }

    private fun navigateToLogin() {
        if (navigationStarted || isFinishing || isDestroyed) return
        navigationStarted = true

        val intent = Intent(this, LoginActivity::class.java)
        val options = ActivityOptions.makeCustomAnimation(
            this,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
        startActivity(intent, options.toBundle())
        finish()
    }
}
