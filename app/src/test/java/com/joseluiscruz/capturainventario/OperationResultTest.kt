package com.joseluiscruz.capturainventario

import com.joseluiscruz.capturainventario.domain.model.ErrorCode
import com.joseluiscruz.capturainventario.domain.model.OperationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests unitarios para OperationResult<T>.
 *
 * Valida la correcta semántica del sealed class:
 * - Success, Error, Loading
 * - Helpers: getOrNull, getOrThrow, map, runCatching
 * - ErrorCode.fromException
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OperationResultTest {

    @Test
    fun `Success contiene datos correctamente`() {
        val result: OperationResult<String> = OperationResult.Success("datos")
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertEquals("datos", result.getOrNull())
    }

    @Test
    fun `Error contiene mensaje y código`() {
        val result: OperationResult<String> = OperationResult.Error(
            "SKU vacío",
            ErrorCode.VALIDATION_ERROR
        )
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
        assertNull(result.getOrNull())

        val error = result as OperationResult.Error
        assertEquals("SKU vacío", error.message)
        assertEquals(ErrorCode.VALIDATION_ERROR, error.code)
    }

    @Test
    fun `Loading es estado intermedio`() {
        val result: OperationResult<String> = OperationResult.Loading
        assertTrue(result.isLoading)
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
    }

    @Test
    fun `getOrThrow lanza excepción en Error`() {
        val result: OperationResult<String> = OperationResult.Error("falló", ErrorCode.UNKNOWN)
        assertThrows(IllegalStateException::class.java) {
            result.getOrThrow()
        }
    }

    @Test
    fun `getOrThrow retorna valor en Success`() {
        val result: OperationResult<Int> = OperationResult.Success(42)
        assertEquals(42, result.getOrThrow())
    }

    @Test
    fun `map transforma Success correctamente`() {
        val result: OperationResult<Int> = OperationResult.Success(10)
        val mapped = result.map { it * 2 }
        assertEquals(20, mapped.getOrNull())
    }

    @Test
    fun `map preserva Error`() {
        val result: OperationResult<Int> = OperationResult.Error("error", ErrorCode.DATABASE_ERROR)
        val mapped = result.map { it * 2 }
        assertTrue(mapped.isError)
        assertEquals("error", (mapped as OperationResult.Error).message)
    }

    @Test
    fun `runCatching captura excepción y retorna Error`() = runTest {
        val result = OperationResult.runCatching<String> {
            throw java.io.IOException("disco lleno")
        }
        assertTrue(result.isError)
        val error = result as OperationResult.Error
        assertEquals(ErrorCode.FILE_READ_ERROR, error.code)
        assertTrue(error.message.contains("disco lleno"))
    }

    @Test
    fun `runCatching retorna Success en ejecución normal`() = runTest {
        val result = OperationResult.runCatching { "éxito" }
        assertTrue(result.isSuccess)
        assertEquals("éxito", result.getOrNull())
    }

    @Test
    fun `ErrorCode fromException mapea excepciones correctamente`() {
        assertEquals(ErrorCode.FILE_READ_ERROR, ErrorCode.fromException(java.io.IOException()))
        assertEquals(ErrorCode.NETWORK_ERROR, ErrorCode.fromException(java.net.UnknownHostException()))
        assertEquals(ErrorCode.VALIDATION_ERROR, ErrorCode.fromException(IllegalArgumentException()))
        assertEquals(ErrorCode.PERMISSION_DENIED, ErrorCode.fromException(SecurityException()))
        assertEquals(ErrorCode.UNKNOWN, ErrorCode.fromException(RuntimeException()))
    }
}
