package com.joseluiscruz.capturainventario.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class AjusteInventarioPendiente(
    val material: MaterialItem,
    val cantidadCorrecta: Int,
    val motivo: String
) {
    val cantidadActual: Int = material.totalPallets?.toIntOrNull() ?: 0
    val diferencia: Int = cantidadCorrecta - cantidadActual
}

data class AjusteInventarioUiState(
    val materiales: List<MaterialItem> = emptyList(),
    val ajustesPendientes: List<AjusteInventarioPendiente> = emptyList(),
    val searchQuery: String = "",
    val materialEnEdicion: MaterialItem? = null,
    val showAuthDialog: Boolean = false,
    val isApplyingAjuste: Boolean = false,
    val authError: String? = null,
    val operadorActual: String = "Operador Desconocido",
    val isLoading: Boolean = false
)

@HiltViewModel
class AjusteInventarioViewModel @Inject constructor(
    private val csvService: CsvInventarioService,
    private val inventoryRepository: InventarioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AjusteInventarioUiState())
    val uiState: StateFlow<AjusteInventarioUiState> = _uiState.asStateFlow()

    init {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        _uiState.update { it.copy(
            operadorActual = prefs.getString("usuario", "Operador Desconocido") ?: "Operador Desconocido"
        ) }
        cargarMateriales()
    }

    fun cargarMateriales() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val materiales = inventoryRepository.obtenerTodosLosEscaneados()
                    .map(::entityToMaterial)
                    .sortedWith(
                        compareBy<MaterialItem> { it.sku }
                            .thenBy { it.tipoTarima.orEmpty() }
                            .thenBy { it.almacen.orEmpty() }
                            .thenBy { it.ubicacion.orEmpty() }
                    )
                _uiState.update { it.copy(materiales = materiales, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        materiales = emptyList(),
                        isLoading = false,
                        authError = e.message ?: "No se pudo cargar el inventario"
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onEditarMaterial(material: MaterialItem?) {
        _uiState.update { it.copy(materialEnEdicion = material) }
    }

    fun onGuardarAjusteBorrador(cantidadCorrecta: Int, motivo: String) {
        val material = _uiState.value.materialEnEdicion ?: return
        val currentPendientes = _uiState.value.ajustesPendientes.toMutableList()
        currentPendientes.removeAll { sameRegistro(it.material, material) }
        
        currentPendientes.add(
            AjusteInventarioPendiente(
                material = material,
                cantidadCorrecta = cantidadCorrecta,
                motivo = motivo
            )
        )
        _uiState.update { it.copy(
            ajustesPendientes = currentPendientes,
            materialEnEdicion = null
        ) }
    }

    fun onRemoverAjusteBorrador(pendiente: AjusteInventarioPendiente) {
        val currentPendientes = _uiState.value.ajustesPendientes.toMutableList()
        currentPendientes.remove(pendiente)
        _uiState.update { it.copy(ajustesPendientes = currentPendientes) }
    }

    fun limpiarBorradores() {
        _uiState.update { it.copy(ajustesPendientes = emptyList()) }
    }

    fun onMostrarAuthDialog(show: Boolean) {
        _uiState.update { it.copy(showAuthDialog = show, authError = null) }
    }

    fun setAuthError(error: String?) {
        _uiState.update { it.copy(authError = error) }
    }

    fun autorizarYAplicarAjuste(callback: (Boolean, String) -> Unit) {
        val ajustes = _uiState.value.ajustesPendientes.toList()
        if (ajustes.isEmpty()) {
            setAuthError("No hay ajustes pendientes")
            return
        }

        _uiState.update {
            it.copy(isApplyingAjuste = true, showAuthDialog = false, authError = null)
        }

        viewModelScope.launch {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser == null) {
                _uiState.update { it.copy(isApplyingAjuste = false) }
                callback(false, "La sesión expiró. Inicia sesión nuevamente")
                return@launch
            }

            val role = try {
                (firebaseUser.getIdToken(true).await().claims["role"] as? String)?.uppercase()
            } catch (_: Exception) {
                null
            }

            if (role != "ADMIN") {
                _uiState.update {
                    it.copy(
                        isApplyingAjuste = false,
                        authError = "La cuenta no tiene autorización ADMIN"
                    )
                }
                callback(false, "La cuenta no tiene autorización ADMIN")
                return@launch
            }

            val autorizadoPor = firebaseUser.displayName
                ?: firebaseUser.email
                ?: firebaseUser.uid

            var fallos = 0
            var exito = 0
            var ultimoError = ""

            val pendientesReales = ajustes.filter { it.diferencia != 0 }
            if (pendientesReales.isEmpty()) {
                _uiState.update { it.copy(isApplyingAjuste = false) }
                callback(false, "No hay diferencias para aplicar")
                return@launch
            }

            try {
                inventoryRepository.registrarAjustes(
                    pendientesReales.map { ajuste ->
                        InventarioRepository.AjusteInput(
                            codigo = ajuste.material.sku,
                            descripcion = ajuste.material.descripcion,
                            cantidadDelta = ajuste.diferencia,
                            metadata = InventarioRepository.ScanMetadata(
                                fechaEtiqueta = ajuste.material.fProd,
                                almacen = ajuste.material.almacen,
                                ubicacion = ajuste.material.ubicacion,
                                tipoTarima = ajuste.material.tipoTarima
                            )
                        )
                    }
                )
                exito = pendientesReales.size
            } catch (e: Exception) {
                fallos = pendientesReales.size
                ultimoError = e.message ?: "Error al registrar los ajustes"
            }

            if (fallos > 0) {
                _uiState.update { it.copy(isApplyingAjuste = false) }
                callback(false, "Aplicados $exito, fallaron $fallos. Último error: $ultimoError")
            } else {
                val snapshot = inventoryRepository.obtenerTodosLosEscaneados()
                    .map(::entityToMaterial)
                val mirrorResult = csvService.reemplazarInventario(snapshot)
                val mirrorWarning =
                    (mirrorResult as? com.joseluiscruz.capturainventario.domain.model.OperationResult.Error)
                        ?.message

                generarArchivoDeAjuste(
                    context,
                    _uiState.value.operadorActual,
                    autorizadoPor,
                    pendientesReales
                ) { success, msg ->
                    _uiState.update {
                        it.copy(
                            isApplyingAjuste = false,
                            ajustesPendientes = emptyList()
                        )
                    }
                    cargarMateriales()
                    callback(
                        success,
                        if (success && mirrorWarning == null) {
                            "Ajuste aplicado y auditado por $autorizadoPor"
                        } else if (success) {
                            "Ajuste aplicado; copia CSV pendiente: $mirrorWarning"
                        } else {
                            "Ajuste aplicado. $msg"
                        }
                    )
                }
            }
        }
    }

    private fun generarArchivoDeAjuste(
        context: Context,
        operador: String,
        autorizadoPor: String,
        ajustes: List<AjusteInventarioPendiente>,
        callback: (Boolean, String) -> Unit
    ) {
        try {
            val documentsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: File(context.filesDir, "reports").apply { mkdirs() }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val archivo = File(documentsDir, "ajuste_inventario_$timestamp.csv")

            val contenido = buildString {
                appendLine("FECHA_HORA;OPERADOR;AUTORIZADO_POR;SKU;DESCRIPCION;ALMACEN;TIPO_TARIMA;CANTIDAD_ACTUAL;CANTIDAD_CORRECTA;DIFERENCIA;MOTIVO")
                val fechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                ajustes.forEach { ajuste ->
                    appendLine(
                        listOf(
                            fechaHora,
                            operador,
                            limpiarCsv(autorizadoPor),
                            ajuste.material.sku,
                            limpiarCsv("${ajuste.material.descripcion.ifBlank { "Ajuste de inventario" }} | Motivo: ${ajuste.motivo}"),
                            limpiarCsv(ajuste.material.almacen.orEmpty()),
                            limpiarCsv(ajuste.material.tipoTarima.orEmpty()),
                            ajuste.cantidadActual,
                            ajuste.cantidadCorrecta,
                            ajuste.diferencia,
                            limpiarCsv(ajuste.motivo)
                        ).joinToString(";")
                    )
                }
            }

            archivo.writeText(contenido, Charsets.UTF_8)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                archivo
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Ajuste inventario - $timestamp")
                putExtra(Intent.EXTRA_TEXT, "Ajuste generado por $operador. Requiere revisión y aprobación del área correspondiente.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Compartir ajuste de inventario").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            callback(true, "Archivo de ajuste listo para compartir")
        } catch (e: Exception) {
            callback(false, "Error al generar ajuste: ${e.message}")
        }
    }

    private fun limpiarCsv(valor: String): String {
        val limpio = valor
            .replace(";", ",")
            .replace("\n", " ")
            .replace("\r", " ")
            .trim()
        return if (limpio.firstOrNull() in setOf('=', '+', '-', '@')) "'$limpio" else limpio
    }

    private fun entityToMaterial(entity: InventarioEscaneadoEntity): MaterialItem {
        return MaterialItem(
            sku = entity.codigo,
            descripcion = entity.descripcion,
            cxPal = "",
            fpc = "",
            con = "",
            centro = "",
            linea = "",
            op = "",
            fProd = entity.fechaEtiqueta.orEmpty(),
            diasV = "",
            ubicacion = entity.ubicacion.orEmpty(),
            totalPallets = entity.cantidadTotal.toString(),
            restos = entity.cantidadRestos.toString(),
            tipoTarima = entity.tipoTarima.orEmpty(),
            almacen = entity.almacen.orEmpty()
        )
    }

    fun sameRegistro(a: MaterialItem, b: MaterialItem): Boolean {
        return a.sku == b.sku &&
            a.tipoTarima == b.tipoTarima &&
            a.almacen == b.almacen &&
            a.ubicacion == b.ubicacion &&
            a.op == b.op &&
            a.fProd == b.fProd &&
            a.diasV == b.diasV
    }
}
