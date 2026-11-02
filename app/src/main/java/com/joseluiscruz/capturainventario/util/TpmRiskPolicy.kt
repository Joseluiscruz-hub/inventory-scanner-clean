package com.joseluiscruz.capturainventario.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class TpmRiskLevel {
    SIN_FECHA,
    CUMPLE,
    RIESGO_ALTO
}

data class TpmRiskEvaluation(
    val level: TpmRiskLevel,
    val diasRestantes: Int? = null,
    val fechaCaducidadEpochDay: Long? = null,
    val thresholdDays: Int = 40
) {
    val etiqueta: String
        get() = when (level) {
            TpmRiskLevel.SIN_FECHA -> "TPM sin fecha"
            TpmRiskLevel.CUMPLE -> "TPM OK"
            TpmRiskLevel.RIESGO_ALTO -> "TPM < ${thresholdDays}d"
        }
}

object TpmRiskPolicy {
    const val DEFAULT_THRESHOLD_DAYS = 40

    fun evaluar(
        fechaCaducidadEpochDay: Long?,
        thresholdDays: Int = DEFAULT_THRESHOLD_DAYS,
        nowMillis: Long = System.currentTimeMillis()
    ): TpmRiskEvaluation {
        if (fechaCaducidadEpochDay == null) {
            return TpmRiskEvaluation(level = TpmRiskLevel.SIN_FECHA, thresholdDays = thresholdDays)
        }

        val hoyEpochDay = TimeUnit.MILLISECONDS.toDays(nowMillis)
        val diasRestantes = (fechaCaducidadEpochDay - hoyEpochDay).toInt()
        val level = if (diasRestantes < thresholdDays) TpmRiskLevel.RIESGO_ALTO else TpmRiskLevel.CUMPLE

        return TpmRiskEvaluation(
            level = level,
            diasRestantes = diasRestantes,
            fechaCaducidadEpochDay = fechaCaducidadEpochDay,
            thresholdDays = thresholdDays
        )
    }

    fun calcularFechaCaducidadEpochDay(fechaEtiqueta: String?, diasV: String?): Long? {
        if (fechaEtiqueta.isNullOrBlank()) return null

        val formatos = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "ddMMyyyy", "ddMMyy")
        val fechaBase = formatos.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
                    .parse(fechaEtiqueta.trim())
            }.getOrNull()
        } ?: return null

        val calendar = Calendar.getInstance().apply { time = fechaBase }
        val dias = diasV?.trim()?.toIntOrNull()
        if (dias != null) {
            calendar.add(Calendar.DAY_OF_YEAR, dias)
        }

        return TimeUnit.MILLISECONDS.toDays(calendar.timeInMillis)
    }
}


