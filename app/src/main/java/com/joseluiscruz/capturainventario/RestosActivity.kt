package com.joseluiscruz.capturainventario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.manager.AlmacenCapacidadManager
import com.joseluiscruz.capturainventario.model.MaterialItem
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

/**
 * Actividad para gestionar restos de materiales (cajas sueltas).
 * Migrada a Jetpack Compose con diseño Premium.
 */
class RestosActivity : ComponentActivity() {
    private val csvService by lazy { com.joseluiscruz.capturainventario.data.repository.CsvInventarioService(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme {
                RestosScreen(csvService)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestosScreen(csvService: com.joseluiscruz.capturainventario.data.repository.CsvInventarioService) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sku by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var acumulado by remember { mutableStateOf(0) }

    val scanLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result?.contents != null) {
            sku = result.contents
            Toast.makeText(context, "Código leído: $sku", Toast.LENGTH_SHORT).show()
        }
    }

    fun cargarAcumulado() {
        scope.launch {
            val result = csvService.obtenerMaterialesEscaneados()
            val materiales = result.getOrNull() ?: emptyList()
            acumulado = materiales.filter { it.tipoTarima == "RESTO" }
                .sumOf { it.totalPallets?.toIntOrNull() ?: 0 }
        }
    }

    LaunchedEffect(Unit) {
        cargarAcumulado()
    }

    val alphaAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Restos", fontWeight = FontWeight.ExtraBold, color = BrandColors.BrandWhite) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.BrandRed,
                    titleContentColor = BrandColors.BrandWhite,
                    navigationIconContentColor = BrandColors.BrandWhite
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = BrandColors.BrandWhite)
                    }
                }
            )
        },
        containerColor = BrandColors.Surface0
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            BrandWaves(
                modifier = Modifier.align(Alignment.BottomCenter).height(180.dp),
                waveHeight = 0.5f, opacityBack = 0.1f, opacityFront = 0.2f
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(20.dp).graphicsLayer(alpha = alphaAnim.value),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandColors.BrandBlack),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Acumulado", color = BrandColors.BrandWhite.copy(alpha = 0.7f), fontSize = 14.sp)
                            Text(
                                "$acumulado CAJAS",
                                color = BrandColors.GoldAccent,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedTextField(
                                value = sku,
                                onValueChange = { sku = it },
                                label = { Text("SKU del Material") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    IconButton(onClick = {
                                        val options = ScanOptions()
                                        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                        options.setPrompt("Escanea el código de la caja")
                                        scanLauncher.launch(options)
                                    }) {
                                        Icon(Icons.Default.QrCodeScanner, null, tint = BrandColors.BrandRed)
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = BrandColors.BrandRed,
                                    unfocusedIndicatorColor = BrandColors.Surface3,
                                    focusedTextColor = BrandColors.TextPrimary,
                                    unfocusedTextColor = BrandColors.TextPrimary,
                                    cursorColor = BrandColors.BrandRed,
                                    focusedLabelColor = BrandColors.BrandRed,
                                    unfocusedLabelColor = BrandColors.TextSecondary
                                )
                            )

                            OutlinedTextField(
                                value = cantidad,
                                onValueChange = { if (it.all { c -> c.isDigit() }) cantidad = it },
                                label = { Text("Cantidad de Cajas") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = BrandColors.BrandRed,
                                    unfocusedIndicatorColor = BrandColors.Surface3,
                                    focusedTextColor = BrandColors.TextPrimary,
                                    unfocusedTextColor = BrandColors.TextPrimary,
                                    cursorColor = BrandColors.BrandRed,
                                    focusedLabelColor = BrandColors.BrandRed,
                                    unfocusedLabelColor = BrandColors.TextSecondary
                                )
                            )

                            Button(
                                onClick = {
                                    if (sku.isBlank()) {
                                        Toast.makeText(context, "Ingrese SKU", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val cant = cantidad.toIntOrNull() ?: 1
                                    val almacenManager = AlmacenCapacidadManager(context)
                                    val material = MaterialItem(
                                        sku = sku, descripcion = "", cxPal = "", fpc = "", con = "",
                                        centro = "", linea = "", op = "", fProd = "", diasV = "",
                                        ubicacion = "", totalPallets = cant.toString(),
                                        restos = "SI", tipoTarima = "RESTO",
                                        almacen = almacenManager.obtenerAlmacenActual() ?: ""
                                    )
                                    scope.launch {
                                        val result = csvService.guardarMaterial(material)
                                        if (result.isSuccess) {
                                            sku = ""; cantidad = ""
                                            cargarAcumulado()
                                            Toast.makeText(context, "Guardado con éxito", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val msg = if (result is com.joseluiscruz.capturainventario.domain.model.OperationResult.Error) result.message else "Error al guardar"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                            ) {
                                Icon(Icons.Default.Save, null, tint = BrandColors.BrandWhite)
                                Spacer(Modifier.width(8.dp))
                                Text("GUARDAR RESTOS", color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseCsvLineCompat(line: String): List<String> {
    val result = mutableListOf<String>()
    var current = StringBuilder()
    var inQuotes = false
    for (i in line.indices) {
        val c = line[i]
        when {
            c == '"' && inQuotes && i + 1 < line.length && line[i + 1] == '"' -> {
                current.append('"')
            }

            c == '"' -> {
                inQuotes = !inQuotes
            }

            c == ',' && !inQuotes -> {
                result.add(current.toString())
                current = StringBuilder()
            }

            else -> current.append(c)
        }
    }
    result.add(current.toString())
    return result
}
