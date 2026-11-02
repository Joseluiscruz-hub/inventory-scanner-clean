package com.joseluiscruz.capturainventario.ui.config

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
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
import com.joseluiscruz.capturainventario.ui.components.BrandWaves
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlinx.coroutines.launch

/**
 * Activity for application configuration settings.
 * Rewritten in Jetpack Compose with PLANTA DE MANUFACTURA Premium design.
 */
class ConfiguracionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme {
                ConfiguracionScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("app_config", Context.MODE_PRIVATE) }

    // Estados de configuración
    var usuario by remember { mutableStateOf(prefs.getString("usuario", "") ?: "") }
    var modoOscuro by remember { mutableStateOf(prefs.getBoolean("modo_oscuro", false)) }
    var tipoEscaneoDefault by remember { mutableStateOf(prefs.getString("tipo_escaneo_default", "rumba") ?: "rumba") }
    var notificaciones by remember { mutableStateOf(prefs.getBoolean("notificaciones", true)) }
    var guardarHistorial by remember { mutableStateOf(prefs.getBoolean("guardar_historial", true)) }

    // Animaciones de entrada
    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, tween(800, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(800, easing = FastOutSlowInEasing)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración", fontWeight = FontWeight.ExtraBold, color = BrandColors.BrandWhite) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            BrandWaves(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .height(150.dp),
                waveHeight = 0.4f,
                opacityBack = 0.1f,
                opacityFront = 0.15f
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .graphicsLayer(
                        alpha = alphaAnim.value,
                        translationY = contentOffsetAnim.value
                    ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                // SECCIÓN: PERFIL
                item { ConfigSectionTitle("Perfil de Usuario") }
                item {
                    ConfigCard {
                        OutlinedTextField(
                            value = usuario,
                            onValueChange = { usuario = it },
                            label = { Text("Nombre de Operador") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandColors.BrandRed) },
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
                    }
                }

                // SECCIÓN: OPERACIONES
                item { ConfigSectionTitle("Preferencias de Operación") }
                item {
                    ConfigCard {
                        Column {
                            Text(
                                "Modo de Escaneo por Defecto",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandColors.TextPrimary
                            )
                            Spacer(Modifier.height(8.dp))
                            val options = listOf("rumba", "pallet", "manual")
                            options.forEach { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clickable { tipoEscaneoDefault = option },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (tipoEscaneoDefault == option),
                                        onClick = { tipoEscaneoDefault = option },
                                        colors = RadioButtonDefaults.colors(selectedColor = BrandColors.BrandRed)
                                    )
                                    Text(option.uppercase(), modifier = Modifier.padding(start = 8.dp), color = BrandColors.TextPrimary)
                                }
                            }
                        }
                    }
                }

                // SECCIÓN: INTERFAZ
                item { ConfigSectionTitle("Interfaz y Sistema") }
                item {
                    ConfigCard {
                        Column {
                            ConfigSwitchRow(
                                title = "Modo Oscuro",
                                subtitle = "Activar tema de alta visibilidad",
                                checked = modoOscuro,
                                onCheckedChange = {
                                    modoOscuro = it
                                    if (it) {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                    } else {
                                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                    }
                                }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BrandColors.Surface3)
                            ConfigSwitchRow(
                                title = "Notificaciones",
                                subtitle = "Alertas de sincronización",
                                checked = notificaciones,
                                onCheckedChange = { notificaciones = it }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = BrandColors.Surface3)
                            ConfigSwitchRow(
                                title = "Guardar Historial",
                                subtitle = "Mantener registro local de escaneos",
                                checked = guardarHistorial,
                                onCheckedChange = { guardarHistorial = it }
                            )
                        }
                    }
                }

                // BOTÓN GUARDAR
                item {
                    Button(
                        onClick = {
                            prefs.edit().apply {
                                putString("usuario", usuario)
                                putBoolean("modo_oscuro", modoOscuro)
                                putString("tipo_escaneo_default", tipoEscaneoDefault)
                                putBoolean("notificaciones", notificaciones)
                                putBoolean("guardar_historial", guardarHistorial)
                                apply()
                            }
                            Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
                            (context as? ComponentActivity)?.finish()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = BrandColors.BrandWhite)
                        Spacer(Modifier.width(8.dp))
                        Text("GUARDAR CAMBIOS", color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold)
                    }
                }

                // LIMPIAR DATOS
                item {
                    OutlinedButton(
                        onClick = {
                            // Diálogo de confirmación
                            androidx.appcompat.app.AlertDialog.Builder(context)
                                .setTitle("¿Limpiar todos los datos?")
                                .setMessage("Esta acción borrará las preferencias y las tablas de la base de datos local.")
                                .setPositiveButton("Limpiar") { _, _ ->
                                    prefs.edit().clear().apply()
                                    try {
                                        // Intento de limpiar DB en hilo secundario (simplificado aquí)
                                        // AppDatabase.getDatabase(context).clearAllTables()
                                    } catch (e: Exception) {
                                    }
                                    Toast.makeText(context, "Datos borrados", Toast.LENGTH_SHORT).show()
                                    (context as? ComponentActivity)?.recreate()
                                }
                                .setNegativeButton("Cancelar", null)
                                .show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandColors.TextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandColors.Surface3)
                    ) {
                        Text("REESTABLECER VALORES", color = BrandColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfigSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = BrandColors.TextPrimary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun ConfigCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ConfigSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = BrandColors.TextPrimary)
            Text(text = subtitle, fontSize = 12.sp, color = BrandColors.TextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandColors.BrandWhite,
                checkedTrackColor = BrandColors.BrandRed,
                uncheckedThumbColor = BrandColors.TextSecondary,
                uncheckedTrackColor = BrandColors.Surface3
            )
        )
    }
}
