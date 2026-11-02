package com.joseluiscruz.capturainventario.ui.gestion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import com.joseluiscruz.capturainventario.R;
import com.joseluiscruz.capturainventario.viewmodel.InventoryComparisonViewModel;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Actividad para gestionar los diferentes tipos de reinicio del sistema
 */
public class GestionReinicioActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // Vistas del layout
    private TextView tvEstadoSistema;
    private TextView tvEstadoEscaneado;
    private TextView tvEstadoComparacion;
    private TextView tvHistorialReinicios;
    private androidx.cardview.widget.CardView cardReiniciarComparacion;
    private androidx.cardview.widget.CardView cardReiniciarEscaneado;
    private androidx.cardview.widget.CardView cardReiniciarCompleto;

    // ViewModel y SharedPreferences
    private InventoryComparisonViewModel viewModel;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion_reinicio);

        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupViewModel();
        setupClickListeners();
        actualizarEstadoSistema();
        cargarHistorialReinicios();

        // Manejo moderno del botón Atrás
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        tvEstadoSistema = findViewById(R.id.tv_estado_sistema);
        tvEstadoEscaneado = findViewById(R.id.tv_estado_escaneado);
        tvEstadoComparacion = findViewById(R.id.tv_estado_comparacion);
        tvHistorialReinicios = findViewById(R.id.tv_historial_reinicios);

        cardReiniciarComparacion = findViewById(R.id.card_reiniciar_comparacion);
        cardReiniciarEscaneado = findViewById(R.id.card_reiniciar_escaneado);
        cardReiniciarCompleto = findViewById(R.id.card_reiniciar_completo);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Reinicio");
        }
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                android.R.string.ok,
                android.R.string.cancel
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(InventoryComparisonViewModel.class);
        sharedPreferences = getSharedPreferences("reinicio_historial", MODE_PRIVATE);

        // Observar cambios en los datos
        viewModel.getInventarioSistema().observe(this, inventario -> actualizarEstadoSistema());
        viewModel.getInventarioEscaneado().observe(this, inventario -> actualizarEstadoSistema());
        viewModel.getComparacion().observe(this, comparacion -> actualizarEstadoSistema());
    }

    private void setupClickListeners() {
        cardReiniciarComparacion.setOnClickListener(v ->
                mostrarDialogoConfirmacion(
                        "Reiniciar Comparación",
                        "¿Estás seguro de que deseas reiniciar solo la comparación?\n\n" +
                                "Se mantendrán:\n• Inventario del sistema\n• Inventario escaneado\n\n" +
                                "Se eliminará:\n• Resultados de comparación",
                        this::ejecutarReinicioComparacion
                )
        );

        cardReiniciarEscaneado.setOnClickListener(v ->
                mostrarDialogoConfirmacion(
                        "Reiniciar Inventario Escaneado",
                        "¿Estás seguro de que deseas reiniciar el inventario escaneado?\n\n" +
                                "Se mantendrá:\n• Inventario del sistema\n\n" +
                                "Se eliminará:\n• Inventario escaneado\n• Resultados de comparación",
                        this::ejecutarReinicioEscaneado
                )
        );

        cardReiniciarCompleto.setOnClickListener(v ->
                mostrarDialogoConfirmacionCompleto()
        );
    }

    private void actualizarEstadoSistema() {
        // Actualizar estado del inventario del sistema
        if (viewModel.getInventarioSistema().getValue() != null &&
                !viewModel.getInventarioSistema().getValue().isEmpty()) {
            int items = viewModel.getInventarioSistema().getValue().size();
            tvEstadoSistema.setText("🟢 Cargado (" + String.format("%,d", items) + " items)");
            tvEstadoSistema.setTextColor(getResources().getColor(R.color.success_dark, null));
        } else {
            tvEstadoSistema.setText("🔴 Sin cargar");
            tvEstadoSistema.setTextColor(getResources().getColor(R.color.error_dark, null));
        }

        // Actualizar estado del inventario escaneado
        if (viewModel.getInventarioEscaneado().getValue() != null &&
                !viewModel.getInventarioEscaneado().getValue().isEmpty()) {
            int items = viewModel.getInventarioEscaneado().getValue().size();
            tvEstadoEscaneado.setText("🟡 En progreso (" + String.format("%,d", items) + " items)");
            tvEstadoEscaneado.setTextColor(getResources().getColor(R.color.warning_dark, null));
        } else {
            tvEstadoEscaneado.setText("🔴 Sin datos");
            tvEstadoEscaneado.setTextColor(getResources().getColor(R.color.error_dark, null));
        }

        // Actualizar estado de la comparación
        if (viewModel.getComparacion().getValue() != null &&
                !viewModel.getComparacion().getValue().isEmpty()) {
            // Calcular diferencias usando Triple<String, InventarioItem, InventarioItem>
            int diferencias = 0;
            for (var item : viewModel.getComparacion().getValue()) {
                // item es Triple<String, InventarioItem, InventarioItem>
                // item.getFirst() = SKU, item.getSecond() = sistema, item.getThird() = escaneado
                int diferencia = viewModel.calcularDiferencia(item.getSecond(), item.getThird());
                if (diferencia != 0) {
                    diferencias++;
                }
            }
            tvEstadoComparacion.setText("🟢 Completada (" + diferencias + " diferencias)");
            tvEstadoComparacion.setTextColor(getResources().getColor(R.color.success_dark, null));
        } else {
            tvEstadoComparacion.setText("🔴 Sin realizar");
            tvEstadoComparacion.setTextColor(getResources().getColor(R.color.error_dark, null));
        }
    }

    private void mostrarDialogoConfirmacion(String titulo, String mensaje, Runnable accion) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    accion.run();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void mostrarDialogoConfirmacionCompleto() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ REINICIO COMPLETO")
                .setMessage("ATENCIÓN: Esta acción eliminará TODOS los datos:\n\n" +
                        "• Inventario del sistema\n" +
                        "• Inventario escaneado\n" +
                        "• Resultados de comparación\n" +
                        "• Datos guardados en el dispositivo\n\n" +
                        "Esta acción NO se puede deshacer.\n\n" +
                        "¿Estás completamente seguro?")
                .setPositiveButton("SÍ, ELIMINAR TODO", (dialog, which) -> {
                    ejecutarReinicioCompleto();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void ejecutarReinicioComparacion() {
        viewModel.reiniciarComparacion();
        guardarEnHistorial("Reinicio de comparación");
        Toast.makeText(this, "Comparación reiniciada exitosamente", Toast.LENGTH_LONG).show();
        actualizarEstadoSistema();
        cargarHistorialReinicios();
    }

    private void ejecutarReinicioEscaneado() {
        viewModel.reiniciarInventarioEscaneado();
        guardarEnHistorial("Reinicio de inventario escaneado");
        Toast.makeText(this, "Inventario escaneado reiniciado exitosamente", Toast.LENGTH_LONG).show();
        actualizarEstadoSistema();
        cargarHistorialReinicios();
    }

    private void ejecutarReinicioCompleto() {
        viewModel.reiniciarCompletamente();
        guardarEnHistorial("Reinicio completo del sistema");
        Toast.makeText(this, "Reinicio completo realizado. Todos los datos han sido eliminados.", Toast.LENGTH_LONG).show();
        actualizarEstadoSistema();
        cargarHistorialReinicios();
    }

    private void guardarEnHistorial(String accion) {
        String timestamp = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        String entrada = timestamp + " - " + accion;

        // Obtener historial actual
        String historialActual = sharedPreferences.getString("historial", "");

        // Agregar nueva entrada al principio
        String nuevoHistorial = entrada + "\n" + historialActual;

        // Limitar a las últimas 10 entradas
        String[] lineas = nuevoHistorial.split("\n");
        StringBuilder historialLimitado = new StringBuilder();
        for (int i = 0; i < Math.min(10, lineas.length); i++) {
            if (!lineas[i].trim().isEmpty()) {
                historialLimitado.append("• ").append(lineas[i]).append("\n");
            }
        }

        // Guardar en SharedPreferences
        sharedPreferences.edit()
                .putString("historial", nuevoHistorial)
                .apply();
    }

    private void cargarHistorialReinicios() {
        String historial = sharedPreferences.getString("historial", "");

        if (historial.isEmpty()) {
            tvHistorialReinicios.setText("No hay reinicios registrados");
        } else {
            String[] lineas = historial.split("\n");
            StringBuilder historialMostrar = new StringBuilder();
            for (int i = 0; i < Math.min(5, lineas.length); i++) {
                if (!lineas[i].trim().isEmpty()) {
                    historialMostrar.append("• ").append(lineas[i]).append("\n");
                }
            }
            tvHistorialReinicios.setText(historialMostrar.toString().trim());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Manejar navegación del drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
