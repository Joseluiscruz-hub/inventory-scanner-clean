package com.joseluiscruz.capturainventario.util

import com.joseluiscruz.capturainventario.domain.model.ScanMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

class ScanFeedbackTest {

    @Test
    fun `ZXing con contenido nulo es cancelacion`() {
        assertEquals(ScannerOutcome.Cancelled(ScannerEngine.ZXING), ScannerOutcome.fromZxing(null))
    }

    @Test
    fun `ZXing con contenido es lectura`() {
        assertEquals(
            ScannerOutcome.Read("SKU:123", ScannerEngine.ZXING),
            ScannerOutcome.fromZxing("SKU:123")
        )
    }

    @Test
    fun `ML Kit con rawValue nulo se trata como lectura vacia y no se ignora`() {
        assertEquals(ScannerOutcome.Read("", ScannerEngine.ML_KIT), ScannerOutcome.fromMlKit(null))
    }

    @Test
    fun `fallo con alternativa es advertencia y sin alternativa es error`() {
        val conFallback = ScannerOutcome.Failed(ScannerEngine.ML_KIT, RuntimeException(), willFallback = true)
        val sinFallback = ScannerOutcome.Failed(ScannerEngine.ZXING, RuntimeException(), willFallback = false)
        assertEquals(ScanFeedbackKind.WARNING, ScanFeedback.kindFor(conFallback))
        assertEquals(ScanFeedbackKind.ERROR, ScanFeedback.kindFor(sinFallback))
    }

    @Test
    fun `lectura y cancelacion no suenan hasta interpretar`() {
        assertEquals(ScanFeedbackKind.NONE, ScanFeedback.kindFor(ScannerOutcome.Read("x", ScannerEngine.ML_KIT)))
        assertEquals(ScanFeedbackKind.NONE, ScanFeedback.kindFor(ScannerOutcome.Cancelled(ScannerEngine.ML_KIT)))
    }

    @Test
    fun `interpretacion correcta es exito e incorrecta es error`() {
        assertEquals(ScanFeedbackKind.SUCCESS, ScanFeedback.kindForParse(parsedOk = true))
        assertEquals(ScanFeedbackKind.ERROR, ScanFeedback.kindForParse(parsedOk = false))
    }

    @Test
    fun `play delega al tono y vibracion correctos`() {
        val manager: FeedbackManager = mock()
        val feedback = ScanFeedback(manager)

        feedback.play(ScanFeedbackKind.SUCCESS)
        verify(manager).playSuccess()
        feedback.play(ScanFeedbackKind.ERROR)
        verify(manager).playError()
        feedback.play(ScanFeedbackKind.WARNING)
        verify(manager).playWarning()
    }

    @Test
    fun `play NONE no produce feedback`() {
        val manager: FeedbackManager = mock()
        ScanFeedback(manager).play(ScanFeedbackKind.NONE)
        verifyNoInteractions(manager)
    }

    @Test
    fun `PALLET siempre relanza, MANUAL nunca, RUMBA segun el toggle`() {
        assertTrue(ContinuousScanPolicy.debeRelanzar(ScanMode.PALLET, escaneoContinuo = false))
        assertTrue(ContinuousScanPolicy.debeRelanzar(ScanMode.PALLET, escaneoContinuo = true))
        assertFalse(ContinuousScanPolicy.debeRelanzar(ScanMode.MANUAL, escaneoContinuo = true))
        assertFalse(ContinuousScanPolicy.debeRelanzar(ScanMode.RUMBA, escaneoContinuo = false))
        assertTrue(ContinuousScanPolicy.debeRelanzar(ScanMode.RUMBA, escaneoContinuo = true))
    }

    @Test
    fun `el toggle solo aplica en modos donde cambia el comportamiento`() {
        assertFalse(ContinuousScanPolicy.toggleAplica(ScanMode.PALLET))
        assertFalse(ContinuousScanPolicy.toggleAplica(ScanMode.MANUAL))
        assertTrue(ContinuousScanPolicy.toggleAplica(ScanMode.RUMBA))
    }
}
