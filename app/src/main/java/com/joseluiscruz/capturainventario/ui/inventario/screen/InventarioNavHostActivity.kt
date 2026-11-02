package com.joseluiscruz.capturainventario.ui.inventario.screen

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.joseluiscruz.capturainventario.ui.theme.EscaneoDeMaterialesDemoTheme

/**
 * Actividad principal de navegación para el módulo de inventario.
 * Implementa SharedTransitionLayout para permitir transiciones compartidas fluidas
 * entre la lista de materiales y los detalles.
 */
class InventarioNavHostActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EscaneoDeMaterialesDemoTheme {
                InventarioNavigation()
            }
        }
    }
}

/**
 * Navegación principal con SharedTransitionLayout.
 * Este layout permite que los elementos "vuelen" entre pantallas.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InventarioNavigation() {
    val navController = rememberNavController()
    val activity = androidx.compose.ui.platform.LocalContext.current as? ComponentActivity

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "materiales_lista"
        ) {
            composable("materiales_lista") {
                MaterialesListaScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBack = { activity?.finish() },
                    onMaterialClick = { transitionKey ->
                        navController.navigate("detalle_material/${Uri.encode(transitionKey)}")
                    }
                )
            }

            composable(
                route = "detalle_material/{materialKey}",
                arguments = listOf(
                    navArgument("materialKey") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val materialKey = backStackEntry.arguments
                    ?.getString("materialKey")
                    ?.let(Uri::decode)
                    .orEmpty()

                DetalleMaterialScreen(
                    materialKey = materialKey,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@composable,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
