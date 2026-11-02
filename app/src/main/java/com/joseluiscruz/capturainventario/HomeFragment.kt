package com.joseluiscruz.capturainventario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.joseluiscruz.capturainventario.ui.home.util.TextAnimator
import com.joseluiscruz.capturainventario.ui.importar.ImportarActivity
import com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioComposeActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * @deprecated Reemplazado por [com.joseluiscruz.capturainventario.ui.main.MainComposeActivity].
 * Este fragmento se mantiene temporalmente por compatibilidad con nav_graph.xml.
 * Eliminar cuando la navegación legacy se desactive completamente.
 */
@Deprecated(
    message = "Reemplazado por MainComposeActivity. Eliminar junto con nav_graph.xml legacy.",
    level = DeprecationLevel.WARNING
)
class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Cambiar a fragment_home_new
        return inflater.inflate(R.layout.fragment_home_new, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar animaciones y frases motivacionales
        setupAnimations(view)
        setupMotivationalPhrase(view)
        setupButtons(view) // Activar los botones principales
        setupDrawer(view) // Nueva función para la barra lateral
    }

    private fun setupDrawer(view: View) {
        val activity = requireActivity()
        val drawerLayout = activity.findViewById<androidx.drawerlayout.widget.DrawerLayout?>(R.id.drawerLayout)
        val navView = activity.findViewById<com.google.android.material.navigation.NavigationView?>(R.id.navView)
        // Listener para el menú lateral
        navView?.setNavigationItemSelectedListener { menuItem ->
            drawerLayout?.closeDrawers()
            when (menuItem.itemId) {
                R.id.nav_inicio -> {
                    // Ir a HomeFragment
                    // Ya estás en Home, puedes mostrar un mensaje o refrescar
                    Toast.makeText(requireContext(), "Pantalla de inicio", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_capturar_inventario -> {
                    // Ir directo a escaneo de pallet automático
                    val intent = Intent(requireContext(), InventarioComposeActivity::class.java)
                    intent.putExtra("tipo_escaneo", "pallet")
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_dashboard -> {
                    // Ir al Dashboard Ejecutivo
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.dashboard.DashboardComposeActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_comparar_inventarios -> {
                    // Ir a la comparación de inventarios
                    val intent = Intent(requireContext(), InventoryComparisonActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_comparacion_tiempo_real -> {
                    // Ir a la pantalla de comparación en tiempo real
                    val intent = Intent(
                        requireContext(),
                        com.joseluiscruz.capturainventario.ui.comparacion.ComparacionTiempoRealActivity::class.java
                    )
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_traspasos -> {
                    // Ir a la pantalla de traspasos
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.TraspasoActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_gestion_almacenes -> {
                    // Ir a la pantalla de gestión de almacenes
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.almacenes.GestionAlmacenesActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_ajuste_inventario -> {
                    mostrarDialogoContrasena()
                }

                R.id.nav_importar -> {
                    // Ir a la pantalla de importar
                    val intent = Intent(requireContext(), ImportarActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_exportar -> {
                    // Ir a la pantalla de exportar
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.exportar.ExportarActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_dashboard_comparacion -> {
                    // Ir a la pantalla de dashboard comparación
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.dashboard.DashboardComparacionActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_centro_ayuda -> {
                    // Ir al centro de ayuda
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.opciones.HelpActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_feedback -> {
                    // Ir a la pantalla de feedback
                    val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.opciones.FeedbackActivity::class.java)
                    startActivity(intent)
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }

                R.id.nav_gestion_reinicio -> {
                    mostrarDialogoBorrarInventarios()
                }

                R.id.nav_mas_opciones -> {
                    btnMasOpcionesClick()
                }
            }
            true
        }
    }

    private fun btnMasOpcionesClick() {
        val opciones = arrayOf(
            getString(R.string.configuracion),
            getString(R.string.borrar_inventarios),
            getString(R.string.resumen),
            getString(R.string.importar),
            getString(R.string.exportar),
            "Traspasos",
            "Dashboard Comparación",
            "Centro de Ayuda",
            "Feedback"
        )
        MaterialAlertDialogBuilder(requireContext(), R.style.BrandDialog)
            .setTitle(getString(R.string.opciones))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> Toast.makeText(requireContext(), getString(R.string.configuracion), Toast.LENGTH_SHORT).show()
                    1 -> mostrarDialogoBorrarInventarios()
                    2 -> {
                        startActivity(
                            Intent(
                                requireContext(),
                                com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioResumenComposeActivity::class.java
                            )
                        )
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    3 -> {
                        startActivity(Intent(requireContext(), ImportarActivity::class.java))
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    4 -> {
                        val intent = Intent(requireContext(), InventarioComposeActivity::class.java)
                        intent.putExtra("enviar_por_correo", true)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    5 -> {
                        // Traspasos
                        val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.TraspasoActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    6 -> {
                        // Dashboard Comparación
                        val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.dashboard.DashboardComparacionActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    7 -> {
                        // Centro de Ayuda
                        val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.opciones.HelpActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                    8 -> {
                        // Feedback
                        val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.opciones.FeedbackActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
            .show()
    }

    /**
     * Configura las animaciones del logo y fondo
     */
    private fun setupAnimations(view: View) {
        // Animar el logo estático con un efecto de pulso continuo
        val logoIntro = view.findViewById<ImageView>(R.id.logoIntro)
        TextAnimator.animatePulse(logoIntro)
        
        // Fondo animado opcional
        val particleBackground = view.findViewById<LottieAnimationView>(R.id.particleBackground)
        particleBackground?.playAnimation()
    }

    /**
     * Configura la frase motivacional con efecto de escritura
     */
    private fun setupMotivationalPhrase(view: View) {
        // Obtener una frase aleatoria y mostrarla con efecto de escritura
        val txtFrase = view.findViewById<TextView>(R.id.txtFrase)
        txtFrase?.text = com.joseluiscruz.capturainventario.ui.home.util.FrasesMotivacionales.random()
    }

    /**
     * Configura los botones y sus animaciones
     */
    private fun setupButtons(view: View) {
        val btnCapturar = view.findViewById<Button>(R.id.btnCapturar)
        val btnDashboard = view.findViewById<Button>(R.id.btnDashboard)
        val btnComparacionTiempoReal = view.findViewById<Button>(R.id.btnComparacionTiempoReal)
        val btnGestionAlmacenes = view.findViewById<Button>(R.id.btnGestionAlmacenes)
        val btnConfiguracion = view.findViewById<Button>(R.id.btnConfiguracion)
        val btnMasOpciones = view.findViewById<Button>(R.id.btnMasOpciones)

        // Animar todos los botones con efecto de entrada
        TextAnimator.animarBotones(
            btnCapturar,
            btnDashboard,
            btnComparacionTiempoReal,
            btnGestionAlmacenes,
            btnConfiguracion,
            btnMasOpciones
        )

        // Configurar listeners para los botones
        btnCapturar.setOnClickListener {
            // Ir directo a escaneo de pallet automático
            val intent = Intent(requireContext(), InventarioComposeActivity::class.java)
            intent.putExtra("tipo_escaneo", "pallet")
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnDashboard.setOnClickListener {
            // Ir al Dashboard Ejecutivo
            val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.dashboard.DashboardComposeActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnComparacionTiempoReal.setOnClickListener {
            // Ir a la pantalla de comparación en tiempo real
            val intent = Intent(
                requireContext(),
                com.joseluiscruz.capturainventario.ui.comparacion.ComparacionTiempoRealActivity::class.java
            )
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnGestionAlmacenes.setOnClickListener {
            // Ir a la pantalla de gestión de almacenes
            val intent = Intent(requireContext(), com.joseluiscruz.capturainventario.ui.almacenes.GestionAlmacenesActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnConfiguracion.setOnClickListener {
            mostrarDialogoContrasena()
        }

        btnMasOpciones.setOnClickListener {
            btnMasOpcionesClick()
        }
    }

    private fun mostrarDialogoContrasena() {
        if (!com.joseluiscruz.capturainventario.manager.AuthManager.isAdmin()) {
            Toast.makeText(
                requireContext(),
                "Acceso restringido a administradores",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        startActivity(Intent(requireContext(), AjusteInventarioActivity::class.java))
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun mostrarDialogoBorrarInventarios() {
        if (!com.joseluiscruz.capturainventario.manager.AuthManager.isAdmin()) {
            Toast.makeText(
                requireContext(),
                "Acceso restringido a administradores",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        startActivity(
            Intent(
                requireContext(),
                com.joseluiscruz.capturainventario.ui.reinicio.GestionReinicioActivity::class.java
            )
        )
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun mostrarOpcionesDashboard() {
        val opciones = arrayOf(
            getString(R.string.comparar_inventarios),
            getString(R.string.ajustes_inventario),
            getString(R.string.resumen)
        )
        MaterialAlertDialogBuilder(requireContext(), R.style.BrandDialog)
            .setTitle(getString(R.string.opciones_dashboard))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        // Ir a la actividad de comparación de inventarios
                        val intent = Intent(requireContext(), InventoryComparisonActivity::class.java)
                        startActivity(intent)
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }

                    1 -> {
                        // Mostrar diálogo de contraseña para ajustes de inventario
                        mostrarDialogoContrasena()
                    }

                    2 -> {
                        // Ir a la actividad de resumen de inventario
                        startActivity(
                            Intent(
                                requireContext(),
                                com.joseluiscruz.capturainventario.ui.inventario.screen.InventarioResumenComposeActivity::class.java
                            )
                        )
                        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    }
                }
            }
            .show()
    }
}
