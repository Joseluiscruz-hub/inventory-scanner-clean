package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.data.repository.InventarioRepository
import com.joseluiscruz.capturainventario.RestosActivity
import com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager
import com.joseluiscruz.capturainventario.ui.common.PremiumCalculatorService
import com.joseluiscruz.capturainventario.ui.comparacion.ComparacionTiempoRealActivity
import com.joseluiscruz.capturainventario.ui.inventario.state.InventarioEvent
import com.joseluiscruz.capturainventario.ui.inventario.state.NavigationEvent
import com.joseluiscruz.capturainventario.ui.theme.DarkPremiumTheme
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.joseluiscruz.capturainventario.util.ScannerEngine
import com.joseluiscruz.capturainventario.util.ScannerOutcome
import com.joseluiscruz.capturainventario.domain.model.TipoTarima
import com.joseluiscruz.capturainventario.ui.inventario.state.ResultadoExportacion
import javax.inject.Inject

/**
 * Actividad principal para la pantalla de Captura de Inventario en Jetpack Compose.
 */
@AndroidEntryPoint
class InventarioComposeActivity : ComponentActivity() {

    @Inject lateinit var repository: InventarioRepository
    @Inject lateinit var csvService: CsvInventarioService

    private val sharedViewModel: com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel by viewModels()

    private val composeViewModel: InventarioComposeViewModel by viewModels {
        InventarioViewModelFactory(application, csvService, sharedViewModel, repository)
    }

    private lateinit var almacenManager: AlmacenCapacidadManager
    private lateinit var gmsScanner: GmsBarcodeScanner

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        isScannerLaunching = false
        onScannerOutcome(ScannerOutcome.fromZxing(result.contents))
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        try {
            if (isGranted) {
                iniciarEscaneoQR()
            } else {
                mostrarError("Sin permiso de cámara no se puede escanear. Actívalo en Ajustes > Aplicaciones > Permisos.")
            }
        } catch (e: Exception) {
            mostrarError("No se pudo abrir la cámara. Inténtalo de nuevo.", e)
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            startCalculatorService()
        } else {
            mostrarError("La calculadora flotante necesita el permiso \"Mostrar sobre otras apps\".")
        }
    }

    private fun startCalculatorService() {
        val intent = Intent(this, PremiumCalculatorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            mostrarError("No se pudo abrir la calculadora.", e)
        }
    }

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            composeViewModel.handleEvent(InventarioEvent.OnExportarCsvDestinoElegido(uri))
        } else {
            composeViewModel.handleEvent(
                InventarioEvent.OnExportacionCsvTerminada(ResultadoExportacion.Cancelada)
            )
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) cargarInventario(uri)
    }

    private var pendingScanType = ""
    private var almacenSeleccionado = ""
    private var isScannerLaunching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        almacenManager = AlmacenCapacidadManager(this)

        val gmsOptions = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        gmsScanner = GmsBarcodeScanning.getClient(this, gmsOptions)

        val tipoEscaneo = intent.getStringExtra("tipo_escaneo") ?: ""
        if (tipoEscaneo.isNotEmpty()) {
            composeViewModel.handleEvent(
                InventarioEvent.OnScanModeSelected(
                    com.joseluiscruz.capturainventario.domain.model.ScanMode.fromString(
                        tipoEscaneo
                    )
                )
            )
        }

        lifecycleScope.launch {
            composeViewModel.scanRequestTrigger.collectLatest { trigger ->
                if (trigger != null) {
                    verificarPermisosYEscaneo()
                }
            }
        }

        lifecycleScope.launch {
            composeViewModel.navigationEvent.collect { event ->
                when (event) {
                    NavigationEvent.ToResumen ->
                        startActivity(Intent(this@InventarioComposeActivity, InventarioResumenComposeActivity::class.java))
                    NavigationEvent.ToCapturaParaImportar ->
                        startActivity(Intent(this@InventarioComposeActivity, InventarioComposeActivity::class.java))
                    NavigationEvent.ToRestos ->
                        startActivity(Intent(this@InventarioComposeActivity, RestosActivity::class.java))
                    NavigationEvent.ToComparacionTiempoReal -> {
                        val intent = Intent(this@InventarioComposeActivity, ComparacionTiempoRealActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                    NavigationEvent.ExportarCsv -> exportarInventario()
                    NavigationEvent.ToEnviarCorreo -> {
                        val intent = Intent(this@InventarioComposeActivity, InventarioComposeActivity::class.java)
                        intent.putExtra("enviar_por_correo", true)
                        startActivity(intent)
                    }
                }
            }
        }

        setContent {
            DarkPremiumTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // El FAB de calculadora vive en el Scaffold de InventarioScreen, encima de la
                        // barra inferior GUARDAR/SIGUIENTE, para que nunca la tape.
                        InventarioScreen(
                            viewModel = composeViewModel,
                            onBack = { finish() },
                            onOpenCalculator = ::abrirCalculadora
                        )
                    }
                }
            }
        }
    }

    private fun abrirCalculadora() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startCalculatorService()
        }
    }

    /** ML Kit y ZXing convergen aquí: el ViewModel aplica el feedback y el flujo comunes. */
    private fun onScannerOutcome(outcome: ScannerOutcome) {
        composeViewModel.handleEvent(InventarioEvent.OnScannerOutcome(outcome))
    }

    private fun mostrarDialogoAlmacen() {
        try {
            val almacenesConfigurados = almacenManager.obtenerAlmacenes()

            if (almacenesConfigurados.isEmpty()) {
                Toast.makeText(
                    this,
                    "No hay almacenes configurados. Ve a Gestión de Almacenes para crearlos.",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            val almacenesNombres = almacenesConfigurados.map { it.nombreAlmacen }.toTypedArray()
            val almacenesCapacidades = almacenesConfigurados.map { it.capacidadMaxima }.toIntArray()

            val almacenActual = almacenManager.obtenerAlmacenActual()
            val indiceInicial = if (almacenActual != null && almacenesNombres.contains(almacenActual)) {
                almacenesNombres.indexOf(almacenActual)
            } else 0

            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Seleccionar Almacén")
                .setSingleChoiceItems(almacenesNombres, indiceInicial) { dialog, which ->
                    almacenSeleccionado = almacenesNombres[which]
                }
                .setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    if (almacenSeleccionado.isNotEmpty()) {
                        almacenManager.establecerAlmacenActual(almacenSeleccionado)
                        val index = almacenesNombres.indexOf(almacenSeleccionado)
                        val capacidad = almacenesCapacidades[index]
                        Toast.makeText(
                            this,
                            "Almacén: $almacenSeleccionado — Capacidad: $capacidad posiciones",
                            Toast.LENGTH_LONG
                        ).show()
                        mostrarDialogoModoEscaneo()
                    } else {
                        Toast.makeText(
                            this,
                            "Debe seleccionar un almacén",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("Cancelar", null)

            val dialog = builder.create()
            dialog.show()
        } catch (e: Exception) {
            mostrarError("No se pudieron cargar los almacenes. Inténtalo de nuevo.", e)
        }
    }

    private fun mostrarDialogoModoEscaneo() {
        try {
            val modos = arrayOf("Manual", "Pallet", "Rumba")
            var modoSeleccionadoIndex = 0

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar Modo de Escaneo")
                .setSingleChoiceItems(modos, 0) { dialog, which ->
                    modoSeleccionadoIndex = which
                }
                .setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    when (modoSeleccionadoIndex) {
                        0 -> seleccionarModo("manual")
                        1 -> seleccionarModo("pallet")
                        2 -> seleccionarModo("rumba")
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            mostrarError("No se pudo cambiar el modo de escaneo.", e)
        }
    }

    private fun seleccionarModo(tipo: String) {
        pendingScanType = tipo
        composeViewModel.handleEvent(com.joseluiscruz.capturainventario.ui.inventario.state.InventarioEvent.OnScanModeSelected(com.joseluiscruz.capturainventario.domain.model.ScanMode.fromString(tipo)))

        when (tipo) {
            "manual" -> {
                Toast.makeText(
                    this,
                    "Modo MANUAL — Almacén: $almacenSeleccionado — Ingrese los datos manualmente",
                    Toast.LENGTH_LONG
                ).show()
            }
            "pallet" -> {
                mostrarDialogoTipoTarima()
            }
            "rumba" -> {
                verificarPermisosYEscaneo()
            }
        }
    }

    private fun mostrarDialogoTipoTarima() {
        try {
            // Misma fuente que el diálogo Compose de InventarioScreen.
            val tiposTarima = TipoTarima.etiquetas.toTypedArray()
            var tipoSeleccionado = tiposTarima[0]

            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Selecciona el tipo de tarima")
                .setMessage("Almacén: $almacenSeleccionado")
                .setSingleChoiceItems(tiposTarima, 0) { _, which ->
                    tipoSeleccionado = tiposTarima[which]
                }
                .setPositiveButton("Aceptar") { dialog, _ ->
                    dialog.dismiss()
                    composeViewModel.handleEvent(com.joseluiscruz.capturainventario.ui.inventario.state.InventarioEvent.OnTipoTarimaSelected(tipoSeleccionado))
                    verificarPermisosYEscaneo()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        } catch (e: Exception) {
            mostrarError("No se pudo seleccionar el tipo de tarima.", e)
        }
    }

    private fun verificarPermisosYEscaneo() {
        if (isScannerLaunching) return
        isScannerLaunching = true

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

        if (resultCode == ConnectionResult.SUCCESS) {
            iniciarEscaneoPremium()
        } else {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    iniciarEscaneoQR()
                }
                else -> {
                    isScannerLaunching = false
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    private fun iniciarEscaneoPremium() {
        gmsScanner.startScan()
            .addOnSuccessListener { barcode: Barcode ->
                isScannerLaunching = false
                onScannerOutcome(ScannerOutcome.fromMlKit(barcode.rawValue))
            }
            .addOnCanceledListener {
                isScannerLaunching = false
                onScannerOutcome(ScannerOutcome.Cancelled(ScannerEngine.ML_KIT))
            }
            .addOnFailureListener { e: Exception ->
                isScannerLaunching = false
                onScannerOutcome(ScannerOutcome.Failed(ScannerEngine.ML_KIT, e, willFallback = true))
                iniciarEscaneoQR()
            }
    }

    private fun iniciarEscaneoQR() {
        try {
            isScannerLaunching = true
            val options = ScanOptions().apply {
                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                setPrompt("$almacenSeleccionado — Enfoque el código QR para capturar")
                setCameraId(0)
                // El beep propio de ZXing se desactiva: el feedback (tono distinto para éxito/error)
                // lo da ScanFeedback igual que con ML Kit, tras interpretar el QR.
                setBeepEnabled(false)
                setBarcodeImageEnabled(true)
                setOrientationLocked(true)
                setCaptureActivity(CaptureActivity::class.java)
            }
            scanLauncher.launch(options)
        } catch (e: Exception) {
            isScannerLaunching = false
            onScannerOutcome(ScannerOutcome.Failed(ScannerEngine.ZXING, e, willFallback = false))
        }
    }

    private fun guardarMaterialConValidacion(sku: String, ubicacion: String, pallets: String, restos: String) {
        try {
            if (sku.isBlank()) {
                Toast.makeText(this, "El SKU es obligatorio", Toast.LENGTH_SHORT).show()
                return
            }
            if (pallets.isNotEmpty() && pallets.toIntOrNull() == null) {
                Toast.makeText(this, "Total Pallets debe ser un número", Toast.LENGTH_SHORT).show()
                return
            }
            Toast.makeText(
                this,
                "Material listo: Almacén $almacenSeleccionado | SKU $sku | Ubicación $ubicacion | Pallets ${pallets.ifEmpty { "1" }} | Restos $restos",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            mostrarError("No se pudo preparar el material.", e)
        }
    }

    private fun limpiarCampos() {
        Toast.makeText(this, "Campos limpiados", Toast.LENGTH_SHORT).show()
    }

    /**
     * Abre el selector de destino del CSV de respaldo. La exportación en sí (registros de Room)
     * la hace el ViewModel al recibir la URI; cualquier fallo se le reporta para que muestre
     * "Reintentar" y, si venía de "Nueva toma", vuelva a abrir la confirmación.
     */
    private fun exportarInventario() {
        try {
            val fileName = "inventario_${System.currentTimeMillis()}.csv"
            exportLauncher.launch(fileName)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo abrir el selector de archivo para exportar", e)
            composeViewModel.handleEvent(
                InventarioEvent.OnExportacionCsvTerminada(ResultadoExportacion.Fallida(sinDatos = false))
            )
        }
    }

    private fun enviarPorCorreo() {
        try {
            val intent = Intent(this, InventarioResumenComposeActivity::class.java)
            intent.putExtra("enviar_por_correo", true)
            startActivity(intent)
        } catch (e: Exception) {
            mostrarError("No se pudo abrir el envío por correo.", e)
        }
    }

    private fun irResumen() {
        try {
            val intent = Intent(this, InventarioResumenComposeActivity::class.java)
            intent.putExtra("mostrarGrafica", true)
            startActivity(intent)
        } catch (e: Exception) {
            mostrarError("No se pudo abrir el resumen.", e)
        }
    }

    private fun cargarInventario() {
        try {
            importLauncher.launch("text/csv")
        } catch (e: Exception) {
            mostrarError("No se pudo abrir el selector de archivos.", e)
        }
    }

    private fun cargarInventario(uri: Uri) {
        lifecycleScope.launch {
            val result = csvService.importarCsv(uri)
            val message = if (result is com.joseluiscruz.capturainventario.domain.model.OperationResult.Error) result.message else "Importación exitosa"
            Toast.makeText(this@InventarioComposeActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun irRestos() {
        try {
            val intent = Intent(this, RestosActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            mostrarError("No se pudo abrir Restos.", e)
        }
    }

    /** Mensaje claro al operador; el detalle técnico solo va al log. */
    private fun mostrarError(mensaje: String, e: Throwable? = null) {
        if (e != null) Log.e(TAG, mensaje, e) else Log.w(TAG, mensaje)
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
    }

    private companion object {
        const val TAG = "InventarioActivity"
    }
}
