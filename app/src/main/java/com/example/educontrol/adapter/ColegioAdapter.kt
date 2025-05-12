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
import com.example.educontrol.api.Colegio

class ColegioAdapter(
    private val onUpdate: (Colegio) -> Unit,
    private val onDelete: (Colegio) -> Unit
) : ListAdapter<Colegio, ColegioAdapter.ColegioViewHolder>(ColegioDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColegioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_colegio, parent, false)
        return ColegioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColegioViewHolder, position: Int) {
        val colegio = getItem(position)
        holder.bind(colegio, onUpdate, onDelete)
    }

    class ColegioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre: TextView = itemView.findViewById(R.id.tvNombreColegio)
        private val tvDireccion: TextView = itemView.findViewById(R.id.tvDireccionColegio)
        private val btnActualizar: ImageButton = itemView.findViewById(R.id.btnActualizar)
        private val btnEliminar: ImageButton = itemView.findViewById(R.id.btnEliminar)

        fun bind(colegio: Colegio, onUpdate: (Colegio) -> Unit, onDelete: (Colegio) -> Unit) {
            tvNombre.text = colegio.nombre
            tvDireccion.text = colegio.direccion

            btnActualizar.setOnClickListener { onUpdate(colegio) }
            btnEliminar.setOnClickListener { onDelete(colegio) }
        }
    }

    class ColegioDiffCallback : DiffUtil.ItemCallback<Colegio>() {
        override fun areItemsTheSame(oldItem: Colegio, newItem: Colegio): Boolean =
            oldItem.id_colegio == newItem.id_colegio

        override fun areContentsTheSame(oldItem: Colegio, newItem: Colegio): Boolean =
            oldItem == newItem
    }
}

