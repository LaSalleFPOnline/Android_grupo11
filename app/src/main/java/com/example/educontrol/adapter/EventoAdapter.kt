package com.example.educontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.api.Evento

class EventoAdapter(
    private val onUpdate: (Evento) -> Unit,
    private val onDelete: (Evento) -> Unit
) : ListAdapter<Evento, EventoAdapter.EventoViewHolder>(EventoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        holder.bind(getItem(position), onUpdate, onDelete)
    }

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvConcepto = itemView.findViewById<TextView>(R.id.tvConcepto)
        private val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)
        private val tvNombreUbicacion = itemView.findViewById<TextView>(R.id.tvNombreUbicacion)
        private val tvNombreColegio = itemView.findViewById<TextView>(R.id.tvNombreColegio)
        private val btnActualizar = itemView.findViewById<ImageButton>(R.id.btnActualizarEvento)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarEvento)

        fun bind(evento: Evento, onUpdate: (Evento) -> Unit, onDelete: (Evento) -> Unit) {
            tvConcepto.text = evento.concepto
            tvFecha.text = evento.fecha
            tvNombreUbicacion.text = evento.nombre_ubicacion
            tvNombreColegio.text = evento.nombre_colegio ?: "No asignado"

            btnActualizar.setOnClickListener { onUpdate(evento) }
            btnEliminar.setOnClickListener { onDelete(evento) }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<Evento>() {
        override fun areItemsTheSame(oldItem: Evento, newItem: Evento) = oldItem.id_evento == newItem.id_evento
        override fun areContentsTheSame(oldItem: Evento, newItem: Evento) = oldItem == newItem
    }
}

