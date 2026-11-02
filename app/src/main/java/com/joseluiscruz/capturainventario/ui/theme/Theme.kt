package com.joseluiscruz.capturainventario.ui.theme

import androidx.compose.runtime.Composable

/**
 * Alias legacy → PremiumTheme (dark-first ops).
 * Con la migración a PremiumTheme, AppTheme delega al tema unificado.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    PremiumTheme(darkTheme = true, content = content)
}
