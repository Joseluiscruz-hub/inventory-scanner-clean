package com.joseluiscruz.capturainventario.util

import com.joseluiscruz.capturainventario.domain.model.ScanMode

/** Motor que produjo la lectura. */
enum class ScannerEngine { ML_KIT, ZXING }

/**
 * Resultado normalizado de cualquier motor de escaneo (ML Kit / Google code scanner o ZXing).
 * Ambos motores pasan por aquí para que el feedback y el flujo sean idénticos.
 */
sealed interface ScannerOutcome {
    /** Se leyó un código (puede venir vacío; la interpretación decide si es válido). */
    data class Read(val raw: String, val engine: ScannerEngine) : ScannerOutcome

    /** El operador cerró el escáner sin leer nada. */
    data class Cancelled(val engine: ScannerEngine) : ScannerOutcome

    /**
     * El escáner no pudo abrirse o falló.
     * @param willFallback `true` si se intentará automáticamente con el otro motor.
     */
    data class Failed(
        val engine: ScannerEngine,
        val cause: Throwable?,
        val willFallback: Boolean
    ) : ScannerOutcome

    companion object {
        /** ZXing: `contents == null` significa cancelado. */
        fun fromZxing(contents: String?): ScannerOutcome =
            if (contents == null) Cancelled(ScannerEngine.ZXING) else Read(contents, ScannerEngine.ZXING)

        /** ML Kit: un `rawValue` nulo se trata como lectura vacía (código ilegible), no se ignora. */
        fun fromMlKit(rawValue: String?): ScannerOutcome = Read(rawValue.orEmpty(), ScannerEngine.ML_KIT)
    }
}

/** Tipo de feedback (háptico + tono) a reproducir. */
enum class ScanFeedbackKind { SUCCESS, WARNING, ERROR, NONE }

/**
 * Utilidad compartida de feedback de escaneo para ZXing y ML Kit.
 *
 * - Lectura interpretada correctamente → [ScanFeedbackKind.SUCCESS] (vibración corta + beep agudo).
 * - Lectura inválida o escáner sin alternativa → [ScanFeedbackKind.ERROR] (vibración doble + tono grave).
 * - Fallo con alternativa automática → [ScanFeedbackKind.WARNING].
 * - Cancelado → sin feedback (acción deliberada del operador).
 */
class ScanFeedback(private val feedbackManager: FeedbackManager) {

    fun play(kind: ScanFeedbackKind) {
        when (kind) {
            ScanFeedbackKind.SUCCESS -> feedbackManager.playSuccess()
            ScanFeedbackKind.WARNING -> feedbackManager.playWarning()
            ScanFeedbackKind.ERROR -> feedbackManager.playError()
            ScanFeedbackKind.NONE -> Unit
        }
    }

    companion object {
        /** Feedback inmediato según el resultado del motor (antes de interpretar el contenido). */
        fun kindFor(outcome: ScannerOutcome): ScanFeedbackKind = when (outcome) {
            // Una lectura se confirma tras interpretarla (ver kindForParse).
            is ScannerOutcome.Read -> ScanFeedbackKind.NONE
            is ScannerOutcome.Cancelled -> ScanFeedbackKind.NONE
            is ScannerOutcome.Failed ->
                if (outcome.willFallback) ScanFeedbackKind.WARNING else ScanFeedbackKind.ERROR
        }

        /** Feedback tras interpretar el contenido del QR. */
        fun kindForParse(parsedOk: Boolean): ScanFeedbackKind =
            if (parsedOk) ScanFeedbackKind.SUCCESS else ScanFeedbackKind.ERROR
    }
}

/**
 * Decide si el escáner se relanza solo tras un guardado exitoso.
 *
 * - PALLET: siempre (comportamiento existente: escaneo → guardado automático → siguiente).
 * - RUMBA: solo con "escaneo continuo" activado (escanea, captura pallets, GUARDAR, siguiente).
 * - MANUAL: nunca (la captura es por teclado).
 */
object ContinuousScanPolicy {
    fun debeRelanzar(modo: ScanMode, escaneoContinuo: Boolean): Boolean = when (modo) {
        ScanMode.PALLET -> true
        ScanMode.MANUAL -> false
        else -> escaneoContinuo
    }

    /** El toggle solo tiene efecto en modos donde puede cambiar el comportamiento. */
    fun toggleAplica(modo: ScanMode): Boolean = modo != ScanMode.PALLET && modo != ScanMode.MANUAL
}
