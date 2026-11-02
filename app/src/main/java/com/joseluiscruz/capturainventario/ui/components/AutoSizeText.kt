package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Texto de una línea que reduce su tamaño (de [maxFontSize] a [minFontSize], en pasos de 1sp)
 * hasta caber en el ancho disponible; si ni con [minFontSize] cabe, recorta con "…".
 *
 * Compose 1.7 no trae autoajuste nativo (`BasicText(autoSize)` llega en 1.8). No se dibuja
 * hasta encontrar el tamaño final, así que no hay parpadeo al medir.
 */
@Composable
fun AutoSizeText(
    text: String,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified
) {
    // Se vuelve a medir si cambia el texto o el ancho disponible (p. ej. al rotar).
    BoxWithConstraints(modifier = modifier) {
        val anchoMax = maxWidth
        var fontSize by remember(text, maxFontSize, minFontSize, anchoMax) { mutableStateOf(maxFontSize) }
        var listo by remember(text, maxFontSize, minFontSize, anchoMax) { mutableStateOf(false) }
        Text(
            text = text,
            modifier = Modifier.drawWithContent { if (listo) drawContent() },
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight,
            letterSpacing = letterSpacing,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = LocalTextStyle.current,
            onTextLayout = { resultado ->
                val siguiente = fontSize.value - 1f
                if (resultado.noCabe() && siguiente >= minFontSize.value) {
                    fontSize = siguiente.sp
                } else {
                    listo = true
                }
            }
        )
    }
}

private fun TextLayoutResult.noCabe(): Boolean =
    hasVisualOverflow || (lineCount > 0 && isLineEllipsized(lineCount - 1))
