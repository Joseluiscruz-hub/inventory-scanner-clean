package com.joseluiscruz.capturainventario.data

import android.content.Context
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.domain.validation.TraspasoValidator
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import com.google.firebase.functions.FirebaseFunctions
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val firebaseDisponible: Boolean
        get() = FirebaseApp.getApps(context).isNotEmpty()
    private val db: FirebaseFirestore?
        get() = if (firebaseDisponible) FirebaseFirestore.getInstance() else null
    private val auth: FirebaseAuth?
        get() = if (firebaseDisponible) FirebaseAuth.getInstance() else null
    private val functions: FirebaseFunctions?
        get() = if (firebaseDisponible) {
            FirebaseFunctions.getInstance("us-central1")
        } else {
            null
        }

    /**
     * Registers a scan/adjust movement via Cloud Functions.
     * Always sends **deltas** + a stable [operationId] (retries must reuse it).
     */
    fun subirEscaneo(
        material: MaterialItem,
        operador: String,
        deviceId: String,
        callback: (Boolean, String) -> Unit,
        operationId: String? = null
    ) {
        val palletsDelta = material.totalPallets?.toIntOrNull() ?: 0
        val restosDelta = material.restos?.toIntOrNull() ?: 0
        registrarMovimientoDelta(
            operationId = operationId?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString(),
            sku = material.sku,
            descripcion = material.descripcion,
            almacen = material.almacen?.takeIf { it.isNotBlank() } ?: "Desconocido",
            tipoTarima = material.tipoTarima?.takeIf { it.isNotBlank() } ?: "N/A",
            palletsDelta = palletsDelta,
            restosDelta = restosDelta,
            deviceId = deviceId,
            tipoMovimiento = "SCAN",
            operador = operador,
            callback = callback
        )
    }

    /**
     * Core callable wrapper used by live uploads and the offline queue.
     * [operationId] is required and must be stable across retries.
     */
    fun registrarMovimientoDelta(
        operationId: String,
        sku: String,
        descripcion: String,
        almacen: String,
        tipoTarima: String,
        palletsDelta: Int,
        restosDelta: Int,
        deviceId: String,
        tipoMovimiento: String = "SCAN",
        operador: String = "",
        callback: (Boolean, String) -> Unit
    ) {
        val functionsClient = functions
        val firebaseAuth = auth
        if (functionsClient == null || firebaseAuth == null) {
            callback(false, FIREBASE_NOT_CONFIGURED_MESSAGE)
            return
        }
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            callback(false, "La sesión expiró. Inicia sesión nuevamente")
            return
        }
        if (operationId.isBlank()) {
            callback(false, "operationId es obligatorio para idempotencia")
            return
        }
        if (palletsDelta == 0 && restosDelta == 0) {
            callback(false, "Cantidad inválida: se requiere delta de pallets/restos distinto de 0")
            return
        }

        fun send(plantaClaim: String?) {
            val payload = hashMapOf<String, Any>(
                "sku" to sku,
                "descripcion" to descripcion,
                "almacen" to almacen,
                "tipoTarima" to tipoTarima,
                "palletsDelta" to palletsDelta,
                "restosDelta" to restosDelta,
                "deviceId" to deviceId,
                "operationId" to operationId,
                "tipoMovimiento" to tipoMovimiento,
                "operador" to (
                    firebaseUser.displayName
                        ?: firebaseUser.email
                        ?: operador.ifBlank { firebaseUser.uid }
                    )
            )
            if (!plantaClaim.isNullOrBlank()) {
                payload["planta"] = plantaClaim
            } else {
                resolveLocalPlanta()?.let { payload["planta"] = it }
            }
            functionsClient
                .getHttpsCallable("registerInventoryMovement")
                .call(payload)
                .addOnSuccessListener {
                    callback(true, "Movimiento registrado ($operationId)")
                }
                .addOnFailureListener { e ->
                    callback(false, "Error al registrar movimiento: ${e.message}")
                }
        }

        firebaseUser.getIdToken(false)
            .addOnSuccessListener { token ->
                val planta = (token.claims["planta"] as? String)?.trim()
                send(planta)
            }
            .addOnFailureListener {
                send(resolveLocalPlanta())
            }
    }

    fun descargarInventario(callback: (List<MaterialItem>?, String) -> Unit) {
        val firestore = db
        val firebaseAuth = auth
        if (firestore == null || firebaseAuth == null) {
            callback(null, FIREBASE_NOT_CONFIGURED_MESSAGE)
            return
        }
        val user = firebaseAuth.currentUser
        if (user == null) {
            callback(null, "La sesión expiró. Inicia sesión nuevamente")
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { tokenResult ->
                val role = (tokenResult.claims["role"] as? String)?.uppercase()
                val planta = (tokenResult.claims["planta"] as? String)?.trim().orEmpty()
                val isAdmin = role == "ADMIN"
                descargarPaginaInventario(
                    firestore = firestore,
                    plantaFilter = if (isAdmin && planta.isEmpty()) null else planta.ifEmpty { null },
                    requirePlanta = !isAdmin,
                    ultimoDocumento = null,
                    acumulado = mutableListOf(),
                    callback = callback
                )
            }
            .addOnFailureListener { e ->
                callback(null, "No se pudo leer claims: ${e.message}")
            }
    }

    private fun descargarPaginaInventario(
        firestore: FirebaseFirestore,
        plantaFilter: String?,
        requirePlanta: Boolean,
        ultimoDocumento: DocumentSnapshot?,
        acumulado: MutableList<MaterialItem>,
        callback: (List<MaterialItem>?, String) -> Unit
    ) {
        if (requirePlanta && plantaFilter.isNullOrBlank()) {
            callback(
                null,
                "El operador no tiene claim 'planta'. Asígnalo con scripts/firebase/set-user-role.mjs"
            )
            return
        }
        var query = if (!plantaFilter.isNullOrBlank()) {
            firestore.collection(INVENTARIOS_COLLECTION)
                .whereEqualTo("planta", plantaFilter)
                .orderBy(FieldPath.documentId())
                .limit(INVENTORY_PAGE_SIZE)
        } else {
            firestore.collection(INVENTARIOS_COLLECTION)
                .orderBy(FieldPath.documentId())
                .limit(INVENTORY_PAGE_SIZE)
        }
        if (ultimoDocumento != null) {
            query = query.startAfter(ultimoDocumento)
        }
        query.get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    try {
                        val item = MaterialItem(
                            sku = document.getString("sku") ?: "",
                            descripcion = document.getString("descripcion") ?: "",
                            cxPal = "",
                            fpc = "",
                            con = "",
                            centro = document.getString("centro") ?: "",
                            linea = document.getString("linea") ?: "",
                            op = document.getString("op") ?: "",
                            fProd = document.getString("fProd") ?: "",
                            diasV = "",
                            ubicacion = document.getString("ubicacion"),
                            totalPallets = document.getLong("pallets")?.toString() ?: "0",
                            restos = document.getLong("restos")?.toString() ?: "0",
                            tipoTarima = document.getString("tipoTarima"),
                            almacen = document.getString("almacen")
                        )
                        acumulado.add(item)
                    } catch (e: Exception) {
                        android.util.Log.w(
                            "FirebaseRepository",
                            "Inventario corrupto ignorado: ${document.id}",
                            e
                        )
                    }
                }
                if (result.size() < INVENTORY_PAGE_SIZE) {
                    callback(
                        acumulado.toList(),
                        "Descarga exitosa: ${acumulado.size} registros"
                    )
                } else {
                    descargarPaginaInventario(
                        firestore = firestore,
                        plantaFilter = plantaFilter,
                        requirePlanta = requirePlanta,
                        ultimoDocumento = result.documents.last(),
                        acumulado = acumulado,
                        callback = callback
                    )
                }
            }
            .addOnFailureListener { exception ->
                callback(null, "Error al descargar: ${exception.message}")
            }
    }

    fun subirInventarioReferencia(
        items: List<InventarioTiempoRealManager.ItemReferencia>,
        callback: (Boolean, String) -> Unit
    ) {
        FirebaseReferenciaRepositoryHelper.subir(context, items, callback)
    }

    fun descargarInventarioReferencia(
        callback: (List<InventarioTiempoRealManager.ItemReferencia>?, String) -> Unit
    ) {
        FirebaseReferenciaRepositoryHelper.descargar(context, callback)
    }

    fun ejecutarTraspaso(
        sku: String,
        tipoTarima: String,
        almacenOrigen: String,
        almacenDestino: String,
        palletsMover: Int,
        restosMover: Int,
        operador: String,
        deviceId: String,
        callback: (Boolean, String) -> Unit,
        operationId: String? = null
    ) {
        TraspasoValidator.validate(
            sku = sku,
            tipoTarima = tipoTarima,
            almacenOrigen = almacenOrigen,
            almacenDestino = almacenDestino,
            palletsMover = palletsMover,
            restosMover = restosMover
        )?.let {
            callback(false, it)
            return
        }
        val functionsClient = functions
        val firebaseAuth = auth
        if (functionsClient == null || firebaseAuth == null) {
            callback(false, FIREBASE_NOT_CONFIGURED_MESSAGE)
            return
        }
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            callback(false, "La sesión expiró. Inicia sesión nuevamente")
            return
        }
        val opId = operationId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val payload = hashMapOf<String, Any>(
            "sku" to sku,
            "tipoTarima" to tipoTarima,
            "almacenOrigen" to almacenOrigen,
            "almacenDestino" to almacenDestino,
            "palletsMover" to palletsMover,
            "restosMover" to restosMover,
            "deviceId" to deviceId,
            "operationId" to opId,
            "operador" to (
                firebaseUser.displayName
                    ?: firebaseUser.email
                    ?: operador.ifBlank { firebaseUser.uid }
                )
        )
        resolveLocalPlanta()?.let { payload["planta"] = it }
        functionsClient
            .getHttpsCallable("ejecutarTraspaso")
            .call(payload)
            .addOnSuccessListener { result ->
                val data = result.data as? Map<*, *>
                val msg = if (data?.get("idempotent") == true) {
                    "Traspaso ya aplicado (idempotente): $almacenOrigen → $almacenDestino"
                } else {
                    "Traspaso exitoso: $palletsMover pallets y $restosMover restos de " +
                        "$almacenOrigen → $almacenDestino"
                }
                callback(true, msg)
            }
            .addOnFailureListener { e ->
                callback(false, "Error en traspaso: ${e.message}")
            }
    }

    private fun resolveLocalPlanta(): String? {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        return prefs.getString("planta_activa", null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private companion object {
        const val INVENTARIOS_COLLECTION = "inventarios"
        const val INVENTORY_PAGE_SIZE = 100L
        const val FIREBASE_NOT_CONFIGURED_MESSAGE =
            "Firebase no está configurado en este build. Agrega app/google-services.json."
    }
}
