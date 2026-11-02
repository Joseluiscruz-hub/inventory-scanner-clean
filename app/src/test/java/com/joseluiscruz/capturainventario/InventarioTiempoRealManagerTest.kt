package com.joseluiscruz.capturainventario

import app.cash.turbine.test
import com.joseluiscruz.capturainventario.data.InventarioEscaneadoEntity
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.ui.comparacion.EstadoDiferencia
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager
import com.joseluiscruz.capturainventario.ui.comparacion.InventarioTiempoRealManager.ItemReferencia
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

/**
 * Tests unitarios para InventarioTiempoRealManager (Enterprise-Grade).
 *
 * Valida:
 * 1. Comparación básica (EXCESO, FALTANTE, COINCIDE)
 * 2. Normalización case-insensitive (sku-123 == SKU-123)
 * 3. Blind Scan Mode (sin referencia SAP)
 * 4. Precálculo de totalReferenciaPallets
 * 5. Ordenamiento por criticidad
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InventarioTiempoRealManagerTest {

    private lateinit var manager: InventarioTiempoRealManager
    private lateinit var repository: InventarioRepository
    private val escaneadosFlow = MutableStateFlow<List<InventarioEscaneadoEntity>>(emptyList())

    @Before
    fun setup() {
        repository = mock()
        whenever(repository.observarEscaneados()).thenReturn(escaneadosFlow)
        manager = InventarioTiempoRealManager(repository)
    }

    // ========================================================
    // Test 1: Comparación básica — COINCIDE
    // ========================================================
    @Test
    fun `cuando escaneado coincide con referencia, estado es COINCIDE`() = runTest {
        // Cargar referencia SAP
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "SKU001", descripcion = "PLANTA DE MANUFACTURA 600ml", cantidad = 10)
        ))

        // Simular escaneo que coincide exactamente
        escaneadosFlow.value = listOf(
            crearEscaneado("SKU001", "PLANTA DE MANUFACTURA 600ml", cantidadTotal = 10)
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()
            val item = resultado.find { it.codigo == "SKU001" }

            assertNotNull("SKU001 debería existir en la comparación", item)
            assertEquals(EstadoDiferencia.COINCIDE, item!!.estado)
            assertEquals(0, item.diferencia)
            assertEquals(10, item.cantidadReferencia)
            assertEquals(10, item.cantidadActual)
        }
    }

    // ========================================================
    // Test 2: Detección de EXCESO
    // ========================================================
    @Test
    fun `cuando escaneado es mayor que referencia, estado es EXCESO`() = runTest {
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "SKU002", descripcion = "Fanta 2L", cantidad = 5)
        ))

        escaneadosFlow.value = listOf(
            crearEscaneado("SKU002", "Fanta 2L", cantidadTotal = 8)
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()
            val item = resultado.find { it.codigo == "SKU002" }

            assertNotNull(item)
            assertEquals(EstadoDiferencia.EXCESO, item!!.estado)
            assertEquals(3, item.diferencia) // 8 - 5 = +3
        }
    }

    // ========================================================
    // Test 3: Detección de FALTANTE puro (no escaneado)
    // ========================================================
    @Test
    fun `items en referencia no escaneados aparecen como FALTANTE`() = runTest {
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "SKU003", descripcion = "Sprite 1L", cantidad = 20),
            ItemReferencia(codigo = "SKU004", descripcion = "PLANTA DE MANUFACTURA Zero", cantidad = 15)
        ))

        // Solo escaneamos SKU003
        escaneadosFlow.value = listOf(
            crearEscaneado("SKU003", "Sprite 1L", cantidadTotal = 20)
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()

            // SKU003 coincide
            val sku3 = resultado.find { it.codigo == "SKU003" }
            assertEquals(EstadoDiferencia.COINCIDE, sku3!!.estado)

            // SKU004 no fue escaneado → FALTANTE
            val sku4 = resultado.find { it.codigo == "SKU004" }
            assertNotNull("SKU004 debería aparecer como faltante", sku4)
            assertEquals(EstadoDiferencia.FALTANTE, sku4!!.estado)
            assertEquals(-15, sku4.diferencia)
            assertEquals(0, sku4.cantidadActual)
        }
    }

    // ========================================================
    // Test 4: Normalización Case-Insensitive
    // ========================================================
    @Test
    fun `normalizacion uppercase garantiza cruce correcto`() = runTest {
        // Referencia viene en minúscula
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "sku-abc", descripcion = "Producto ABC", cantidad = 7)
        ))

        // Escaneo viene en mayúscula (como lo haría un QR reader)
        escaneadosFlow.value = listOf(
            crearEscaneado("SKU-ABC", "Producto ABC", cantidadTotal = 7)
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()

            // Debe haber coincidencia, NO un falso exceso + faltante
            val coincidencias = resultado.filter { it.estado == EstadoDiferencia.COINCIDE }
            val excesos = resultado.filter { it.estado == EstadoDiferencia.EXCESO }

            assertEquals("Debería haber exactamente 1 coincidencia", 1, coincidencias.size)
            assertEquals("No debería haber excesos falsos", 0, excesos.size)
        }
    }

    // ========================================================
    // Test 5: Blind Scan Mode (sin referencia SAP)
    // ========================================================
    @Test
    fun `blind scan mode registra captura sin generar exceso`() = runTest {
        // NO cargar referencia SAP → Blind Scan Mode

        escaneadosFlow.value = listOf(
            crearEscaneado("BLIND001", "Producto Ciego", cantidadTotal = 3)
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()

            // Debe mostrar el item, pero no compararlo contra un cero ficticio.
            assertEquals(1, resultado.size)
            val item = resultado.first()
            assertEquals("BLIND001", item.codigo)
            assertEquals(EstadoDiferencia.SIN_REFERENCIA, item.estado)
            assertEquals(3, item.diferencia) // 3 - 0 = +3
            assertEquals(0, item.cantidadReferencia)
        }
    }

    @Test
    fun `capturas sin referencia no se consideran diferencias criticas`() = runTest {
        escaneadosFlow.value = listOf(
            crearEscaneado("BLIND002", "Producto Ciego", cantidadTotal = 1)
        )

        manager.diferenciasCriticas.test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    // ========================================================
    // Test 6: Precálculo de totalReferenciaPallets
    // ========================================================
    @Test
    fun `tieneReferenciaCargada devuelve true despues de cargar`() {
        assertFalse(manager.tieneReferenciaCargada())

        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "X1", descripcion = "Test", cantidad = 100)
        ))

        assertTrue(manager.tieneReferenciaCargada())
    }

    @Test
    fun `getInventarioReferencia devuelve mapa normalizado`() {
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "abc-123", descripcion = "Test Lower", cantidad = 50),
            ItemReferencia(codigo = "DEF-456", descripcion = "Test Upper", cantidad = 30)
        ))

        val mapa = manager.getInventarioReferencia()
        // Ambas llaves deben estar en UPPERCASE
        assertTrue("Clave ABC-123 debe existir", mapa.containsKey("ABC-123"))
        assertTrue("Clave DEF-456 debe existir", mapa.containsKey("DEF-456"))
        assertFalse("Clave abc-123 en minúscula NO debe existir", mapa.containsKey("abc-123"))
    }

    // ========================================================
    // Test 7: Ordenamiento por criticidad
    // ========================================================
    @Test
    fun `resultado ordenado por criticidad EXCESO primero, luego FALTANTE, luego COINCIDE`() = runTest {
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "COINCIDE1", descripcion = "OK", cantidad = 10),
            ItemReferencia(codigo = "FALTANTE1", descripcion = "Falta", cantidad = 20),
            ItemReferencia(codigo = "EXCESO1", descripcion = "Sobra", cantidad = 5)
        ))

        escaneadosFlow.value = listOf(
            crearEscaneado("COINCIDE1", "OK", cantidadTotal = 10),
            crearEscaneado("EXCESO1", "Sobra", cantidadTotal = 15) // +10 exceso
            // FALTANTE1 no se escanea → faltante
        )

        manager.comparacionEnTiempoReal.test {
            val resultado = awaitItem()

            assertEquals(3, resultado.size)
            // Primer item debe ser EXCESO (más crítico)
            assertEquals(EstadoDiferencia.EXCESO, resultado[0].estado)
            // Segundo item debe ser FALTANTE
            assertEquals(EstadoDiferencia.FALTANTE, resultado[1].estado)
            // Tercer item debe ser COINCIDE
            assertEquals(EstadoDiferencia.COINCIDE, resultado[2].estado)
        }
    }

    // ========================================================
    // Test 8: Diferencias críticas filtra correctamente
    // ========================================================
    @Test
    fun `diferenciasCriticas excluye items que coinciden`() = runTest {
        manager.cargarInventarioSistema(listOf(
            ItemReferencia(codigo = "A", descripcion = "OK", cantidad = 10),
            ItemReferencia(codigo = "B", descripcion = "Exceso", cantidad = 5)
        ))

        escaneadosFlow.value = listOf(
            crearEscaneado("A", "OK", cantidadTotal = 10),
            crearEscaneado("B", "Exceso", cantidadTotal = 8)
        )

        manager.diferenciasCriticas.test {
            val criticos = awaitItem()

            // Solo B debería aparecer (es EXCESO)
            assertEquals(1, criticos.size)
            assertEquals("B", criticos[0].codigo)
            assertEquals(EstadoDiferencia.EXCESO, criticos[0].estado)
        }
    }

    // ========================================================
    // Utilidades
    // ========================================================
    private fun crearEscaneado(
        codigo: String,
        descripcion: String,
        cantidadTotal: Int,
        timestamp: Long = System.currentTimeMillis()
    ) = InventarioEscaneadoEntity(
        operationId = java.util.UUID.randomUUID().toString(),
        sessionId = "TEST_SESSION",
        codigo = codigo,
        descripcion = descripcion,
        cantidadTotal = cantidadTotal,
        ultimoScanTimestamp = timestamp
    )
}
