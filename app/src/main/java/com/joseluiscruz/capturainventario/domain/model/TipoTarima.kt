package com.joseluiscruz.capturainventario.domain.model

/**
 * Fuente única de los tipos de tarima que se pueden capturar.
 *
 * El modelo persiste `tipoTarima` como texto libre (MaterialItem / InventarioEscaneadoEntity),
 * así que la [etiqueta] es exactamente el valor que ya se guardaba: no hay migración de datos.
 *
 * Lista = unión de las dos listas que existían:
 * - diálogo Compose de captura: PLANTA DE MANUFACTURA, SAMS, IEQSA, CHEP, PLASTICO, OTRA
 * - diálogo legacy de la Activity: PLANTA DE MANUFACTURA, SAMS, IEQSA, CHEP (subconjunto de la anterior)
 */
enum class TipoTarima(val etiqueta: String) {
    DEMO_CORP("PLANTA DE MANUFACTURA"),
    SAMS("SAMS"),
    IEQSA("IEQSA"),
    CHEP("CHEP"),
    PLASTICO("PLASTICO"),
    OTRA("OTRA");

    companion object {
        /** Etiquetas en el orden en que se muestran en los diálogos. */
        val etiquetas: List<String> = entries.map { it.etiqueta }

        /** Busca por etiqueta ignorando mayúsculas/espacios; `null` si es un valor histórico no listado. */
        fun fromEtiqueta(valor: String?): TipoTarima? {
            val normalizado = valor?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.etiqueta == normalizado }
        }
    }
}
