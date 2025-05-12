package com.example.educontrol.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R

class AsignaturaAdapter2(private val items: List<AsignaturaInfo>) :
    RecyclerView.Adapter<AsignaturaAdapter2.ViewHolder>() {

    data class AsignaturaInfo(val asignatura: String, val curso: String, val profesor: String)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val asignatura: TextView = view.findViewById(R.id.textAsignatura)
        val curso: TextView = view.findViewById(R.id.textCurso)
        val profesor: TextView = view.findViewById(R.id.textProfesor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_asignatura, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.asignatura.text = "📘 ${item.asignatura}"
        holder.curso.text = "🎓 Curso: ${item.curso}"
        holder.profesor.text = "👨‍🏫 Profesor: ${item.profesor}"
    }

    override fun getItemCount(): Int = items.size
}
