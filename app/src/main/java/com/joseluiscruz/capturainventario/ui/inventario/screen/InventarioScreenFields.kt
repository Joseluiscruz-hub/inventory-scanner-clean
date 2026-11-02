package com.joseluiscruz.capturainventario.ui.inventario.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    // Mapeo de emojis/palabras clave a Material Icons
    val leadingIcon: @Composable (() -> Unit)? = when {
        label.contains("SKU", ignoreCase = true) -> {
            { Icon(Icons.Default.QrCode, null, tint = PremiumTheme.colors.primary, modifier = Modifier.size(20.dp)) }
        }
        label.contains("Ubicación", ignoreCase = true) || label.contains("Posición", ignoreCase = true) -> {
            { Icon(Icons.Default.Place, null, tint = PremiumTheme.colors.primary, modifier = Modifier.size(20.dp)) }
        }
        label.contains("Pallet", ignoreCase = true) || label.contains("Pieza", ignoreCase = true) -> {
            { Icon(Icons.Default.Inventory2, null, tint = PremiumTheme.colors.primary, modifier = Modifier.size(20.dp)) }
        }
        label.contains("Resto", ignoreCase = true) -> {
            { Icon(Icons.Default.BarChart, null, tint = PremiumTheme.colors.primary, modifier = Modifier.size(20.dp)) }
        }
        else -> null
    }

    // Limpiar emojis del label para mostrar texto limpio
    val cleanLabel = label.replace(Regex("[🏷️📍📦📊]"), "").trim()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(cleanLabel, fontSize = 12.sp) },
        leadingIcon = leadingIcon,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PremiumTheme.colors.primary,
            unfocusedBorderColor = PremiumTheme.colors.divider,
            disabledBorderColor = PremiumTheme.colors.divider.copy(alpha = 0.5f),
            disabledLabelColor = PremiumTheme.colors.textSecondary.copy(alpha = 0.5f),
            focusedTextColor = PremiumTheme.colors.textPrimary,
            unfocusedTextColor = PremiumTheme.colors.textPrimary,
            focusedContainerColor = PremiumTheme.colors.primary.copy(alpha = 0.03f),
            unfocusedContainerColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
fun PremiumDateField(
    value: String,
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val cleanLabel = label.replace(Regex("[🏷️📍📦📊📅]"), "").trim()

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(cleanLabel, fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = PremiumTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = PremiumTheme.colors.textSecondary
                )
            },
            enabled = false,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = if (enabled) PremiumTheme.colors.divider else PremiumTheme.colors.divider.copy(alpha = 0.5f),
                disabledLabelColor = PremiumTheme.colors.textSecondary.copy(alpha = if (enabled) 1f else 0.5f),
                disabledTextColor = PremiumTheme.colors.textPrimary.copy(alpha = if (enabled) 1f else 0.5f),
                disabledLeadingIconColor = PremiumTheme.colors.primary.copy(alpha = if (enabled) 1f else 0.5f),
                disabledTrailingIconColor = PremiumTheme.colors.textSecondary.copy(alpha = if (enabled) 1f else 0.5f),
                disabledContainerColor = Color.Transparent
            ),
            singleLine = true
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { onClick() }
        )
    }
}
