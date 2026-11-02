package com.joseluiscruz.capturainventario.ui.dashboard

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import android.graphics.Color as AndroidColor

@Composable
fun PieChartComponent(labelsPipe: String, valuesPipe: String) {
    val labels = labelsPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    val values = valuesPipe.split("|").mapNotNull { it.trim().toIntOrNull() }
    val data = labels.zip(values).toMap()
    val isDark = true
    val textColor = if (isDark) AndroidColor.WHITE else AndroidColor.BLACK
    val holeColor = if (isDark) AndroidColor.parseColor("#1E1E1E") else AndroidColor.WHITE

    AndroidView(
        factory = { context ->
            PieChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = true
                legend.textColor = textColor
                legend.textSize = 10f
                setEntryLabelColor(textColor)
                setEntryLabelTextSize(10f)
                setUsePercentValues(true)
                holeRadius = 45f
                transparentCircleRadius = 50f
                setHoleColor(holeColor)
                animateY(1400)
            }
        },
        update = { chart ->
            val entries = data.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "").apply {
                colors = listOf(
                    AndroidColor.parseColor("#E30613"),
                    AndroidColor.parseColor("#FF9800"),
                    AndroidColor.parseColor("#FFC107"),
                    AndroidColor.parseColor("#4CAF50"),
                    AndroidColor.parseColor("#2196F3")
                )
                valueTextColor = AndroidColor.WHITE
                valueTextSize = 12f
                valueFormatter = PercentFormatter(chart)
            }

            val pieData = PieData(dataSet)
            chart.data = pieData
            chart.legend.textColor = textColor
            chart.setHoleColor(holeColor)
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BarChartComponent(labelsPipe: String, valuesPipe: String) {
    val labels = labelsPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    val values = valuesPipe.split("|").mapNotNull { it.trim().toIntOrNull() }
    val data = labels.zip(values)
    val isDark = true
    val textColor = if (isDark) AndroidColor.WHITE else AndroidColor.BLACK
    val gridColor = if (isDark) AndroidColor.parseColor("#404040") else AndroidColor.LTGRAY

    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                description.isEnabled = false
                legend.isEnabled = false

                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setDrawGridLines(false)
                xAxis.granularity = 1f
                xAxis.textColor = textColor

                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = gridColor
                axisLeft.textColor = textColor
                axisRight.isEnabled = false

                animateY(1400)
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, pair ->
                BarEntry(index.toFloat(), pair.second.toFloat())
            }

            val dataSet = BarDataSet(entries, "Stock").apply {
                color = AndroidColor.parseColor("#E30613")
                valueTextColor = textColor
                valueTextSize = 10f
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.6f

            chart.xAxis.valueFormatter = IndexAxisValueFormatter(data.map { it.first })
            chart.xAxis.textColor = textColor
            chart.axisLeft.textColor = textColor
            chart.axisLeft.gridColor = gridColor
            chart.data = barData
            chart.invalidate()
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AlertsSection(alertasPipe: String) {
    val alertas = alertasPipe.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = PremiumTheme.colors.cardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = PremiumTheme.colors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Atencion Requerida",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PremiumTheme.colors.textPrimary
                )
            }
            Spacer(Modifier.height(12.dp))

            if (alertas.isEmpty()) {
                Text(
                    "No hay alertas criticas. Operacion normal.",
                    fontSize = 13.sp,
                    color = PremiumTheme.colors.success,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                alertas.forEach { alerta ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .background(PremiumTheme.colors.primary, CircleShape)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(alerta, fontSize = 13.sp, color = PremiumTheme.colors.textSecondary)
                    }
                    if (alerta != alertas.last()) Divider(color = PremiumTheme.colors.divider)
                }
            }
        }
    }
}
