package com.joseluiscruz.capturainventario.model

/**
 * Model class representing a material item in the inventory.
 */
data class MaterialItem(
	val sku: String,
	val descripcion: String,
	val cxPal: String,
	val fpc: String,
	val con: String,
	val centro: String,
	val linea: String,
	val op: String,
	val fProd: String,
	val diasV: String,
	val ubicacion: String? = "",
	val totalPallets: String? = "",
	val restos: String? = "",
	val tipoTarima: String? = "",
	val almacen: String? = "", // Nuevo campo para asociar el escaneo al almacén seleccionado
	/** Fecha normalizada de Room; evita recalcular TPM con metadatos CSV incompletos. */
	val fechaCaducidadEpochDay: Long? = null
)
