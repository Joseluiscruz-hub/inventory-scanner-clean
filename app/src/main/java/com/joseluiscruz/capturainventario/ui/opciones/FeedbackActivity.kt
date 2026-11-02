package com.joseluiscruz.capturainventario.ui.opciones

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joseluiscruz.capturainventario.ui.theme.PremiumTheme
import com.joseluiscruz.capturainventario.ui.theme.BrandColors

class FeedbackActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PremiumTheme {
                FeedbackScreen(
                    onBack = { finish() },
                    onSend = { feedback ->
                        Toast.makeText(this, "Feedback enviado: $feedback", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(onBack: () -> Unit, onSend: (String) -> Unit) {
    var feedback by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enviar Feedback", color = BrandColors.BrandWhite, fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Tu opinión es importante para nosotros. Cuéntanos qué podemos mejorar.",
                color = BrandColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = feedback,
                onValueChange = { feedback = it },
                label = { Text("Comentarios") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandColors.BrandRed,
                    focusedLabelColor = BrandColors.BrandRed
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onSend(feedback) },
                modifier = Modifier.fillMaxWidth(),
                enabled = feedback.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandColors.BrandRed)
            ) {
                Text("ENVIAR", color = BrandColors.BrandWhite)
            }
        }
    }
}
