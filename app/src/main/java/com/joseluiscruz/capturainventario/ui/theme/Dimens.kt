package com.joseluiscruz.capturainventario.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado y radio — uso compartido en pantallas ops.
 * Evitar literales sueltos (8.dp / 16.dp) cuando el token exista.
 */
object BrandSpacing {
    val xxxs: Dp = 2.dp
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 24.dp
    val xl: Dp = 32.dp
    val xxl: Dp = 48.dp
    val xxxl: Dp = 64.dp

    /** Padding horizontal estándar de pantallas */
    val screenHorizontal: Dp = md

    /** Gap entre cards en grids 2-col */
    val cardGap: Dp = sm

    /** Separación entre secciones del home */
    val sectionGap: Dp = lg
}

object BrandRadius {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val pill: Dp = 999.dp

    /** Cards operativas */
    val card: Dp = md

    /** Botones CTA principales */
    val button: Dp = md

    /** Chips de estado / sync */
    val chip: Dp = pill
}

object BrandElevation {
    val none: Dp = 0.dp
    val card: Dp = 2.dp
    val raised: Dp = 4.dp
    val modal: Dp = 8.dp
}

object BrandSizes {
    /** Altura mínima CTA primario táctil (ops / guantes) */
    val ctaMinHeight: Dp = 56.dp

    /** KPI chip / strip item */
    val kpiMinHeight: Dp = 72.dp

    /** Icono en cards de acción */
    val actionIcon: Dp = 28.dp

    /** Status bar / top app bar content */
    val topBarHeight: Dp = 56.dp
}
