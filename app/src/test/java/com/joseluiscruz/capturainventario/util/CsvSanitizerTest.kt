package com.joseluiscruz.capturainventario.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvSanitizerTest {

    @Test
    fun neutralizesSpreadsheetFormula() {
        assertEquals("'=SUM(A1:A2)", CsvSanitizer.escapeCell("=SUM(A1:A2)", true))
    }

    @Test
    fun preservesFormulaLikeTextForInternalStorage() {
        assertEquals("=ABC", CsvSanitizer.escapeCell("=ABC", false))
    }

    @Test
    fun quotesCommasAndEscapesQuotes() {
        assertEquals(
            "\"a,b \"\"c\"\"\"",
            CsvSanitizer.escapeCell("a,b \"c\"", false)
        )
    }
}
