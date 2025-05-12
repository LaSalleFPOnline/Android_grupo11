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
import com.example.educontrol.api.ACS

class AcsAdapter(
    private val onUpdate: (ACS) -> Unit,
    private val onDelete: (ACS) -> Unit
) : ListAdapter<ACS, AcsAdapter.AcsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_acs, parent, false)
        return AcsViewHolder(view)
    }

    override fun onBindViewHolder(holder: AcsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AcsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProfesor = itemView.findViewById<TextView>(R.id.tvNombreProfesorACS)
        private val tvCurso = itemView.findViewById<TextView>(R.id.tvCursoACS)
        private val tvAsignatura = itemView.findViewById<TextView>(R.id.tvAsignaturaACS)
        private val tvSemestre = itemView.findViewById<TextView>(R.id.tvSemestreACS)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarACS)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarACS)

        fun bind(acs: ACS) {
            tvProfesor.text = "Profesor: ${acs.nombre_profesor ?: "N/A"}"
            tvCurso.text = "Curso: ${acs.nombre_curso ?: "N/A"}"
            tvAsignatura.text = "Asignatura: ${acs.nombre_asignatura ?: "N/A"}"
            tvSemestre.text = "Semestre: ${acs.fecha_semestre ?: "N/A"}"

            btnEditar.setOnClickListener { onUpdate(acs) }
            btnEliminar.setOnClickListener { onDelete(acs) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ACS>() {
        override fun areItemsTheSame(oldItem: ACS, newItem: ACS): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ACS, newItem: ACS): Boolean =
            oldItem == newItem
    }
}




