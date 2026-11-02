package com.joseluiscruz.capturainventario.data

/**
 * Catálogo de SKUs de Productos Terminados (PT) con sus configuraciones.
 * Cada producto terminado tiene asociado:
 * - Número de cajas por pallet
 * - SKU de la tarima plástica que usa
 * 
 * Los materiales (como tarimas plásticas) se cuentan en piezas directamente.
 */
object ProductoTerminadoCatalogo {

    /**
     * Overrides cargados dinamicamente desde CSV / nube.
     * Se usan para SKUs no definidos en el estandar global.
     */
    private val configuracionesDinamicas = mutableMapOf<String, ProductoTerminadoConfig>()

    /**
     * SKU de la tarima plástica estándar PLANTA DE MANUFACTURA
     */
    const val TARIMA_PLASTICA_ESTANDAR = "195051"

    /**
     * Datos de configuración para un producto terminado
     */
    data class ProductoTerminadoConfig(
        val sku: String,
        val cajasXPallet: Int,
        val tarimaPlasticaSku: String = TARIMA_PLASTICA_ESTANDAR
    )

    /**
     * Catalogo global estandar (fuente confiable de estibaje para conversion a cajas).
     */
private val catalogoGlobalEstandar: Map<String, ProductoTerminadoConfig> = mapOf(
        "360" to ProductoTerminadoConfig("360", 54),
        "376" to ProductoTerminadoConfig("376", 40),
        "396" to ProductoTerminadoConfig("396", 40),
        "445" to ProductoTerminadoConfig("445", 64),
        "500" to ProductoTerminadoConfig("500", 40),
        "501" to ProductoTerminadoConfig("501", 32),
        "610" to ProductoTerminadoConfig("610", 108),
        "1119" to ProductoTerminadoConfig("1119", 48),
        "2352" to ProductoTerminadoConfig("2352", 36),
        "2621" to ProductoTerminadoConfig("2621", 108),
        "2623" to ProductoTerminadoConfig("2623", 64),
        "2898" to ProductoTerminadoConfig("2898", 32),
        "2908" to ProductoTerminadoConfig("2908", 40),
        "2909" to ProductoTerminadoConfig("2909", 55),
        "3017" to ProductoTerminadoConfig("3017", 32),
        "3158" to ProductoTerminadoConfig("3158", 64),
        "3276" to ProductoTerminadoConfig("3276", 36),
        "3296" to ProductoTerminadoConfig("3296", 64),
        "170460" to ProductoTerminadoConfig("170460", 32)
    )

    /**
     * Catalogo legado complementario para SKUs historicos no incluidos
     * en el estandar global actual.
     */
    private val catalogoLegacyComplementario: Map<String, ProductoTerminadoConfig> = mapOf(
        "99939" to ProductoTerminadoConfig("99939", 32),
        "3020" to ProductoTerminadoConfig("3020", 64),
        "933" to ProductoTerminadoConfig("933", 144),
        "99938" to ProductoTerminadoConfig("99938", 32),
        "99940" to ProductoTerminadoConfig("99940", 32),
        "3055" to ProductoTerminadoConfig("3055", 32),
        "171836" to ProductoTerminadoConfig("171836", 81, "195046"),
        "171036" to ProductoTerminadoConfig("171036", 36, "195054")
    )

    /**
     * SKUs de materiales (tarimas plásticas) que se cuentan en piezas
     */
    private val materialesTarimas: Set<String> = setOf(
        "195051",  // Tarima plástica PLANTA DE MANUFACTURA estándar
        "195046",  // Tarima plástica alternativa
        "195054",  // Tarima plástica alternativa
        "195057",  // Tarima plástica
        "195536",  // Tarima plástica
        "195538",  // Tarima plástica
        "195540"   // Tarima plástica
    )

    @Synchronized
    fun cargarConfiguracionesDinamicas(configuraciones: List<ProductoTerminadoConfig>) {
        configuracionesDinamicas.clear()
        configuraciones.forEach { config ->
            if (config.sku.isNotBlank()) {
                configuracionesDinamicas[config.sku] = config
            }
        }
    }

    @Synchronized
    fun limpiarConfiguracionesDinamicas() {
        configuracionesDinamicas.clear()
    }

    @Synchronized
    fun obtenerConfiguracionesDinamicas(): List<ProductoTerminadoConfig> =
        configuracionesDinamicas.values.toList()

    private fun resolverConfiguracion(sku: String): ProductoTerminadoConfig? {
        // Precedencia: estandar global -> dinamico -> legacy.
        return catalogoGlobalEstandar[sku]
            ?: configuracionesDinamicas[sku]
            ?: catalogoLegacyComplementario[sku]
    }

    /**
     * Verifica si un SKU es un producto terminado
     */
    fun esProductoTerminado(sku: String): Boolean {
        return resolverConfiguracion(sku) != null
    }

    /**
     * Verifica si un SKU es un material (tarima plástica)
     */
    fun esMaterial(sku: String): Boolean {
        return materialesTarimas.contains(sku)
    }

    /**
     * Obtiene la configuración de un producto terminado
     * @return ProductoTerminadoConfig si existe, null si no es PT
     */
    fun obtenerConfiguracion(sku: String): ProductoTerminadoConfig? {
        return resolverConfiguracion(sku)
    }

    /**
     * Obtiene las cajas por pallet de un producto terminado
     * @return número de cajas por pallet, o null si no es PT
     */
    fun obtenerCajasXPallet(sku: String): Int? {
        return resolverConfiguracion(sku)?.cajasXPallet
    }

    /**
     * Calcula el total de cajas basado en el número de pallets
     * @param sku SKU del producto terminado
     * @param pallets número de pallets contados
     * @return total de cajas (pallets * cajasXPallet), o null si no es PT
     */
    fun calcularTotalCajas(sku: String, pallets: Int): Int? {
        val config = resolverConfiguracion(sku) ?: return null
        return pallets * config.cajasXPallet
    }

    /**
     * Obtiene el SKU de la tarima plástica asociada a un producto terminado
     * @return SKU de tarima plástica, o null si no es PT
     */
    fun obtenerTarimaPlasticaSku(sku: String): String? {
        return resolverConfiguracion(sku)?.tarimaPlasticaSku
    }

    /**
     * Resultado del cálculo de inventario para producto terminado
     */
    data class ResultadoCalculoPT(
        val totalCajas: Int,
        val totalPallets: Int,
        val tarimasPlasticas: Int,
        val tarimaPlasticaSku: String
    )

    /**
     * Calcula el inventario completo para un producto terminado
     * @param sku SKU del producto terminado
     * @param palletsContados número de pallets contados físicamente
     * @return ResultadoCalculoPT con totales, o null si no es PT
     * 
     * Ejemplo: Si cuento 64 pallets del SKU 2352 (36 cajas/pallet):
     * - totalCajas = 64 * 36 = 2304
     * - totalPallets = 64
     * - tarimasPlasticas = 64 (cada pallet usa una tarima)
     * - tarimaPlasticaSku = "195051"
     */
    fun calcularInventarioPT(sku: String, palletsContados: Int): ResultadoCalculoPT? {
        val config = resolverConfiguracion(sku) ?: return null
        return ResultadoCalculoPT(
            totalCajas = palletsContados * config.cajasXPallet,
            totalPallets = palletsContados,
            tarimasPlasticas = palletsContados,  // 1 tarima por pallet
            tarimaPlasticaSku = config.tarimaPlasticaSku
        )
    }

    /**
     * Obtiene todos los SKUs de productos terminados
     */
    fun obtenerTodosLosSKUs(): Set<String> {
        return (catalogoGlobalEstandar.keys + catalogoLegacyComplementario.keys + configuracionesDinamicas.keys).toSet()
    }

    /**
     * Obtiene todos los SKUs de materiales (tarimas)
     */
    fun obtenerTodosLosMateriales(): Set<String> {
        return materialesTarimas
    }
}
