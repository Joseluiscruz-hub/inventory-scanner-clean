package com.joseluiscruz.capturainventario.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val InterFont = GoogleFont("Inter")
val BebasFont = GoogleFont("Bebas Neue")

// ==================== TIPOGRAFÍA CORPORATIVA ESTRICTA ====================
/**
 * Jerarquía tipográfica premium inspirada en guías de diseño de Apple y Material Design 3
 * 
 * Reglas estrictas:
 * - Headers: Bebas Neue (Bold/Black) - Solo para títulos principales y métricas grandes
 * - Cuerpo: Inter (Regular/Medium) - Para texto corrido y descripciones
 * - Acentos: Inter (Light) - Para texto secundario y hints
 * 
 * Contraste deliberado entre pesos extremos para aspecto de "revista de diseño"
 */

// ==================== DISPLAY - Métricas impactantes y títulos principales ====================
val BrandTypography = Typography(
    // Display para métricas gigantes (Dashboard KPIs)
    displayLarge = TextStyle(
        fontFamily = FontFamily(Font(BebasFont, provider)),
        fontWeight = FontWeight.Black,
        fontSize = 64.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    
    // Display mediano para títulos de sección
    displayMedium = TextStyle(
        fontFamily = FontFamily(Font(BebasFont, provider)),
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),
    
    // Display pequeño para subtítulos importantes
    displaySmall = TextStyle(
        fontFamily = FontFamily(Font(BebasFont, provider)),
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.5.sp
    ),
    
    // ==================== HEADLINE - Títulos de pantalla y secciones ====================
    headlineLarge = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = -0.5.sp
    ),
    
    headlineMedium = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = -0.25.sp
    ),
    
    headlineSmall = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    
    // ==================== TITLE - Títulos de cards y elementos ====================
    titleLarge = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    
    titleMedium = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    
    titleSmall = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    // ==================== BODY - Texto corrido principal ====================
    bodyLarge = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    
    bodyMedium = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    bodySmall = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    
    // ==================== LABEL - Botones, etiquetas y badges ====================
    labelLarge = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    
    labelMedium = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    
    labelSmall = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// ==================== ESTILOS DE TEXTO PREMIUM ====================

/**
 * Estilos predefinidos para casos de uso específicos
 * Mantener consistencia en toda la app
 */
object BrandTextStyles {
    
    // Títulos principales - Máximo impacto visual
    val HeroTitle = TextStyle(
        fontFamily = FontFamily(Font(BebasFont, provider)),
        fontWeight = FontWeight.Black,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.sp,
        color = BrandColors.BrandBlack
    )
    
    // Títulos de sección - Bold y claro
    val SectionTitle = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = -0.5.sp,
        color = BrandColors.BrandBlack
    )
    
    // Títulos de tarjeta - Semi-bold
    val CardTitle = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = BrandColors.BrandBlack
    )
    
    // Métricas KPI - Números grandes e impactantes
    val KpiNumber = TextStyle(
        fontFamily = FontFamily(Font(BebasFont, provider)),
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        lineHeight = 56.sp,
        letterSpacing = 0.sp,
        color = BrandColors.BrandRed
    )
    
    // Etiquetas de KPI - Texto secundario pequeño
    val KpiLabel = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.sp,
        color = BrandColors.TextSecondary
    )
    
    // Cuerpo principal - Lectura cómoda
    val BodyRegular = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.BrandBlack
    )
    
    // Cuerpo secundario - Información adicional
    val BodySecondary = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
        color = BrandColors.TextSecondary
    )
    
    // Caption - Texto de ayuda pequeño
    val Caption = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Light,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = BrandColors.TextHint
    )
    
    // Overline - Texto encima de títulos (estilo editorial)
    val Overline = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp,
        color = BrandColors.BrandRed
    )
    
    // Botón primario - Texto de botones principales
    val ButtonPrimary = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        color = BrandColors.BrandWhite
    )
    
    // Botón secundario - Texto de botones outlined
    val ButtonSecondary = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
        color = BrandColors.BrandRed
    )
    
    // Badge - Etiquetas pequeñas
    val Badge = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = BrandColors.BrandWhite
    )
    
    // Input label - Etiquetas de campos de texto
    val InputLabel = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.BrandBlack
    )
    
    // Input placeholder - Placeholder de campos
    val InputPlaceholder = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.TextHint
    )
    
    // Navegación - Items de menú
    val NavigationItem = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.BrandBlack
    )
    
    // Precio/Monto - Números importantes
    val PriceAmount = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = -0.5.sp,
        color = BrandColors.BrandBlack
    )
    
    // Status success - Texto de estado exitoso
    val StatusSuccess = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.SuccessGreen
    )
    
    // Status error - Texto de estado de error
    val StatusError = TextStyle(
        fontFamily = FontFamily(Font(InterFont, provider)),
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp,
        color = BrandColors.ErrorRed
    )
}

// ==================== EXTENSIONES PARA TEXT ====================

/**
 * Extensiones para aplicar estilos rápidamente
 */
fun TextStyle.hero(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(BebasFont, provider)),
    fontWeight = FontWeight.Black,
    fontSize = 72.sp
)

fun TextStyle.kpi(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(BebasFont, provider)),
    fontWeight = FontWeight.Black,
    fontSize = 56.sp,
    color = BrandColors.BrandRed
)

fun TextStyle.sectionTitle(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(InterFont, provider)),
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp
)

fun TextStyle.cardTitle(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(InterFont, provider)),
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp
)

fun TextStyle.bodySecondary(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(InterFont, provider)),
    fontWeight = FontWeight.Light,
    fontSize = 13.sp,
    color = BrandColors.TextSecondary
)

fun TextStyle.caption(): TextStyle = this.copy(
    fontFamily = FontFamily(Font(InterFont, provider)),
    fontWeight = FontWeight.Light,
    fontSize = 11.sp,
    color = BrandColors.TextHint
)

// Legacy Typography (kept for backward compatibility)
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
