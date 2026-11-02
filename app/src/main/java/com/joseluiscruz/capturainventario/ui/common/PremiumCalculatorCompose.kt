package com.joseluiscruz.capturainventario.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun PremiumCalculatorCompose(onClose: () -> Unit) {
    var displayText by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var operator by remember { mutableStateOf<String?>(null) }
    var shouldResetDisplay by remember { mutableStateOf(false) }

    val onNumberClick = { num: String ->
        if (displayText == "0" || shouldResetDisplay) {
            displayText = num
            shouldResetDisplay = false
        } else {
            displayText += num
        }
    }

    val onOperatorClick = { op: String ->
        operand1 = displayText.toDoubleOrNull()
        operator = op
        shouldResetDisplay = true
    }

    val onCalculate = {
        val val1 = operand1
        val val2 = displayText.toDoubleOrNull()
        if (val1 != null && val2 != null && operator != null) {
            val result = when (operator) {
                "+" -> val1 + val2
                "-" -> val1 - val2
                "*" -> val1 * val2
                "/" -> if (val2 != 0.0) val1 / val2 else 0.0
                else -> val2
            }
            displayText = if (result % 1.0 == 0.0) result.toInt().toString() else String.format("%.2f", result)
            operand1 = null
            operator = null
            shouldResetDisplay = true
        }
    }

    val onQuickMultiplier = { multiplier: Int ->
        val current = displayText.toDoubleOrNull() ?: 0.0
        val result = current * multiplier
        displayText = if (result % 1.0 == 0.0) result.toInt().toString() else String.format("%.2f", result)
        shouldResetDisplay = true
    }

    Card(
        modifier = Modifier
            .width(260.dp)
            .wrapContentHeight()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header con Drag Handle y Cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.LightGray, CircleShape)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    "PREMIUM CALC",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandColors.BrandRed,
                    letterSpacing = 1.sp
                )
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = BrandColors.BrandRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Display "Digital"
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = BrandColors.BrandBlack
            ) {
                Box(contentAlignment = Alignment.CenterEnd, modifier = Modifier.padding(horizontal = 12.dp)) {
                    Text(
                        text = displayText,
                        color = BrandColors.GoldAccent,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Multiplicadores Innovadores (Atajos de Estiba)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickButton("x24", Modifier.weight(1f)) { onQuickMultiplier(24) }
                QuickButton("x40", Modifier.weight(1f)) { onQuickMultiplier(40) }
                QuickButton("x60", Modifier.weight(1f)) { onQuickMultiplier(60) }
                QuickButton("x80", Modifier.weight(1f)) { onQuickMultiplier(80) }
            }

            Spacer(Modifier.height(12.dp))

            // Grid de Botones
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("7", Modifier.weight(1f)) { onNumberClick("7") }
                    CalcButton("8", Modifier.weight(1f)) { onNumberClick("8") }
                    CalcButton("9", Modifier.weight(1f)) { onNumberClick("9") }
                    CalcButton("/", Modifier.weight(1f), BrandColors.BrandRed, Color.White) { onOperatorClick("/") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("4", Modifier.weight(1f)) { onNumberClick("4") }
                    CalcButton("5", Modifier.weight(1f)) { onNumberClick("5") }
                    CalcButton("6", Modifier.weight(1f)) { onNumberClick("6") }
                    CalcButton("*", Modifier.weight(1f), BrandColors.BrandRed, Color.White) { onOperatorClick("*") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("1", Modifier.weight(1f)) { onNumberClick("1") }
                    CalcButton("2", Modifier.weight(1f)) { onNumberClick("2") }
                    CalcButton("3", Modifier.weight(1f)) { onNumberClick("3") }
                    CalcButton("-", Modifier.weight(1f), BrandColors.BrandRed, Color.White) { onOperatorClick("-") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalcButton("C", Modifier.weight(1f), Color(0xFFEEEEEE), BrandColors.BrandRed) {
                        displayText = "0"
                        operand1 = null
                        operator = null
                    }
                    CalcButton("0", Modifier.weight(1f)) { onNumberClick("0") }
                    CalcButton("=", Modifier.weight(1f), BrandColors.GoldAccent, BrandColors.BrandBlack) { onCalculate() }
                    CalcButton("+", Modifier.weight(1f), BrandColors.BrandRed, Color.White) { onOperatorClick("+") }
                }
            }
        }
    }
}

@Composable
fun QuickButton(text: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(32.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = BrandColors.BrandBlack,
        elevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier,
    backgroundColor: Color = Color(0xFFF5F5F5),
    contentColor: Color = BrandColors.BrandBlack,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .aspectRatio(1.2f)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        elevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
