package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.InterFont
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.BrandTextStyles
import com.joseluiscruz.capturainventario.ui.theme.provider

// ==================== COMPONENTES DE TEXTO PREMIUM ====================

/**
 * Hero Text - Título principal de máximo impacto
 * Usar solo en pantallas principales o métricas clave
 */
@Composable
fun HeroText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack,
    maxLines: Int = 1
) {
    Text(
        text = text,
        style = BrandTextStyles.HeroTitle.copy(color = color),
        modifier = modifier,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

/**
 * Section Title - Título de sección
 * Para headers de pantalla o divisiones principales
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack
) {
    Text(
        text = text,
        style = BrandTextStyles.SectionTitle.copy(color = color),
        modifier = modifier
    )
}

/**
 * Card Title - Título de tarjeta
 * Para títulos dentro de cards o elementos de lista
 */
@Composable
fun CardTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack
) {
    Text(
        text = text,
        style = BrandTextStyles.CardTitle.copy(color = color),
        modifier = modifier
    )
}

/**
 * KPI Number - Número grande para métricas
 * El elemento más importante de un dashboard
 */
@Composable
fun KpiNumber(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandRed
) {
    Text(
        text = value,
        style = BrandTextStyles.KpiNumber.copy(color = color),
        modifier = modifier
    )
}

/**
 * KPI Label - Etiqueta de métrica
 * Texto secundario debajo de un KPI
 */
@Composable
fun KpiLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.TextSecondary
) {
    Text(
        text = text,
        style = BrandTextStyles.KpiLabel.copy(color = color),
        modifier = modifier
    )
}

/**
 * KPI Completo - Número + Label juntos
 */
@Composable
fun KpiComplete(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = BrandColors.BrandRed,
    labelColor: Color = BrandColors.TextSecondary
) {
    Column(modifier = modifier) {
        KpiNumber(value = value, color = valueColor)
        Spacer(modifier = Modifier.height(4.dp))
        KpiLabel(text = label, color = labelColor)
    }
}

/**
 * Body Regular - Texto de cuerpo principal
 * Para contenido de lectura estándar
 */
@Composable
fun BodyRegular(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack
) {
    Text(
        text = text,
        style = BrandTextStyles.BodyRegular.copy(color = color),
        modifier = modifier
    )
}

/**
 * Body Secondary - Texto secundario
 * Para descripciones o información adicional
 */
@Composable
fun BodySecondary(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.TextSecondary
) {
    Text(
        text = text,
        style = BrandTextStyles.BodySecondary.copy(color = color),
        modifier = modifier
    )
}

/**
 * Caption - Texto pequeño de ayuda
 * Para hints, notas al pie o información contextual
 */
@Composable
fun Caption(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.TextHint,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = BrandTextStyles.Caption.copy(color = color, textAlign = textAlign),
        modifier = modifier
    )
}

/**
 * Overline - Texto sobre título
 * Estilo editorial, para categorías o tags
 */
@Composable
fun OverlineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandRed
) {
    Text(
        text = text,
        style = BrandTextStyles.Overline.copy(color = color),
        modifier = modifier
    )
}

/**
 * Badge Text - Texto para badges/etiquetas
 */
@Composable
fun BadgeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandWhite
) {
    Text(
        text = text,
        style = BrandTextStyles.Badge.copy(color = color),
        modifier = modifier
    )
}

/**
 * Price Text - Texto para precios/montos
 */
@Composable
fun PriceText(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack,
    currency: String? = null,
    currencyPosition: CurrencyPosition = CurrencyPosition.Prefix
) {
    Row(modifier = modifier, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        if (currencyPosition == CurrencyPosition.Prefix && currency != null) {
            Text(
                text = currency,
                style = BrandTextStyles.PriceAmount.copy(
                    fontSize = 20.sp,
                    color = color
                ),
                modifier = Modifier.padding(end = 2.dp)
            )
        }
        Text(
            text = value,
            style = BrandTextStyles.PriceAmount.copy(color = color),
            modifier = modifier
        )
        if (currencyPosition == CurrencyPosition.Suffix && currency != null) {
            Text(
                text = " $currency",
                style = BrandTextStyles.PriceAmount.copy(
                    fontSize = 20.sp,
                    color = color
                )
            )
        }
    }
}

enum class CurrencyPosition { Prefix, Suffix }

/**
 * Status Text - Texto de estado con color semántico
 */
@Composable
fun StatusText(
    text: String,
    status: StatusType,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        StatusType.Success -> BrandColors.SuccessGreen
        StatusType.Error -> BrandColors.ErrorRed
        StatusType.Warning -> BrandColors.WarningAmber
        StatusType.Info -> BrandColors.BrandBlack
    }

    Text(
        text = text,
        style = TextStyle(
            fontFamily = FontFamily(Font(InterFont, provider)),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.3.sp
        ).copy(color = color),
        modifier = modifier
    )
}

enum class StatusType { Success, Error, Warning, Info }

/**
 * Input Label - Etiqueta para campos de texto
 */
@Composable
fun InputLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack,
    required: Boolean = false
) {
    val displayText = if (required) "$text *" else text
    Text(
        text = displayText,
        style = BrandTextStyles.InputLabel.copy(color = color),
        modifier = modifier
    )
}

/**
 * Navigation Item Text - Texto para items de navegación
 */
@Composable
fun NavigationItemText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.BrandBlack,
    selected: Boolean = false
) {
    val textColor = if (selected) BrandColors.BrandRed else color
    Text(
        text = text,
        style = BrandTextStyles.NavigationItem.copy(color = textColor),
        modifier = modifier
    )
}

/**
 * Empty State - Texto para estados vacíos
 */
@Composable
fun EmptyStateText(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = BrandTextStyles.SectionTitle.copy(
                color = BrandColors.TextSecondary,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            BodySecondary(
                text = description,
                color = BrandColors.TextHint,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }
    }
}

/**
 * Error State - Texto para estados de error
 */
@Composable
fun ErrorStateText(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = BrandTextStyles.SectionTitle.copy(
                color = BrandColors.ErrorRed,
                fontWeight = FontWeight.Normal
            ),
            textAlign = TextAlign.Center
        )
        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            BodySecondary(
                text = description,
                color = BrandColors.TextHint
            )
        }
    }
}

/**
 * Loading Text - Texto para estados de carga
 */
@Composable
fun LoadingText(
    text: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = BrandTextStyles.BodySecondary.copy(
            color = BrandColors.TextSecondary
        ),
        modifier = modifier
    )
}
