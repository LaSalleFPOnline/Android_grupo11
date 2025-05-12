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
import com.example.educontrol.api.Asignatura

class AsignaturaAdapter(
    private val onUpdate: (Asignatura) -> Unit,
    private val onDelete: (Asignatura) -> Unit
) : ListAdapter<Asignatura, AsignaturaAdapter.SubjectViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_asignatura, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreAsignatura)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarAsignatura)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarAsignatura)

        fun bind(asignatura: Asignatura) {
            tvNombre.text = asignatura.nombre
            btnEditar.setOnClickListener { onUpdate(asignatura) }
            btnEliminar.setOnClickListener { onDelete(asignatura) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Asignatura>() {
        override fun areItemsTheSame(oldItem: Asignatura, newItem: Asignatura): Boolean {
            return oldItem.id_asignatura == newItem.id_asignatura
        }

        override fun areContentsTheSame(oldItem: Asignatura, newItem: Asignatura): Boolean {
            return oldItem == newItem
        }
    }
}
