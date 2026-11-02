package com.joseluiscruz.capturainventario.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- COLORES CORPORATIVOS (PLANTA DE MANUFACTURA) — alineados a BrandColors.BrandRed #E30613 ---
private val BrandPrimaryRed = BrandColors.BrandRed
private val BrandPrimaryDarkRed = BrandColors.BrandDarkRed
private val BackgroundLight = Color(0xFFF8F9FA)
private val SurfaceLight = Color(0xFFFFFFFF)
private val TextPrimaryLight = Color(0xFF1D1D1B)
private val TextSecondaryLight = Color(0xFF4A4A4A)

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryDarkRed,
    onPrimaryContainer = Color.White,
    secondary = TextPrimaryLight,
    onSecondary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = TextSecondaryLight,
    secondaryContainer = Color(0xFFEEEEEE),
    onSecondaryContainer = TextPrimaryLight,
    outline = BrandColors.TextHint,
    outlineVariant = Color(0xFFE0E0E0),
    error = BrandColors.ErrorRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryRed,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryDarkRed,
    onPrimaryContainer = Color.White,
    secondary = BrandColors.TextSecondary,
    onSecondary = Color.White,
    background = BrandColors.Surface0,
    onBackground = BrandColors.TextPrimary,
    surface = BrandColors.Surface1,
    onSurface = BrandColors.TextPrimary,
    surfaceVariant = BrandColors.Surface2,
    onSurfaceVariant = BrandColors.TextSecondary,
    // Selección (chips/filtros/indicadores) con acento de marca en vez de gris plano.
    secondaryContainer = BrandColors.BrandRedContainer,
    onSecondaryContainer = BrandColors.TextPrimary,
    // Bordes y divisores con contraste medio (antes TextHint #666666 / Surface3 #2E2E2E).
    outline = BrandColors.OutlineDark,
    outlineVariant = BrandColors.OutlineVariantDark,
    error = BrandColors.ErrorRed,
    onError = Color.White
)

/** Formas corporativas derivadas de los tokens de [BrandRadius]. */
private val CorporateShapes = Shapes(
    extraSmall = RoundedCornerShape(BrandRadius.xs),
    small = RoundedCornerShape(BrandRadius.sm),
    medium = RoundedCornerShape(BrandRadius.md),
    large = RoundedCornerShape(BrandRadius.lg),
    extraLarge = RoundedCornerShape(BrandRadius.xl)
)

@Immutable
data class PremiumColors(
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val error: Color,
    val onPrimary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onError: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val sync: Color,
    val cardBackground: Color
)

val LightPremiumColors = PremiumColors(
    primary = BrandPrimaryRed,
    primaryVariant = BrandPrimaryDarkRed,
    secondary = TextPrimaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    error = BrandColors.ErrorRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onError = Color.White,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    divider = Color(0xFFEEEEEE),
    success = BrandColors.SuccessGreen,
    warning = BrandColors.WarningAmber,
    sync = BrandColors.SyncBlue,
    cardBackground = SurfaceLight
)

val DarkPremiumColors = PremiumColors(
    primary = BrandPrimaryRed,
    primaryVariant = BrandPrimaryDarkRed,
    secondary = BrandColors.TextSecondary,
    background = BrandColors.Surface0,
    surface = BrandColors.Surface1,
    error = BrandColors.ErrorRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = BrandColors.TextPrimary,
    onSurface = BrandColors.TextPrimary,
    onError = Color.White,
    textPrimary = BrandColors.TextPrimary,
    textSecondary = BrandColors.TextSecondary,
    divider = BrandColors.Surface3,
    success = BrandColors.SuccessGreen,
    warning = BrandColors.WarningAmber,
    sync = BrandColors.SyncBlue,
    cardBackground = BrandColors.Surface1
)

val LocalPremiumColors = staticCompositionLocalOf { DarkPremiumColors }

object PremiumTheme {
    val colors: PremiumColors
        @Composable
        get() = LocalPremiumColors.current
}

/**
 * Tema corporativo PLANTA DE MANUFACTURA.
 * Default darkTheme=true para pantallas operativas (almacén / planta).
 * Usar darkTheme=false solo en flujos ligeros (login claro si se requiere).
 */
@Composable
fun PremiumTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val premiumColors = if (darkTheme) DarkPremiumColors else LightPremiumColors
    val materialColors = if (darkTheme) DarkColorScheme else LightColorScheme
    val statusBarColor = if (darkTheme) BrandColors.Surface0 else BrandPrimaryRed

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            var context = view.context
            var activity: Activity? = null
            while (context is android.content.ContextWrapper) {
                if (context is Activity) {
                    activity = context
                    break
                }
                context = context.baseContext
            }

            val window = activity?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = statusBarColor.toArgb()
                @Suppress("DEPRECATION")
                window.navigationBarColor = materialColors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    CompositionLocalProvider(LocalPremiumColors provides premiumColors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = BrandTypography,
            shapes = CorporateShapes,
            content = content
        )
    }
}

/**
 * Tema oscuro corporativo para pantallas de operación intensiva.
 * Reduce fatiga visual en almacenes con luz artificial.
 */
@Composable
fun DarkPremiumTheme(
    content: @Composable () -> Unit
) {
    PremiumTheme(darkTheme = true, content = content)
}

/** Compatibilidad con nombre de tema legacy */
@Composable
fun EscaneoDeMaterialesDemoTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    PremiumTheme(darkTheme = darkTheme, content = content)
}
