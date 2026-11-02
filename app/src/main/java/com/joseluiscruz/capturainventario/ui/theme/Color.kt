package com.joseluiscruz.capturainventario.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta corporativa PLANTA DE MANUFACTURA — única fuente de verdad para color.
 * BrandRed oficial: #E30613. Uso semántico (success / warning / error / sync)
 * para pantallas operativas; no usar rainbow por tile.
 */
object BrandColors {
    // Primarios
    val BrandRed = Color(0xFFE30613) // Rojo PLANTA DE MANUFACTURA oficial
    val BrandDarkRed = Color(0xFF9B0000) // Rojo profundo
    val BrandBlack = Color(0xFF1A1A1A) // Negro corporativo
    val BrandWhite = Color(0xFFFFFFFF)

    // Superficies (dark-first ops)
    val Surface0 = Color(0xFF0D0D0D) // Fondo principal
    val Surface1 = Color(0xFF1A1A1A) // Cards
    val Surface2 = Color(0xFF242424) // Cards elevadas
    val Surface3 = Color(0xFF2E2E2E) // Inputs

    // Acentos controlados (métricas / KPIs)
    val GoldAccent = Color(0xFFFFD700)

    // Semánticos operativos
    val SuccessGreen = Color(0xFF00C853)
    val WarningAmber = Color(0xFFFFAB00)
    val ErrorRed = Color(0xFFFF3D00)
    val SyncBlue = Color(0xFF2979FF) // Estado de sincronización

    // Texto
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
    val TextHint = Color(0xFF666666)

    // Contornos y selección del esquema oscuro (Material3 outline / outlineVariant / secondaryContainer).
    // Grises neutros de contraste medio: los bordes de campos, botones outlined, tarjetas y
    // divisores vuelven a distinguirse sobre Surface0/Surface1 (con TextHint/Surface3 quedaban casi
    // invisibles). Sin el tinte violeta de los valores por defecto de Material3.
    val OutlineDark = Color(0xFF8A8A8A)
    val OutlineVariantDark = Color(0xFF3D3D3D)

    // Contenedor de selección con acento de marca: BrandRed al 25 % sobre Surface1.
    val BrandRedContainer = Color(0xFF4C1518)
}

/**
 * Alias semánticos para pantallas ops — preferir estos sobre literales.
 */
object OpsSemanticColors {
    val Success = BrandColors.SuccessGreen
    val Warning = BrandColors.WarningAmber
    val Error = BrandColors.ErrorRed
    val Sync = BrandColors.SyncBlue
    val Primary = BrandColors.BrandRed
    val PrimaryDark = BrandColors.BrandDarkRed
}

// Aliases de marca alineados a BrandColors (compatibilidad con código legacy)
val BrandGreen = BrandColors.SuccessGreen
val BrandYellow = BrandColors.WarningAmber
val BrandOrange = BrandColors.WarningAmber
val BrandAccentRed = BrandColors.BrandRed // unificado a #E30613
val BrandNavy = BrandColors.Surface0
val BrandGray = BrandColors.TextSecondary
val BrandLightGray = BrandColors.TextHint

/**
 * Legacy Material template colors — NO usar en UI nueva.
 * Se mantienen como @Deprecated para no romper compilaciones residuales.
 */
@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.TextSecondary"))
val Purple80 = Color(0xFFB3B3B3)

@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.TextHint"))
val PurpleGrey80 = Color(0xFF666666)

@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.BrandRed"))
val Pink80 = Color(0xFFE30613)

@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.BrandRed"))
val Purple40 = Color(0xFFE30613)

@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.TextSecondary"))
val PurpleGrey40 = Color(0xFFB3B3B3)

@Deprecated("Usar BrandColors / OpsSemanticColors", ReplaceWith("BrandColors.BrandDarkRed"))
val Pink40 = Color(0xFF9B0000)
