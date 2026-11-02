package com.joseluiscruz.capturainventario.ui.opciones

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.viewmodel.InventarioSharedViewModel
import com.joseluiscruz.capturainventario.ui.theme.BrandColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasOpcionesScreen(viewModel: InventarioSharedViewModel) {
    val context = LocalContext.current

    val alphaAnim = remember { Animatable(0f) }
    val contentOffsetAnim = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { alphaAnim.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
        launch { contentOffsetAnim.animateTo(0f, tween(400, easing = FastOutSlowInEasing)) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mas Opciones",
                        color = BrandColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColors.Surface1,
                    titleContentColor = BrandColors.TextPrimary,
                    navigationIconContentColor = BrandColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BrandColors.TextPrimary
                        )
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .graphicsLayer(
                        alpha = alphaAnim.value,
                        translationY = contentOffsetAnim.value
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
            ) {
                item {
                    SmartSyncCard(viewModel)
                }

                item { SectionHeader("Acerca de la Aplicacion") }
                item {
                    OptionCard(
                        title = "Informacion del Sistema",
                        subtitle = "Version v${com.joseluiscruz.capturainventario.BuildConfig.VERSION_NAME} · PLANTA DE MANUFACTURA",
                        icon = Icons.Default.Info,
                        iconColor = BrandColors.BrandRed
                    ) {
                        showAboutDialog(context)
                    }
                }

                item { SectionHeader("Ayuda y Soporte") }
                item {
                    OptionCard(
                        title = "Centro de Ayuda",
                        subtitle = "Guias de usuario y tutoriales",
                        icon = Icons.Default.Help,
                        iconColor = BrandColors.SyncBlue
                    ) {
                        context.startActivity(Intent(context, HelpActivity::class.java))
                    }
                }
                item {
                    OptionCard(
                        title = "Contactar Soporte",
                        subtitle = "Enviar un correo a soporte tecnico",
                        icon = Icons.Default.Email,
                        iconColor = BrandColors.SuccessGreen
                    ) {
                        sendSupportEmail(context)
                    }
                }
                item {
                    OptionCard(
                        title = "Reportar Error / Feedback",
                        subtitle = "Enviar detalles si algo fallo o sugerencias",
                        icon = Icons.Default.BugReport,
                        iconColor = BrandColors.WarningAmber
                    ) {
                        context.startActivity(Intent(context, FeedbackActivity::class.java))
                    }
                }
                item {
                    OptionCard(
                        title = "Enviar Inventario",
                        subtitle = "Enviar el inventario por correo electronico",
                        icon = Icons.Default.Send,
                        iconColor = BrandColors.SyncBlue
                    ) {
                        sendInventoryEmail(context)
                    }
                }

                item { SectionHeader("Gestion de Catalogos") }
                item {
                    OptionCard(
                        title = "Catalogos por Planta",
                        subtitle = "Configurar estibaje y TPM por ubicacion",
                        icon = Icons.Default.Storefront,
                        iconColor = BrandColors.BrandRed
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                com.joseluiscruz.capturainventario.ui.inventario.screen.CatalogoPlantaActivity::class.java
                            )
                        )
                    }
                }

                item { SectionHeader("Configuracion Rapida") }
                item {
                    OptionCard(
                        title = "Preferencias de Interfaz",
                        subtitle = "Tema, idioma y modo de captura",
                        icon = Icons.Default.Settings,
                        iconColor = BrandColors.TextPrimary
                    ) {
                        context.startActivity(
                            Intent(
                                context,
                                com.joseluiscruz.capturainventario.ui.config.ConfiguracionActivity::class.java
                            )
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        text = "(c) 2026 PLANTA DE MANUFACTURA · Operaciones Mexico\nTodos los derechos reservados.",
                        fontSize = 11.sp,
                        color = BrandColors.TextHint,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 16.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
