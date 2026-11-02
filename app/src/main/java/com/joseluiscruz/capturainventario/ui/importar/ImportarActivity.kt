package com.joseluiscruz.capturainventario.ui.importar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.util.SapPortalUrlValidator
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

enum class ImportTarget {
    INVENTARIO,
    TARIMAS,
    SISTEMA
}

@AndroidEntryPoint
class ImportarActivity : ComponentActivity() {

    companion object {
        private const val SAP_PORTAL_PREFERENCES = "sap_portal_settings"
        private const val SAP_PORTAL_URL_KEY = "sap_download_url"
    }

    private val csvService by lazy { CsvInventarioService(this) }
    private val sharedViewModel: InventarioSharedViewModel by viewModels()
    private val sapPortalPreferences by lazy {
        getSharedPreferences(SAP_PORTAL_PREFERENCES, MODE_PRIVATE)
    }

    private var pendingTarget: ImportTarget = ImportTarget.INVENTARIO
    private var importStatusMessage = mutableStateOf("Seleccione un archivo para importar")
    private var sapPortalUrl = mutableStateOf("")

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { handleImportFile(it, pendingTarget) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sapPortalUrl.value = sapPortalPreferences.getString(SAP_PORTAL_URL_KEY, "").orEmpty()
        setContent {
            PremiumTheme {
                ImportarScreen(
                    statusMessage = importStatusMessage.value,
                    sapPortalUrl = sapPortalUrl.value,
                    onSapPortalUrlChange = { sapPortalUrl.value = it },
                    onSaveSapPortalUrl = { saveSapPortalUrl() },
                    onOpenSapPortal = { openSapPortal() },
                    onImportClick = { target -> 
                        pendingTarget = target
                        importLauncher.launch("text/csv") 
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun saveSapPortalUrl(): Boolean {
        val normalized = SapPortalUrlValidator.normalize(sapPortalUrl.value)
        if (normalized == null) {
            Toast.makeText(
                this,
                "Escribe una liga HTTPS válida del portal SAP",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        sapPortalUrl.value = normalized
        sapPortalPreferences.edit().putString(SAP_PORTAL_URL_KEY, normalized).apply()
        Toast.makeText(this, "Liga SAP guardada en este dispositivo", Toast.LENGTH_SHORT).show()
        return true
    }

    private fun openSapPortal() {
        val normalized = SapPortalUrlValidator.normalize(sapPortalUrl.value)
        if (normalized == null) {
            Toast.makeText(
                this,
                "Configura primero la liga HTTPS proporcionada por tu empresa",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        sapPortalUrl.value = normalized
        sapPortalPreferences.edit().putString(SAP_PORTAL_URL_KEY, normalized).apply()
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
        }.onFailure {
            Toast.makeText(
                this,
                "No hay una aplicación disponible para abrir la liga SAP",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun handleImportFile(uri: Uri, target: ImportTarget) {
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            importStatusMessage.value = "Error: No se pudo acceder al archivo"
            Toast.makeText(this, "Error: No se pudo acceder al archivo", Toast.LENGTH_SHORT).show()
            return
        }
        inputStream.close()

        when (target) {
            ImportTarget.INVENTARIO -> {
                importStatusMessage.value = "Importando inventario local..."
                lifecycleScope.launch {
                    val result = csvService.importarCsv(uri)
                    if (result.isSuccess) {
                        val msg = "Inventario importado correctamente. Registros: ${result.getOrNull()}"
                        importStatusMessage.value = msg
                        Toast.makeText(this@ImportarActivity, msg, Toast.LENGTH_SHORT).show()
                    } else {
                        val error = result as com.joseluiscruz.capturainventario.domain.model.OperationResult.Error
                        val msg = "Error al importar: ${error.message}"
                        importStatusMessage.value = msg
                        Toast.makeText(this@ImportarActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }

            ImportTarget.TARIMAS, ImportTarget.SISTEMA -> {
                importStatusMessage.value = if (target == ImportTarget.TARIMAS) {
                    "Importando base de tarimas y conversiones..."
                } else {
                    "Importando datos de referencia del sistema..."
                }
                
                sharedViewModel.importarInventarioReferenciaCsv(uri, contentResolver) { success, message ->
                    importStatusMessage.value = message
                    Toast.makeText(this, message, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportarScreen(
    statusMessage: String,
    sapPortalUrl: String,
    onSapPortalUrlChange: (String) -> Unit,
    onSaveSapPortalUrl: () -> Unit,
    onOpenSapPortal: () -> Unit,
    onImportClick: (ImportTarget) -> Unit,
    onBack: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Datos", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrandColors.BrandWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandColors.BrandRed)
            )
        },
        containerColor = BrandColors.Surface0
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            BrandWaves(
                modifier = Modifier.align(Alignment.BottomCenter).height(150.dp),
                waveHeight = 0.3f,
                opacityBack = 0.05f,
                opacityFront = 0.1f
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
                    .graphicsLayer(alpha = alphaAnim.value),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                if (statusMessage.isNotBlank() && statusMessage != "Seleccione un archivo para importar") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BrandColors.BrandRed.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudDownload, null, tint = BrandColors.BrandRed)
                            Spacer(Modifier.width(12.dp))
                            Text(statusMessage, fontWeight = FontWeight.Bold, color = BrandColors.BrandRed, fontSize = 13.sp)
                        }
                    }
                }

                SapPortalCard(
                    url = sapPortalUrl,
                    onUrlChange = onSapPortalUrlChange,
                    onSave = onSaveSapPortalUrl,
                    onOpen = onOpenSapPortal
                )

                ImportOptionCardPremium(
                    title = "Importar Inventario",
                    description = "Importa datos de inventario desde un archivo CSV. Conserva el flujo actual de captura sin reemplazarlo.",
                    icon = Icons.Default.Inventory,
                    onClick = { onImportClick(ImportTarget.INVENTARIO) }
                )

                ImportOptionCardPremium(
                    title = "Importar Tarimas",
                    description = "Importa la base de tarimas, armado y conversión para enriquecer la validación de pallets y cajas.",
                    icon = Icons.Default.TableChart,
                    onClick = { onImportClick(ImportTarget.TARIMAS) }
                )

                ImportOptionCardPremium(
                    title = "Importar Referencia SAP",
                    description = "Selecciona el CSV descargado del portal SAP para comparar el inventario físico contra el inventario del sistema.",
                    icon = Icons.Default.Settings,
                    onClick = { onImportClick(ImportTarget.SISTEMA) }
                )

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun SapPortalCard(
    url: String,
    onUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandColors.BrandRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = BrandColors.BrandRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "1. Descargar archivo SAP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = BrandColors.TextPrimary
                    )
                    Text(
                        "La liga interna se configura una sola vez por dispositivo.",
                        color = BrandColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Liga del portal SAP") },
                placeholder = { Text("https://portal.empresa...") },
                supportingText = { Text("Por seguridad sólo se permiten ligas HTTPS") },
                singleLine = true
            )

            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Icon(Icons.Default.CloudDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("ABRIR PORTAL SAP", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GUARDAR LIGA", fontWeight = FontWeight.Bold)
            }

            Text(
                "Después de descargar el CSV, usa “Importar Referencia SAP” para activar la comparación.",
                color = BrandColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun ImportOptionCardPremium(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandColors.BrandRed.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = BrandColors.BrandRed, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(16.dp))
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = BrandColors.TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text(description, color = BrandColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Text("Seleccionar Archivo CSV", fontWeight = FontWeight.Bold, color = BrandColors.BrandWhite)
            }
        }
    }
}
