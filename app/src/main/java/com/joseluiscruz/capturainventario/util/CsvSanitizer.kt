package com.joseluiscruz.capturainventario.util

/**
 * Codifica celdas CSV y, cuando el archivo se abrirá en una hoja de cálculo,
 * neutraliza valores que podrían interpretarse como fórmulas.
 */
object CsvSanitizer {
    fun escapeCell(value: String, neutralizeFormula: Boolean): String {
        val protectedValue =
            if (neutralizeFormula &&
                value.trimStart().firstOrNull() in setOf('=', '+', '-', '@')
            ) {
                "'$value"
            } else {
                value
            }

        return if (protectedValue.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + protectedValue.replace("\"", "\"\"") + "\""
        } else {
            protectedValue
        }
    }
}
