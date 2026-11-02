package com.joseluiscruz.capturainventario.data.repository

import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.model.MaterialItem

/** Convierte el estado consolidado de Room a la proyección CSV/visual existente. */
fun InventarioEscaneadoEntity.toMaterialItem(): MaterialItem = MaterialItem(
    sku = codigo,
    descripcion = descripcion,
    cxPal = "",
    fpc = "",
    con = "",
    centro = "",
    linea = "",
    // El modelo CSV histórico usa OP como el mejor campo disponible para lote.
    op = lote.orEmpty(),
    fProd = fechaEtiqueta.orEmpty(),
    diasV = "",
    ubicacion = ubicacion.orEmpty(),
    totalPallets = cantidadTotal.toString(),
    restos = cantidadRestos.toString(),
    tipoTarima = tipoTarima.orEmpty(),
    almacen = almacen.orEmpty(),
    fechaCaducidadEpochDay = fechaCaducidadEpochDay
)
