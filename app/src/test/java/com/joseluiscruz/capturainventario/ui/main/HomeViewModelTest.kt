package com.joseluiscruz.capturainventario.ui.main

import com.joseluiscruz.capturainventario.data.InventarioDao
import com.joseluiscruz.capturainventario.data.repository.InventorySessionStore
import com.joseluiscruz.capturainventario.util.NetworkStatusMonitor
import com.joseluiscruz.capturainventario.util.TpmRiskPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val inventarioDao: InventarioDao = mock()
    private val sessionStore: InventorySessionStore = mock()

    private val sessionId = MutableStateFlow("S1")
    private val escaneadosS1 = MutableStateFlow(0)
    private val escaneadosS2 = MutableStateFlow(0)
    private val pendientes = MutableStateFlow(0)
    private val fallidos = MutableStateFlow(0)
    private val tpmS1 = MutableStateFlow(0)
    private val tpmS2 = MutableStateFlow(0)
    private val online = MutableStateFlow(true)

    private val network = object : NetworkStatusMonitor {
        override val isOnline: Flow<Boolean> = online
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        whenever(sessionStore.sessionId).thenReturn(sessionId)
        whenever(inventarioDao.contarItemsEscaneados("S1")).thenReturn(escaneadosS1)
        whenever(inventarioDao.contarItemsEscaneados("S2")).thenReturn(escaneadosS2)
        whenever(inventarioDao.contarPendientesDeSync()).thenReturn(pendientes)
        whenever(inventarioDao.contarFallidosDeSync()).thenReturn(fallidos)
        whenever(inventarioDao.contarSkusEnRiesgoTpm(eq("S1"), any())).thenReturn(tpmS1)
        whenever(inventarioDao.contarSkusEnRiesgoTpm(eq("S2"), any())).thenReturn(tpmS2)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Mantiene un suscriptor activo (el StateFlow usa WhileSubscribed) y devuelve el VM. */
    private fun TestScope.subscribedViewModel(): HomeViewModel {
        val vm = HomeViewModel(inventarioDao, sessionStore, network)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect { } }
        return vm
    }

    @Test
    fun `estado inicial no inventa datos`() {
        val state = HomeKpisUiState()
        assertNull(state.escaneados)
        assertNull(state.pendientesSync)
        assertNull(state.fallidosSync)
        assertNull(state.tpmEnRiesgo)
        assertNull(state.online)
    }

    @Test
    fun `refleja los conteos reales de Room y el estado de red`() = runTest {
        escaneadosS1.value = 12
        pendientes.value = 5
        fallidos.value = 2
        tpmS1.value = 3

        val vm = subscribedViewModel()
        assertEquals(
            HomeKpisUiState(
                escaneados = 12,
                pendientesSync = 5,
                fallidosSync = 2,
                tpmEnRiesgo = 3,
                online = true
            ),
            vm.uiState.value
        )

        pendientes.value = 6
        assertEquals(6, vm.uiState.value.pendientesSync)

        online.value = false
        assertEquals(false, vm.uiState.value.online)
    }

    @Test
    fun `al cambiar de toma escaneados y TPM siguen a la sesion activa`() = runTest {
        escaneadosS1.value = 7
        tpmS1.value = 2

        val vm = subscribedViewModel()
        assertEquals(7, vm.uiState.value.escaneados)
        assertEquals(2, vm.uiState.value.tpmEnRiesgo)

        sessionId.value = "S2"
        assertEquals(0, vm.uiState.value.escaneados)
        assertEquals(0, vm.uiState.value.tpmEnRiesgo)

        escaneadosS2.value = 1
        assertEquals(1, vm.uiState.value.escaneados)

        // La sesión anterior ya no influye.
        escaneadosS1.value = 99
        assertEquals(1, vm.uiState.value.escaneados)
    }

    @Test
    fun `TPM usa el umbral de TpmRiskPolicy sobre la fecha de hoy`() = runTest {
        val antes = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        subscribedViewModel()
        val despues = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())

        val limite = argumentCaptor<Long>()
        verify(inventarioDao).contarSkusEnRiesgoTpm(eq("S1"), limite.capture())
        val umbral = TpmRiskPolicy.DEFAULT_THRESHOLD_DAYS
        assertTrue(limite.firstValue in (antes + umbral)..(despues + umbral))
    }

    @Test
    fun `un error en un KPI se degrada a sin dato sin afectar a los demas`() = runTest {
        whenever(inventarioDao.contarSkusEnRiesgoTpm(eq("S1"), any()))
            .thenReturn(flow { throw IllegalStateException("db") })
        escaneadosS1.value = 4

        val vm = subscribedViewModel()
        assertNull(vm.uiState.value.tpmEnRiesgo)
        assertEquals(4, vm.uiState.value.escaneados)
        assertEquals(true, vm.uiState.value.online)
    }

    @Test
    fun `fallidos ilegible se mantiene como sin dato y no como cero`() = runTest {
        whenever(inventarioDao.contarFallidosDeSync()).thenReturn(flow { throw IllegalStateException("db") })
        pendientes.value = 5

        val vm = subscribedViewModel()
        assertEquals(5, vm.uiState.value.pendientesSync)
        assertNull(vm.uiState.value.fallidosSync)
    }
}
