package com.example.educontrol.adapter


import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.api.Usuario

class UsuarioAdapter(
    private val onDelete: (Usuario) -> Unit,
    private val onEdit: (Usuario) -> Unit,
) : ListAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UsuarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombre = itemView.findViewById<TextView>(R.id.tvNombreUsuario)
        private val tvEmail = itemView.findViewById<TextView>(R.id.tvCorreoUsuario)
        private val tvRol = itemView.findViewById<TextView>(R.id.tvRolUsuario)
        private val ivFoto = itemView.findViewById<ImageView>(R.id.ivFotoUsuario)
        private val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarUsuario)
        private val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarUsuario)

        fun bind(usuario: Usuario) {
            tvNombre.text = "${usuario.nombre} ${usuario.primerApellido}"
            tvEmail.text = usuario.email
            tvRol.text = usuario.rolUsuario

            val base64 = usuario.photo
            if (!base64.isNullOrEmpty()) {
                try {
                    Log.d("educontrol", "🖼️ Cargando imagen Base64")
                    val base64Clean = base64.substringAfter(",")
                    val bytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ivFoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e("educontrol", "❌ Error imagen: ${e.message}")
                    ivFoto.setImageResource(R.drawable.ic_account_profile)
                }
            } else {
                ivFoto.setImageResource(R.drawable.ic_account_profile)
            }

            btnEliminar.setOnClickListener { onDelete(usuario) }
            btnEditar.setOnClickListener { onEdit(usuario) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean =
            oldItem.idUsuario == newItem.idUsuario

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem.photo == newItem.photo
        }
    }
}










