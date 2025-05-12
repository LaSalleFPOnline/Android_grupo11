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
import com.example.educontrol.api.Ubicacion

class UbicacionAdapter(
    private val onUpdate: (Ubicacion) -> Unit,
    private val onDelete: (Ubicacion) -> Unit
) : ListAdapter<Ubicacion, UbicacionAdapter.UbicacionViewHolder>(UbicacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UbicacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ubicacion, parent, false)
        return UbicacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: UbicacionViewHolder, position: Int) {
        holder.bind(getItem(position), onUpdate, onDelete)
    }

    class UbicacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreUbicacion)
        private val tvTipo = itemView.findViewById<TextView>(R.id.tvTipoUbicacion)
        private val tvNombreColegio = itemView.findViewById<TextView>(R.id.tvNombreColegioUbicacion)
        private val btnActualizar = itemView.findViewById<ImageButton>(R.id.btnActualizarUbicacion)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarUbicacion)

        fun bind(ubicacion: Ubicacion, onUpdate: (Ubicacion) -> Unit, onDelete: (Ubicacion) -> Unit) {
            tvNombre.text = ubicacion.nombre
            tvTipo.text = ubicacion.tipo
            tvNombreColegio.text = ubicacion.nombre_colegio

            btnActualizar.setOnClickListener { onUpdate(ubicacion) }
            btnEliminar.setOnClickListener { onDelete(ubicacion) }
        }
    }

    class UbicacionDiffCallback : DiffUtil.ItemCallback<Ubicacion>() {
        override fun areItemsTheSame(oldItem: Ubicacion, newItem: Ubicacion) = oldItem.id_ubicacion == newItem.id_ubicacion
        override fun areContentsTheSame(oldItem: Ubicacion, newItem: Ubicacion) = oldItem == newItem
    }
}


