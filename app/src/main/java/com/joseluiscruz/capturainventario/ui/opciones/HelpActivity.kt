package com.joseluiscruz.capturainventario.ui.opciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

class HelpActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme {
                HelpScreen(onBack = { finish() })
            }
        }
    }
}

data class HelpItem(val question: String, val answer: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val helpItems = listOf(
        HelpItem(
            "¿Cómo capturar un material?",
            "Ve a la sección 'Capturar', selecciona el modo de escaneo (Rumba, Pallet o Manual) y usa la cámara para escanear el código QR o ingresa el SKU manualmente."
        ),
        HelpItem(
            "¿Qué es el modo Rumba?",
            "Permite escanear un SKU y luego ingresar manualmente la cantidad total de pallets de esa rumba."
        ),
        HelpItem(
            "¿Cómo hacer un traspaso?",
            "Selecciona 'Traspasos', elige el SKU, el almacén de origen, el de destino y la cantidad a mover."
        ),
        HelpItem(
            "¿Cómo ver las diferencias de inventario?",
            "Usa la sección 'Comparar' para ver en tiempo real las diferencias entre lo escaneado y lo que el sistema espera."
        ),
        HelpItem(
            "¿Cómo exportar reportes?",
            "En la sección 'Resumen' o 'Exportar', puedes generar archivos CSV o reportes en formato PDF."
        ),
        HelpItem(
            "Sincronización con la nube",
            "La app guarda los datos localmente y los sincroniza automáticamente con Firebase cuando hay conexión a internet."
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Ayuda", color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandColors.BrandRed),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = BrandColors.BrandWhite
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(helpItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BrandColors.Surface1)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = item.question,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = BrandColors.BrandRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = item.answer, fontSize = 14.sp, color = BrandColors.TextPrimary)
                    }
                }
            }
        }
    }
}
