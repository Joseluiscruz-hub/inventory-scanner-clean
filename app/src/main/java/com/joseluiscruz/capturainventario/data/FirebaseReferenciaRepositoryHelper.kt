package com.joseluiscruz.capturainventario.data

import android.content.Context
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.joseluiscruz.capturainventario.util.CsvParser
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * inventario_referencia client paths: stamp `planta` on writes; filter reads by claim.
 * ADMIN without claim may see all. Aligns with firestore.rules from PR #20.
 */
internal object FirebaseReferenciaRepositoryHelper {
    private const val REFERENCIA_COLLECTION = "inventario_referencia"
    private const val FIREBASE_NOT_CONFIGURED_MESSAGE =
        "Firebase no está configurado en este build. Agrega app/google-services.json."

    private fun db(context: Context): FirebaseFirestore? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseFirestore.getInstance() else null

    private fun auth(context: Context): FirebaseAuth? =
        if (FirebaseApp.getApps(context).isNotEmpty()) FirebaseAuth.getInstance() else null

    private fun resolveLocalPlanta(context: Context): String? {
        val prefs = context.getSharedPreferences("app_config", Context.MODE_PRIVATE)
        return prefs.getString("planta_activa", null)?.trim()?.takeIf { it.isNotEmpty() }
    }

    private fun plantStableReferenciaId(planta: String, sku: String): String =
        listOf(planta, sku)
            .joinToString("__") { it.replace(Regex("[/\\s:]+"), "_") }

    fun subir(
        context: Context,
        items: List<InventarioTiempoRealManager.ItemReferencia>,
        callback: (Boolean, String) -> Unit
    ) {
        if (items.isEmpty()) {
            callback(false, "No hay referencia para sincronizar")
            return
        }
        val invalidItem = items.firstOrNull { !CsvParser.isValidReferenceItem(it) }
        if (invalidItem != null) {
            callback(
                false,
                "La referencia contiene un SKU inválido o corrupto: ${invalidItem.codigo.take(40)}"
            )
            return
        }
        val firestore = db(context)
        val firebaseAuth = auth(context)
        if (firestore == null || firebaseAuth == null) {
            callback(false, FIREBASE_NOT_CONFIGURED_MESSAGE)
            return
        }
        val user = firebaseAuth.currentUser
        if (user == null) {
            callback(false, "La sesión expiró. Inicia sesión nuevamente")
            return
        }
        user.getIdToken(false)
            .addOnSuccessListener { token ->
                val role = (token.claims["role"] as? String)?.uppercase()
                if (role != "ADMIN") {
                    callback(false, "Solo ADMIN puede escribir inventario_referencia")
                    return@addOnSuccessListener
                }
                val planta = (token.claims["planta"] as? String)?.trim().orEmpty()
                    .ifEmpty { resolveLocalPlanta(context).orEmpty() }
                if (planta.isBlank()) {
                    callback(
                        false,
                        "Falta planta (claim o preferencia planta_activa) para stamp en referencia"
                    )
                    return@addOnSuccessListener
                }
                commitChunks(firestore, items, planta, callback)
            }
            .addOnFailureListener { e ->
                callback(false, "No se pudo leer claims: ${e.message}")
            }
    }

    private fun commitChunks(
        firestore: FirebaseFirestore,
        items: List<InventarioTiempoRealManager.ItemReferencia>,
        planta: String,
        callback: (Boolean, String) -> Unit
    ) {
        val chunks = items.chunked(450)
        var successfulChunks = 0
        val totalChunks = chunks.size
        val timestamp = com.google.firebase.Timestamp.now()
        chunks.forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { item ->
                val docId = plantStableReferenciaId(planta, item.codigo)
                batch.set(
                    firestore.collection(REFERENCIA_COLLECTION).document(docId),
                    hashMapOf(
                        "sku" to item.codigo,
                        "descripcion" to item.descripcion,
                        "cantidad" to item.cantidad,
                        "cajasXPallet" to item.cajasXPallet,
                        "tarimaPlasticaSku" to item.tarimaPlasticaSku,
                        "armado" to item.armado,
                        "planta" to planta,
                        "timestamp" to timestamp
                    ),
                    SetOptions.merge()
                )
            }
            batch.commit()
                .addOnSuccessListener {
                    successfulChunks++
                    if (successfulChunks == totalChunks) {
                        callback(
                            true,
                            "Referencia sincronizada: ${items.size} registros (planta=$planta)"
                        )
                    }
                }
                .addOnFailureListener { e ->
                    callback(false, "Error parcial en sincronización: ${e.message}")
                }
        }
    }

    fun descargar(
        context: Context,
        callback: (List<InventarioTiempoRealManager.ItemReferencia>?, String) -> Unit
    ) {
        val firestore = db(context)
        val firebaseAuth = auth(context)
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
            .addOnSuccessListener { token ->
                val role = (token.claims["role"] as? String)?.uppercase()
                val planta = (token.claims["planta"] as? String)?.trim().orEmpty()
                val isAdmin = role == "ADMIN"
                if (!isAdmin && planta.isBlank()) {
                    callback(
                        null,
                        "El operador no tiene claim 'planta'. Asígnalo con scripts/firebase/set-user-role.mjs"
                    )
                    return@addOnSuccessListener
                }
                val query = if (!isAdmin || planta.isNotBlank()) {
                    firestore.collection(REFERENCIA_COLLECTION).whereEqualTo("planta", planta)
                } else {
                    firestore.collection(REFERENCIA_COLLECTION)
                }
                query.get()
                    .addOnSuccessListener { result ->
                        val lista = mutableListOf<InventarioTiempoRealManager.ItemReferencia>()
                        for (document in result) {
                            try {
                                val item = InventarioTiempoRealManager.ItemReferencia(
                                    codigo = document.getString("sku") ?: document.id,
                                    descripcion = document.getString("descripcion") ?: "",
                                    cantidad = (document.getLong("cantidad") ?: 0L).toInt(),
                                    cajasXPallet = (document.getLong("cajasXPallet") ?: 0L).toInt(),
                                    tarimaPlasticaSku = document.getString("tarimaPlasticaSku") ?: "",
                                    armado = document.getString("armado") ?: ""
                                )
                                if (CsvParser.isValidReferenceItem(item)) {
                                    lista.add(item)
                                } else {
                                    android.util.Log.w(
                                        "FirebaseReferencia",
                                        "Referencia corrupta ignorada: ${document.id}"
                                    )
                                }
                            } catch (e: Exception) {
                                android.util.Log.w(
                                    "FirebaseReferencia",
                                    "Referencia ilegible: ${document.id}",
                                    e
                                )
                            }
                        }
                        callback(lista, "Descarga exitosa: ${lista.size} referencias")
                    }
                    .addOnFailureListener { exception ->
                        callback(null, "Error al descargar referencia: ${exception.message}")
                    }
            }
            .addOnFailureListener { e ->
                callback(null, "No se pudo leer claims: ${e.message}")
            }
    }
}
