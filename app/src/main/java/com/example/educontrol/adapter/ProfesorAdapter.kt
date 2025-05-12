package com.example.educontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.database.UsuarioEntity

class ProfesorAdapter(private val usuarios: List<UsuarioEntity>) :
    RecyclerView.Adapter<ProfesorAdapter.ProfesorViewHolder>() {

    class ProfesorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.nombreProfesor)
        val email: TextView = view.findViewById(R.id.emailProfesor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profesor, parent, false)
        return ProfesorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfesorViewHolder, position: Int) {
        val usuario = usuarios[position]
        holder.nombre.text = usuario.nombre
        holder.email.text = usuario.email
    }

    override fun getItemCount(): Int = usuarios.size
}
