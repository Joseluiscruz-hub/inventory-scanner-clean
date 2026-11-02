package com.joseluiscruz.capturainventario.domain.model

/**
 * Resultado sellado para operaciones del dominio.
 *
 * Reemplaza los try/catch dispersos + callbacks (Boolean, String) por un
 * flujo tipado que obliga al consumidor a manejar tanto éxito como error.
 *
 * Uso:
 * ```
 * when (val result = repository.guardarEscaneo(entity)) {
 *     is OperationResult.Success -> showSnackbar("Guardado: ${result.data}")
 *     is OperationResult.Error   -> showError(result.message, result.code)
 *     is OperationResult.Loading -> showSpinner()
 * }
 * ```
 */
sealed class OperationResult<out T> {
    data class Success<T>(val data: T) : OperationResult<T>()
    data class Error(
        val message: String,
        val code: ErrorCode = ErrorCode.UNKNOWN,
        val cause: Throwable? = null
    ) : OperationResult<Nothing>()
    data object Loading : OperationResult<Nothing>()

    /** Helpers para uso conciso */
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    /** Extrae el valor o null si no es Success */
    fun getOrNull(): T? = (this as? Success)?.data

    /** Extrae el valor o lanza excepción si no es Success */
    fun getOrThrow(): T = (this as? Success)?.data
        ?: throw IllegalStateException("OperationResult is not Success: $this")

    /** Transforma el valor si es Success, preserva Error/Loading */
    fun <R> map(transform: (T) -> R): OperationResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> this
    }

    companion object {
        /** Ejecuta un bloque y envuelve el resultado en Success o Error */
        inline fun <T> runCatching(block: () -> T): OperationResult<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Error(
                    message = e.message ?: "Error desconocido",
                    code = ErrorCode.fromException(e),
                    cause = e
                )
            }
        }
    }
}

/**
 * Códigos de error específicos del dominio.
 * Permiten al UI mostrar mensajes contextuales y tomar acciones correctivas.
 */
enum class ErrorCode {
    // --- Datos ---
    VALIDATION_ERROR,        // SKU vacío, cantidad negativa, etc.
    NOT_FOUND,               // Registro no encontrado en BD/CSV
    DUPLICATE,               // SKU duplicado donde no se espera

    // --- I/O ---
    FILE_READ_ERROR,         // Error al leer CSV/archivo local
    FILE_WRITE_ERROR,        // Error al escribir CSV/archivo local
    DATABASE_ERROR,          // Error de Room

    // --- Red ---
    NETWORK_ERROR,           // Sin conexión o timeout
    FIREBASE_ERROR,          // Error de Firebase (Auth/Firestore)
    SAP_EXPORT_ERROR,        // Error al generar archivo SAP 701/702

    // --- Sistema ---
    PERMISSION_DENIED,       // Permisos de almacenamiento/cámara denegados
    SCANNER_ERROR,           // Error del lector de códigos

    // --- General ---
    UNKNOWN;                 // Error no categorizado

    companion object {
        fun fromException(e: Exception): ErrorCode = when (e) {
            is java.io.FileNotFoundException -> NOT_FOUND
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException -> NETWORK_ERROR
            is java.io.IOException -> FILE_READ_ERROR
            is android.database.sqlite.SQLiteException -> DATABASE_ERROR
            is SecurityException -> PERMISSION_DENIED
            is IllegalArgumentException -> VALIDATION_ERROR
            else -> UNKNOWN
        }
    }
}
