package com.joseluiscruz.capturainventario.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.joseluiscruz.capturainventario.R
import com.joseluiscruz.capturainventario.databinding.ItemAlmacenCapacidadBinding
import com.joseluiscruz.capturainventario.model.AlmacenCapacidad
import com.joseluiscruz.capturainventario.model.EstadoSaturacion

/**
 * Adapter para mostrar la lista de almacenes con su capacidad y saturación
 *
 * @deprecated Debe migrarse a Compose. La UI de almacenes debería usar LazyColumn
 * con componentes Composable equivalentes, como los definidos en LegacyAdaptersCompose.kt.
 */
@Deprecated(
    message = "Pendiente de migración a Compose. Crear AlmacenCapacidadItem Composable.",
    level = DeprecationLevel.WARNING
)
class AlmacenCapacidadAdapter(
    private val onEditarClick: (AlmacenCapacidad) -> Unit,
    private val onEliminarClick: (AlmacenCapacidad) -> Unit
) : ListAdapter<AlmacenCapacidad, AlmacenCapacidadAdapter.AlmacenViewHolder>(AlmacenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlmacenViewHolder {
        val binding = ItemAlmacenCapacidadBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AlmacenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlmacenViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlmacenViewHolder(
        private val binding: ItemAlmacenCapacidadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(almacen: AlmacenCapacidad) {
            binding.apply {
                // Información básica del almacén
                tvNombreAlmacen.text = almacen.nombreAlmacen
                tvPalletsEscaneados.text = almacen.palletsEscaneados.toString()
                tvCapacidadMaxima.text = almacen.capacidadMaxima.toString()

                // Porcentaje de saturación
                val porcentaje = almacen.porcentajeSaturacion.toInt()
                tvPorcentajeSaturacion.text = almacen.porcentajeSaturacionFormateado
                progressSaturacion.progress = porcentaje

                // Estado y colores según el nivel de saturación
                val context = binding.root.context
                val estado = almacen.estadoSaturacion

                tvEstadoSaturacion.text = estado.descripcion

                // Aplicar colores según el estado
                val colorEstado = when (estado) {
                    EstadoSaturacion.BAJO -> ContextCompat.getColor(context, R.color.success_dark)
                    EstadoSaturacion.MEDIO -> ContextCompat.getColor(context, R.color.warning_dark)
                    EstadoSaturacion.ALTO -> ContextCompat.getColor(context, R.color.error_dark)
                    EstadoSaturacion.CRITICO -> Color.parseColor("#D32F2F")
                }

                tvPorcentajeSaturacion.setTextColor(colorEstado)
                tvEstadoSaturacion.setTextColor(colorEstado)

                // Color de la barra de progreso
                progressSaturacion.progressTintList = android.content.res.ColorStateList.valueOf(colorEstado)

                // Color de fondo del porcentaje
                val bgColor = when (estado) {
                    EstadoSaturacion.BAJO -> ContextCompat.getColor(context, R.color.success_light)
                    EstadoSaturacion.MEDIO -> ContextCompat.getColor(context, R.color.warning_light)
                    EstadoSaturacion.ALTO -> ContextCompat.getColor(context, R.color.error_light)
                    EstadoSaturacion.CRITICO -> Color.parseColor("#FFEBEE")
                }

                tvPorcentajeSaturacion.setBackgroundColor(bgColor)

                // Configurar botones
                btnEditarAlmacen.setOnClickListener {
                    onEditarClick(almacen)
                }

                btnEliminarAlmacen.setOnClickListener {
                    onEliminarClick(almacen)
                }
            }
        }
    }

    class AlmacenDiffCallback : DiffUtil.ItemCallback<AlmacenCapacidad>() {
        override fun areItemsTheSame(oldItem: AlmacenCapacidad, newItem: AlmacenCapacidad): Boolean {
            return oldItem.nombreAlmacen == newItem.nombreAlmacen
        }

        override fun areContentsTheSame(oldItem: AlmacenCapacidad, newItem: AlmacenCapacidad): Boolean {
            return oldItem == newItem
        }
    }
}
