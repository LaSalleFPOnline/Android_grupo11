package com.example.educontrol.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.api.Semestre

class SemestreAdapter(
    private val onUpdate: (Semestre) -> Unit,
    private val onDelete: (Semestre) -> Unit
) : ListAdapter<Semestre, SemestreAdapter.SemesterViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_semestre, parent, false)
        return SemesterViewHolder(view)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SemesterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumero = itemView.findViewById<TextView>(R.id.tvNumeroSemestre)
        private val tvInicio = itemView.findViewById<TextView>(R.id.tvFechaInicioSemestre)
        private val tvFin = itemView.findViewById<TextView>(R.id.tvFechaFinSemestre)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarSemestre)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarSemestre)

        fun bind(semestre: Semestre) {
            tvNumero.text = "Semestre: ${semestre.numero_semestre}"
            tvInicio.text = "Inicio: ${semestre.fecha_inicio}"
            tvFin.text = "Fin: ${semestre.fecha_fin}"

            btnEditar.setOnClickListener { onUpdate(semestre) }
            btnEliminar.setOnClickListener { onDelete(semestre) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Semestre>() {
        override fun areItemsTheSame(oldItem: Semestre, newItem: Semestre) =
            oldItem.id_semestre == newItem.id_semestre

        override fun areContentsTheSame(oldItem: Semestre, newItem: Semestre) =
            oldItem == newItem
    }
}
