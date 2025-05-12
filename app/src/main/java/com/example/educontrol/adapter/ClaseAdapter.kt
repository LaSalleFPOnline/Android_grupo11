package com.example.educontrol.adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.api.Clase
import com.example.educontrol.api.Ubicacion

class ClaseAdapter(
    private var ubicaciones: List<Ubicacion>,
    private val onEdit: (Clase) -> Unit,
    private val onDelete: (Clase) -> Unit
) : RecyclerView.Adapter<ClaseAdapter.ClaseViewHolder>() {

    private var clases: List<Clase> = emptyList()

    fun submitList(newList: List<Clase>) {
        clases = newList
        notifyDataSetChanged()
    }

    fun updateUbicaciones(nuevasUbicaciones: List<Ubicacion>) {
        ubicaciones = nuevasUbicaciones
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clase, parent, false)
        return ClaseViewHolder(view)
    }

    override fun getItemCount(): Int = clases.size

    override fun onBindViewHolder(holder: ClaseViewHolder, position: Int) {
        holder.bind(clases[position])
    }

    inner class ClaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUbicacion = itemView.findViewById<TextView>(R.id.tvUbicacion)
        private val tvDias = itemView.findViewById<TextView>(R.id.tvDias)
        private val tvHorario = itemView.findViewById<TextView>(R.id.tvHorario)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarClase)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarClase)

        fun bind(clase: Clase) {
            tvUbicacion.text = "Ubicación: ${clase.ubicacion ?: "Sin asignar"}"

            val diasSemana = listOf(
                "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"
            )

            val diasTexto = clase.dia_semana
                ?.mapNotNull { dayNumber ->
                    diasSemana.getOrNull(dayNumber - 1) // porque los días empiezan en 1
                }
                ?.joinToString(", ")
                ?: "No asignado"

            val horaInicio = clase.hora_inicio ?: "Sin hora"
            val horaFin = clase.hora_fin ?: "Sin hora"

            tvDias.text = "Días: $diasTexto"
            tvHorario.text = "Horario: $horaInicio - $horaFin"

            btnEditar.setOnClickListener { onEdit(clase) }
            btnEliminar.setOnClickListener { onDelete(clase) }
        }
    }
}
