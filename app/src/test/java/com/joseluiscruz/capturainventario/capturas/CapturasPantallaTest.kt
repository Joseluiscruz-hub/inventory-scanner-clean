package com.joseluiscruz.capturainventario.capturas

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.domain.model.ScanMode
import com.joseluiscruz.capturainventario.domain.model.User
import com.joseluiscruz.capturainventario.domain.repository.AuthRepository
import com.joseluiscruz.capturainventario.manager.AuthManager
import com.joseluiscruz.capturainventario.ui.inventario.screen.ComparisonControlCard
import com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioComposeViewModel
import com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioScreen
import com.joseluiscruz.capturainventario.ui.inventario.screen.MainScannerCard
import com.joseluiscruz.capturainventario.ui.inventario.screen.OperativoStatusCard
import com.joseluiscruz.capturainventario.ui.login.LoginScreen
import com.joseluiscruz.capturainventario.ui.login.state.LoginUiState
import com.joseluiscruz.capturainventario.ui.main.HomeKpisUiState
import com.joseluiscruz.capturainventario.ui.main.OpsHomeScreen
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.io.FileOutputStream

/**
 * Arnés de capturas de pantalla (Robolectric, gráficos nativos) para comparar el diseño
 * antes/después de un cambio de UI sin emulador.
 *
 * Se excluye de `testDebugUnitTest` normal y de CI; solo corre si se pasa `-PcapturasDir`:
 * ```
 * ./gradlew :app:testDebugUnitTest --tests '*CapturasPantallaTest*' \
 *     -PcapturasDir=/ruta/salida -PcapturasVariante=antes
 * ```
 * Genera `<nombre>__<variante>.png` en la carpeta indicada. Ejecutándolo en la rama base
 * (`antes`) y en la rama del cambio (`despues`) se obtienen pares comparables.
 *
 * Notas: tipografía de respaldo de Robolectric (la misma en ambas variantes); los datos son
 * de ejemplo (usuario falso, KPIs fijos, ViewModel de captura con dependencias simuladas).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class, qualifiers = "w400dp-h1600dp-xhdpi")
class CapturasPantallaTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val variante = System.getProperty("capturas.variante") ?: "actual"
    private val outDir = File(System.getProperty("capturas.out") ?: "build/capturas").also { it.mkdirs() }

    private class FakeAuth(user: User?) : AuthRepository {
        override val currentUser: StateFlow<User?> = MutableStateFlow(user)
        override suspend fun login(username: String, password: String) = Result.failure<User>(Exception("sin red"))
        override suspend fun logout() = Unit
        override suspend fun isUserLoggedIn() = true
    }

    private fun capturar(nombre: String, content: @Composable () -> Unit) {
        rule.setContent {
            PremiumTheme(darkTheme = true) {
                Surface(color = MaterialTheme.colorScheme.background) { content() }
            }
        }
        // Deja terminar las animaciones de entrada.
        rule.mainClock.advanceTimeBy(3_000)
        rule.waitForIdle()
        val view = rule.activity.window.decorView.rootView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        rule.runOnUiThread { view.draw(Canvas(bitmap)) }
        FileOutputStream(File(outDir, "${nombre}__$variante.png")).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun tarjeta(content: @Composable () -> Unit): @Composable () -> Unit = {
        Box(Modifier.width(400.dp).wrapContentHeight().padding(16.dp)) { content() }
    }

    private fun capturaViewModel(): InventarioComposeViewModel = InventarioComposeViewModel(
        ApplicationProvider.getApplicationContext(),
        Mockito.mock(CsvInventarioService::class.java),
        Mockito.mock(InventarioSharedViewModel::class.java),
        Mockito.mock(InventarioRepository::class.java)
    )

    private val kpisEjemplo = HomeKpisUiState(
        escaneados = 128, pendientesSync = 12, fallidosSync = 2, tpmEnRiesgo = 5, online = true
    )

    @Test
    fun home_operador() {
        AuthManager.init(FakeAuth(User("1", "operador1", "o@example.com", "OPERADOR")))
        capturar("01_home_operador") { OpsHomeScreen(kpis = kpisEjemplo) }
    }

    @Test
    fun home_admin() {
        AuthManager.init(FakeAuth(User("2", "admin1", "a@example.com", "ADMIN")))
        capturar("02_home_admin") { OpsHomeScreen(kpis = kpisEjemplo) }
    }

    @Test
    fun login() {
        capturar("03_login") { LoginScreen(uiState = LoginUiState(), onEvent = {}) }
    }

    @Test
    fun captura_pantalla_completa() {
        val vm = capturaViewModel()
        capturar("04_captura_pantalla") { InventarioScreen(viewModel = vm, onBack = {}, onOpenCalculator = {}) }
    }

    @Test
    @Config(qualifiers = "w393dp-h851dp-xxhdpi")
    fun captura_telefono() {
        val vm = capturaViewModel()
        capturar("08_captura_telefono_393x851") { InventarioScreen(viewModel = vm, onBack = {}, onOpenCalculator = {}) }
    }

    @Test
    fun captura_tarjeta_estado() {
        capturar("05_captura_tarjeta_estado", tarjeta {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OperativoStatusCard(
                    almacen = "ALM-01 Planta", modo = "RUMBA", tarima = "CHEP",
                    referenciaCargada = false, cargandoReferenciaSap = false,
                    onTarimaClick = {}, onLoadSapClick = {}, onNuevaTomaClick = {}
                )
                OperativoStatusCard(
                    almacen = "Seleccionar Almacén", modo = "DEFAULT", tarima = "ESTÁNDAR",
                    referenciaCargada = true, cargandoReferenciaSap = false,
                    onTarimaClick = {}, onLoadSapClick = {}, onNuevaTomaClick = {}
                )
            }
        })
    }

    @Test
    fun captura_metodos() {
        capturar("06_captura_metodos", tarjeta {
            MainScannerCard(
                onScan = {}, onModeChange = {}, currentMode = ScanMode.RUMBA,
                escaneoContinuo = true, onEscaneoContinuoChange = {}
            )
        })
    }

    @Test
    fun captura_comparacion() {
        capturar("07_captura_comparacion", tarjeta {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ComparisonControlCard(isActive = false, onStart = {}, onStop = {}, faltantes = 0, sobrantes = 0, diferencias = 0)
                ComparisonControlCard(isActive = true, onStart = {}, onStop = {}, faltantes = 3, sobrantes = 1, diferencias = 4)
                ComparisonControlCard(isActive = true, onStart = {}, onStop = {}, faltantes = 0, sobrantes = 0, diferencias = 0)
            }
        })
    }
}
