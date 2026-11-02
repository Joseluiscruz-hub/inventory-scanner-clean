package com.joseluiscruz.capturainventario.ui.exportar

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.joseluiscruz.capturainventario.data.repository.CsvInventarioService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ExportarActivity : AppCompatActivity() {

    private lateinit var btnExportarInventario: Button
    private lateinit var btnExportarPDF: Button
    private lateinit var btnExportarComparacion: Button
    private lateinit var btnExportarHistorial: Button
    private lateinit var tvStatusExport: TextView

    private val csvService by lazy { CsvInventarioService(this) }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { handleExportFile(it, isPdf = false) }
    }

    private val exportPdfLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            uri?.let { handleExportFile(it, isPdf = true) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createUI()
        setupClickListeners()
    }

    private fun createUI() {
        val scrollView = ScrollView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.parseColor("#0D0D0D"))
        }

        val mainLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }

        val titleText = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
            text = "Exportar Datos"
            textSize = 24f
            setTextColor(Color.parseColor("#FFFFFF"))
            gravity = Gravity.CENTER
        }
        mainLayout.addView(titleText)

        val inventoryCard = createCard()
        val inventoryLayout = createCardLayout()

        inventoryLayout.addView(createSectionTitle("Exportar Inventario"))

        val inventoryDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text =
                "Exporta los datos de inventario actual a un archivo CSV. Incluye SKU, descripción, cantidad, fecha de producción y días de vida."
            setTextColor(Color.parseColor("#B3B3B3"))
        }
        inventoryLayout.addView(inventoryDesc)

        btnExportarInventario = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar CSV"
            setBackgroundColor(Color.parseColor("#E30613"))
            setTextColor(Color.WHITE)
        }
        inventoryLayout.addView(btnExportarInventario)

        btnExportarPDF = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
            text = "Exportar PDF"
            setBackgroundColor(Color.parseColor("#00C853"))
            setTextColor(Color.WHITE)
        }
        inventoryLayout.addView(btnExportarPDF)

        inventoryCard.addView(inventoryLayout)
        mainLayout.addView(inventoryCard)

        val comparisonCard = createCard()
        val comparisonLayout = createCardLayout()
        comparisonLayout.addView(createSectionTitle("Exportar Comparación"))

        val comparisonDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Exporta los resultados de la comparación entre el inventario escaneado y los datos del sistema."
            setTextColor(Color.parseColor("#B3B3B3"))
        }
        comparisonLayout.addView(comparisonDesc)

        btnExportarComparacion = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar Comparación"
            setBackgroundColor(Color.parseColor("#E30613"))
            setTextColor(Color.WHITE)
        }
        comparisonLayout.addView(btnExportarComparacion)

        comparisonCard.addView(comparisonLayout)
        mainLayout.addView(comparisonCard)

        val historyCard = createCard()
        val historyLayout = createCardLayout()
        historyLayout.addView(createSectionTitle("Exportar Historial"))

        val historyDesc = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = "Exporta el historial de escaneos realizados, incluyendo fecha, hora y usuario."
            setTextColor(Color.parseColor("#B3B3B3"))
        }
        historyLayout.addView(historyDesc)

        btnExportarHistorial = Button(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            text = "Exportar Historial"
            setBackgroundColor(Color.parseColor("#E30613"))
            setTextColor(Color.WHITE)
        }
        historyLayout.addView(btnExportarHistorial)

        historyCard.addView(historyLayout)
        mainLayout.addView(historyCard)

        tvStatusExport = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            text = "Seleccione una opción para exportar"
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#B3B3B3"))
        }
        mainLayout.addView(tvStatusExport)

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }

    private fun createCard(): CardView {
        return CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 32
            }
            radius = 16f
            cardElevation = 4f
            setCardBackgroundColor(Color.parseColor("#1A1A1A"))
        }
    }

    private fun createCardLayout(): LinearLayout {
        return LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(32)
        }
    }

    private fun createSectionTitle(title: String): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
            text = title
            textSize = 18f
            setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    private fun setupClickListeners() {
        btnExportarInventario.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("inventario_$timestamp.csv")
        }

        btnExportarPDF.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportPdfLauncher.launch("reporte_inventario_$timestamp.pdf")
        }

        btnExportarComparacion.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("comparacion_$timestamp.csv")
        }

        btnExportarHistorial.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            exportLauncher.launch("historial_$timestamp.csv")
        }
    }

    private fun handleExportFile(uri: Uri, isPdf: Boolean) {
        tvStatusExport.text = "Exportando archivo..."

        lifecycleScope.launch {
            val result = if (isPdf) {
                csvService.exportarPdf(uri)
            } else {
                csvService.exportarCsv(uri)
            }

            if (result.isSuccess) {
                val msg = "Exportación exitosa. Registros: ${result.getOrNull()}"
                tvStatusExport.text = msg
                Toast.makeText(this@ExportarActivity, "Exportación exitosa", Toast.LENGTH_SHORT).show()
            } else {
                val error = result as com.joseluiscruz.capturainventario.domain.model.OperationResult.Error
                val msg = "Error en la exportación: ${error.message}"
                tvStatusExport.text = msg
                Toast.makeText(this@ExportarActivity, "Error en la exportación", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
