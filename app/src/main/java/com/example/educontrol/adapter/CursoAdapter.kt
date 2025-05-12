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
import com.example.educontrol.api.Curso

class CursoAdapter(
    private val onUpdate: (Curso) -> Unit,
    private val onDelete: (Curso) -> Unit
) : ListAdapter<Curso, CursoAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position), onUpdate, onDelete)
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreCurso)
        private val tvGrupo = itemView.findViewById<TextView>(R.id.tvGrupoCurso)
        private val tvColegio = itemView.findViewById<TextView>(R.id.tvColegioCurso)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarCurso)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarCurso)

        fun bind(curso: Curso, onUpdate: (Curso) -> Unit, onDelete: (Curso) -> Unit) {
            tvNombre.text = curso.nombre
            tvGrupo.text = "Grupo: ${curso.grupo}"
            tvColegio.text = "Colegio: ${curso.nombre_colegio ?: "-"}"
            btnEditar.setOnClickListener { onUpdate(curso) }
            btnEliminar.setOnClickListener { onDelete(curso) }
        }
    }

    class CourseDiffCallback : DiffUtil.ItemCallback<Curso>() {
        override fun areItemsTheSame(oldItem: Curso, newItem: Curso) = oldItem.id_curso == newItem.id_curso
        override fun areContentsTheSame(oldItem: Curso, newItem: Curso) = oldItem == newItem
    }
}
