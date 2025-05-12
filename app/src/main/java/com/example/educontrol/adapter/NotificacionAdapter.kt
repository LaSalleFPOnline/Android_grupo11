package com.example.educontrol.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.example.educontrol.R
import com.example.educontrol.api.NotificacionResponse

class NotificacionAdapter(
    private val context: Context,
    private val notificaciones: MutableList<NotificacionResponse>,
    private val onChecked: (NotificacionResponse) -> Unit
) : BaseAdapter() {

    override fun getCount() = notificaciones.size
    override fun getItem(position: Int) = notificaciones[position]
    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val view = convertView ?: inflater.inflate(R.layout.item_notificacion, parent, false)

        val noti = notificaciones[position]
        view.findViewById<TextView>(R.id.txtTitulo).text = noti.titulo
        view.findViewById<TextView>(R.id.txtMensaje).text = noti.mensaje
        view.findViewById<TextView>(R.id.txtFecha).text = noti.fecha_registro.substring(0, 10)

        val chk = view.findViewById<CheckBox>(R.id.chkLeido)
        chk.setOnCheckedChangeListener(null)
        chk.isChecked = noti.leido == true // ✅ Solo se marca si es true

        chk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onChecked(noti)
                notificaciones.removeAt(position)
                notifyDataSetChanged()
            }
        }

        return view
    }

    fun updateData(nuevaLista: MutableList<NotificacionResponse>) {
        notificaciones.clear()
        notificaciones.addAll(nuevaLista)
        notifyDataSetChanged()
    }


}
