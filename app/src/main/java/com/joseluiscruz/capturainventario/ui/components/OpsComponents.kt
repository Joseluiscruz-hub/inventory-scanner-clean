package com.joseluiscruz.capturainventario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.BrandRadius
import com.joseluiscruz.capturainventario.ui.theme.BrandSizes
import com.joseluiscruz.capturainventario.ui.theme.BrandSpacing

/**
 * Componentes ops compartidos — jerarquía calmada PLANTA DE MANUFACTURA.
 */

enum class OpsStatusKind { Success, Warning, Error, Sync, Neutral }

@Composable
fun OpsSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .background(BrandColors.BrandRed, RoundedCornerShape(BrandRadius.xs))
        )
        Spacer(Modifier.width(BrandSpacing.xs))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
fun StatusBadge(
    label: String,
    kind: OpsStatusKind,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (kind) {
        OpsStatusKind.Success -> BrandColors.SuccessGreen.copy(alpha = 0.15f) to BrandColors.SuccessGreen
        OpsStatusKind.Warning -> BrandColors.WarningAmber.copy(alpha = 0.15f) to BrandColors.WarningAmber
        OpsStatusKind.Error -> BrandColors.ErrorRed.copy(alpha = 0.15f) to BrandColors.ErrorRed
        OpsStatusKind.Sync -> BrandColors.SyncBlue.copy(alpha = 0.15f) to BrandColors.SyncBlue
        OpsStatusKind.Neutral -> BrandColors.Surface3 to BrandColors.TextSecondary
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(BrandRadius.chip),
        color = bg
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = BrandSpacing.sm, vertical = BrandSpacing.xxs),
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun KpiChip(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = BrandColors.BrandRed
) {
    Surface(
        modifier = modifier
            .heightIn(min = BrandSizes.kpiMinHeight)
            .fillMaxWidth(),
        shape = RoundedCornerShape(BrandRadius.card),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .padding(BrandSpacing.sm)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                color = accent,
                fontWeight = FontWeight.Black,
                fontSize = 26.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(BrandSpacing.xxs))
            Text(
                text = label.uppercase(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PrimaryCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BrandSizes.ctaMinHeight),
        shape = RoundedCornerShape(BrandRadius.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandColors.BrandRed,
            contentColor = Color.White,
            disabledContainerColor = BrandColors.BrandRed.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 2.dp),
        contentPadding = PaddingValues(horizontal = BrandSpacing.lg, vertical = BrandSpacing.md)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = Color.White)
            Spacer(Modifier.width(BrandSpacing.sm))
        }
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun OpsActionCard(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Etiqueta opcional (p. ej. "ADMIN") para operaciones restringidas. */
    badge: String? = null
) {
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    Surface(
        onClick = onClick,
        modifier = modifier.heightIn(min = 120.dp),
        shape = RoundedCornerShape(BrandRadius.card),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(BrandSpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        BrandColors.BrandRed.copy(alpha = 0.08f),
                        RoundedCornerShape(BrandRadius.sm)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = BrandColors.BrandRed.copy(alpha = 0.9f),
                    modifier = Modifier.size(BrandSizes.actionIcon)
                )
            }
            Spacer(Modifier.height(BrandSpacing.xs))
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (badge != null) {
                Spacer(Modifier.height(BrandSpacing.xxs))
                StatusBadge(label = badge, kind = OpsStatusKind.Neutral)
            }
        }
    }
}

@Composable
fun OpsEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(BrandSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(BrandSpacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(BrandSpacing.md))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = BrandColors.BrandRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
