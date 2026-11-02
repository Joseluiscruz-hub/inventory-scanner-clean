package com.joseluiscruz.capturainventario.domain.validation

object TraspasoValidator {
    fun validate(
        sku: String,
        tipoTarima: String,
        almacenOrigen: String,
        almacenDestino: String,
        palletsMover: Int,
        restosMover: Int
    ): String? {
        if (sku.isBlank()) return "El SKU es obligatorio"
        if (tipoTarima.isBlank()) return "El tipo de tarima es obligatorio"
        if (almacenOrigen.isBlank() || almacenDestino.isBlank()) {
            return "Los almacenes de origen y destino son obligatorios"
        }
        if (almacenOrigen.equals(almacenDestino, ignoreCase = true)) {
            return "El almacén destino debe ser distinto al origen"
        }
        if (palletsMover < 0 || restosMover < 0) {
            return "Las cantidades no pueden ser negativas"
        }
        if (palletsMover == 0 && restosMover == 0) {
            return "Indica al menos una cantidad para traspasar"
        }
        return null
    }
}
