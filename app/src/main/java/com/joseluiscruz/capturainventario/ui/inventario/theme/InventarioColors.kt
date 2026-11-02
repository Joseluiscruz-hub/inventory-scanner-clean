package com.joseluiscruz.capturainventario.ui.inventario.theme

import androidx.compose.ui.graphics.Color
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

/**
 * Paleta de inventario alineada a BrandColors (PLANTA DE MANUFACTURA #E30613).
 * Dark-first para captura operativa.
 */
object InventarioColors {
    val primary = BrandColors.BrandRed
    val primaryVariant = BrandColors.BrandDarkRed
    val onPrimary = BrandColors.BrandWhite
    val primaryContainer = BrandColors.BrandDarkRed
    val onPrimaryContainer = BrandColors.BrandWhite

    val secondary = BrandColors.TextSecondary
    val secondaryVariant = BrandColors.TextHint
    val onSecondary = BrandColors.BrandWhite
    val secondaryContainer = BrandColors.Surface2
    val onSecondaryContainer = BrandColors.TextPrimary

    val tertiary = BrandColors.WarningAmber
    val onTertiary = BrandColors.BrandBlack
    val tertiaryContainer = BrandColors.Surface2
    val onTertiaryContainer = BrandColors.WarningAmber

    val background = BrandColors.Surface0
    val onBackground = BrandColors.TextPrimary
    val surface = BrandColors.Surface1
    val onSurface = BrandColors.TextPrimary
    val surfaceVariant = BrandColors.Surface2
    val onSurfaceVariant = BrandColors.TextSecondary

    val success = BrandColors.SuccessGreen
    val onSuccess = BrandColors.BrandBlack
    val successContainer = BrandColors.Surface2
    val onSuccessContainer = BrandColors.SuccessGreen

    val error = BrandColors.ErrorRed
    val onError = BrandColors.BrandWhite
    val errorContainer = BrandColors.Surface2
    val onErrorContainer = BrandColors.ErrorRed

    val warning = BrandColors.WarningAmber
    val onWarning = BrandColors.BrandBlack
    val warningContainer = BrandColors.Surface2
    val onWarningContainer = BrandColors.WarningAmber

    val divider = BrandColors.Surface3
    val textPrimary = BrandColors.TextPrimary
    val textSecondary = BrandColors.TextSecondary
    val textDisabled = BrandColors.TextHint

    val cardBackground = BrandColors.Surface1
    val cardStroke = BrandColors.Surface3

    val progressTrack = BrandColors.Surface3

    object Dark {
        val primary = BrandColors.BrandRed
        val primaryVariant = BrandColors.BrandDarkRed
        val onPrimary = BrandColors.BrandWhite
        val primaryContainer = BrandColors.BrandDarkRed
        val onPrimaryContainer = BrandColors.BrandWhite

        val secondary = BrandColors.TextSecondary
        val onSecondary = BrandColors.BrandWhite
        val secondaryContainer = BrandColors.Surface2
        val onSecondaryContainer = BrandColors.TextPrimary

        val background = BrandColors.Surface0
        val onBackground = BrandColors.TextPrimary
        val surface = BrandColors.Surface1
        val onSurface = BrandColors.TextPrimary
        val surfaceVariant = BrandColors.Surface2
        val onSurfaceVariant = BrandColors.TextSecondary

        val cardBackground = BrandColors.Surface1
        val cardStroke = BrandColors.Surface3
        val divider = BrandColors.Surface3
        val textPrimary = BrandColors.TextPrimary
        val textSecondary = BrandColors.TextSecondary
    }
}

fun Boolean.toButtonColor(
    primary: Color = InventarioColors.primary,
    disabled: Color = InventarioColors.secondary
): Color = if (this) primary else disabled

fun Boolean.toAlpha(): Float = if (this) 1.0f else 0.6f
