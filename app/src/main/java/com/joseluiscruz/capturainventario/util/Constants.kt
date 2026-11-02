package com.joseluiscruz.capturainventario.util

/**
 * Constants used throughout the application.
 */
object Constants {
    // File names
    const val INVENTORY_FILE_NAME = "materiales_guardados.csv"

    // Restos se agrega al final para conservar compatibilidad con CSV históricos de 14 columnas.
    const val CSV_HEADER = "SKU,DP,CxPal,FPC,Con,Centro,LINEA,OP,FProd,Dias V,Ubicacion,TotalPallets,TipoTarima,Almacen,Restos"
}
