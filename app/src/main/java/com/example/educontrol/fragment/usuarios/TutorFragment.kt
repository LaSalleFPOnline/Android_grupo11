package com.example.educontrol.fragment.usuarios

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.educontrol.R

import com.example.educontrol.adapter.NotificacionAdapter
import com.example.educontrol.api.*
import com.example.educontrol.viewmodel.DataViewModel

class TutorFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()

    private lateinit var btnVerAdvertencias: Button
    private lateinit var btnVerNotas: Button
    private lateinit var btnEnviarNoti: Button
    private lateinit var btnVerNotificaciones: Button
    private lateinit var btnTodasNotificaciones: Button

    private var token = ""
    private var idTutor = -1

    private var listaAdvertenciasTutor = listOf<AdvertenciaAlumno>()
    private var listaNotasTutor = listOf<NotaAlumno>()
    private var listaUsuarios = listOf<Usuario>()
    private var listaNotificaciones = listOf<NotificacionResponse>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_tutor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Botones
        btnVerAdvertencias = view.findViewById(R.id.btnVerAdvertencias)
        btnVerNotas = view.findViewById(R.id.btnVerNotas)
        btnEnviarNoti = view.findViewById(R.id.btnEnviarNoti)
        btnVerNotificaciones = view.findViewById(R.id.btnVerNotificaciones)
        btnTodasNotificaciones = view.findViewById(R.id.btnTodasNotificaciones)

        token = getToken(requireContext()) ?: ""
        idTutor = getUserId(requireContext())

        Log.d("educontrol", "🧪 TOKEN: $token")
        Log.d("educontrol", "🧪 ID Tutor: $idTutor")

        // Cargar datos
        viewModel.getAdvertenciasPorTutor(idTutor, token)
        viewModel.getNotasPorTutor(idTutor, token)
        viewModel.getUser(token)
        viewModel.getNotificacionesNoLeidas(idTutor, token)

        // Observadores
        viewModel.listaAdvertenciasTutor.observe(viewLifecycleOwner) {
            listaAdvertenciasTutor = it
        }

        viewModel.listaNotasTutor.observe(viewLifecycleOwner) {
            listaNotasTutor = it
        }

        viewModel.listaUsuarios.observe(viewLifecycleOwner) {
            listaUsuarios = it
        }

        viewModel.notificacionesNoLeidas.observe(viewLifecycleOwner) {
            listaNotificaciones = it
            actualizarTextoBotonNotificaciones()
        }

        viewModel.enviarNotificacionStatus.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        // Listeners
        btnVerAdvertencias.setOnClickListener {
            if (listaAdvertenciasTutor.isEmpty()) {
                toast("No hay advertencias registradas.")
                return@setOnClickListener
            }
            mostrarDialogo("Advertencias del Alumno", listaAdvertenciasTutor.map {
                "📚 ${it.nombre}\n🧑 Alumno: ${it.nombre_alumno}\n👨‍🏫 Profesor: ${it.nombre_profesor}\n🔔 Motivo: ${it.message}\n"
            }.toTypedArray())
        }

        btnVerNotas.setOnClickListener {
            if (listaNotasTutor.isEmpty()) {
                toast("No hay notas registradas.")
                return@setOnClickListener
            }
            mostrarDialogo("Notas del Alumno", listaNotasTutor.map {
                "📘 ${it.nombre_asignatura}\n🧑 Alumno: ${it.nombre_alumno}\n🎯 Nota: ${it.nota}\n"
            }.toTypedArray())
        }

        btnEnviarNoti.setOnClickListener {
            mostrarDialogoNotificacion()
        }

        btnVerNotificaciones.setOnClickListener {
            mostrarNotificacionesNoLeidas()
        }

        btnTodasNotificaciones.setOnClickListener {
            mostrarTodasLasNotificaciones()}
    }

    private fun mostrarDialogoNotificacion() {
        val view = layoutInflater.inflate(R.layout.dialog_enviar_notificacion, null)
        val spinner = view.findViewById<Spinner>(R.id.spinnerDestinatario)
        val tituloInput = view.findViewById<EditText>(R.id.editTituloNoti)
        val mensajeInput = view.findViewById<EditText>(R.id.editMensajeNoti)

        val posiblesProfesores = listaUsuarios.filter { it.idRol == 1 } // Solo profesores

        val nombres = listOf("Selecciona profesor") + posiblesProfesores.map {
            "${it.nombre} ${it.primerApellido}"
        }

        spinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)

        AlertDialog.Builder(requireContext())
            .setTitle("Enviar Notificación")
            .setView(view)
            .setPositiveButton("Enviar") { _, _ ->
                val profesor = posiblesProfesores.getOrNull(spinner.selectedItemPosition - 1)
                val titulo = tituloInput.text.toString().trim()
                val mensaje = mensajeInput.text.toString().trim()

                if (profesor == null || titulo.isEmpty() || mensaje.isEmpty()) {
                    toast("Completa todos los campos")
                    return@setPositiveButton
                }

                val noti = NotificacionRequest(profesor.idUsuario, titulo, mensaje)
                viewModel.enviarNotificacion(noti, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarNotificacionesNoLeidas() {
        if (listaNotificaciones.isEmpty()) {
            toast("No tienes notificaciones nuevas")
            return
        }

        val listView = ListView(requireContext())
        val adapter = NotificacionAdapter(requireContext(), listaNotificaciones.toMutableList()) { noti ->
            viewModel.marcarNotificacionLeida(noti.id_notificacion, token)
            toast("Notificación marcada como leída")
            viewModel.getNotificacionesNoLeidas(idTutor, token)
        }
        listView.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Notificaciones no leídas")
            .setView(listView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun actualizarTextoBotonNotificaciones() {
        val total = listaNotificaciones.size
        if (total == 0) {
            btnVerNotificaciones.visibility = View.GONE
        } else {
            btnVerNotificaciones.visibility = View.VISIBLE
            btnVerNotificaciones.text = "📥 Notificaciones sin leer ($total)"
            btnVerNotificaciones.setTextColor(Color.RED)
        }
    }

    private fun mostrarTodasLasNotificaciones() {
        // Lanzamos la petición una sola vez
        viewModel.getTodasNotificaciones(idTutor, token)

        // Evitamos múltiples observadores eliminando cualquier anterior (opcional, si ya hay control en ViewModel)
        viewModel.listaTodasNotificaciones.removeObservers(viewLifecycleOwner)

        // Observamos sólo una vez esta respuesta
        viewModel.listaTodasNotificaciones.observe(viewLifecycleOwner) { todas ->
            if (todas.isNullOrEmpty()) {
                toast("No tienes notificaciones")
                return@observe
            }

            val listView = ListView(requireContext())
            val adapter = NotificacionAdapter(requireContext(), todas.toMutableList()) { /* sin acción */ }
            listView.adapter = adapter

            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(" Todas las Notificaciones")
                .setView(listView)
                .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                .create()

            dialog.show()
        }
    }

    private fun mostrarDialogo(titulo: String, contenido: Array<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setItems(contenido, null)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun getToken(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    private fun getUserId(context: Context): Int {
        return context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
    }

    private fun toast(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }
}
