package com.joseluiscruz.capturainventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.components.LiquidProgress
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

@Composable
fun TimeFilterSection() {
    var selectedFilter by remember { mutableStateOf("Hoy") }
    val filters = listOf("Hoy", "Semana", "Mes")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        filters.forEach { filter ->
            val isSelected = selectedFilter == filter
            Surface(
                color = if (isSelected) PremiumTheme.colors.surface else PremiumTheme.colors.surface.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { selectedFilter = filter }
            ) {
                Text(
                    text = filter,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = if (isSelected) PremiumTheme.colors.primary else PremiumTheme.colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun KpiSection(totalPallets: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LiquidProgress(
            progress = 0.985f,
            label = "Exactitud",
            color = BrandColors.SuccessGreen
        )
        LiquidProgress(
            progress = 0.76f,
            label = "Ocupacion",
            color = BrandColors.BrandRed
        )
        LiquidProgress(
            progress = 0.92f,
            label = "SLA",
            color = BrandColors.SyncBlue
        )
    }
}

@Composable
fun KpiCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = PremiumTheme.colors.cardBackground
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PremiumTheme.colors.textPrimary
                )
                Text(title, fontSize = 11.sp, color = PremiumTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun ChartCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(320.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = PremiumTheme.colors.cardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = PremiumTheme.colors.textSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PremiumTheme.colors.textPrimary)
            }
            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
