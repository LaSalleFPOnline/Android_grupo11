package com.example.educontrol.fragment.usuarios

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
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

@Suppress("DEPRECATION")
class AlumnoFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var btnVerAsignaturas: Button
    private lateinit var btnVerAdvertencias: Button
    private lateinit var btnVerHorario: Button
    private lateinit var btnVerExamenes: Button
    private lateinit var btnEnviarNoti: Button
    private lateinit var btnVerNotificaciones: Button
    private lateinit var btnTodasNotificaciones: Button
    private lateinit var btnNfc: Button
    private var codigoRecibidoNfc: String? = null


    private var listaAsignaturas = listOf<Asignatura>()
    private var listaACS = listOf<ACS>()
    private var listaUsuarios = listOf<Usuario>()
    private var listaCursos = listOf<Curso>()
    private var listaAdvertencias = listOf<WarningResponse>()
    private var listaTests = listOf<Test>()
    private var listaNotas = listOf<NoteResponse>()
    private var listaNotificaciones = listOf<NotificacionResponse>()
    private var alumno: Usuario? = null
    private var idUsuario: Int = -1
    private var token = ""
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_alumno, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = Intent(requireContext(), requireActivity()::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        btnVerAsignaturas = view.findViewById(R.id.btnVerAsignaturas)
        btnVerAdvertencias = view.findViewById(R.id.btnVerAdvertencias)
        btnVerHorario = view.findViewById(R.id.btnVerHorario)
        btnVerExamenes = view.findViewById(R.id.btnVerExamenes)
        btnEnviarNoti = view.findViewById(R.id.btnEnviarNoti)
        btnVerNotificaciones = view.findViewById(R.id.btnVerNotificaciones)
        btnTodasNotificaciones = view.findViewById(R.id.btntodasNotificaciones)
        btnNfc = view.findViewById(R.id.btnEnviarNFC)


        val prefs = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        idUsuario = prefs.getInt("USER_ID", -1)
        token = getToken()


        // Cargar datos
        viewModel.getSubjects(token)
        viewModel.getUser(token)
        viewModel.getAcs(token)
        viewModel.getCursos(token)
        viewModel.getWarningsByUsuario(idUsuario, token)
        viewModel.getTestsByUser(idUsuario, token)
        viewModel.getNotasByUsuario(idUsuario, token)
        //viewModel.getHorarioAlumno(idUsuario, token)
        viewModel.getNotificacionesNoLeidas(idUsuario, token)

        // Observadores
        viewModel.listaAsignaturas.observe(viewLifecycleOwner) { listaAsignaturas = it }
        viewModel.listaAcs.observe(viewLifecycleOwner) { listaACS = it }
        viewModel.listaCursos.observe(viewLifecycleOwner) { listaCursos = it }
        viewModel.listaAdvertencias.observe(viewLifecycleOwner) { listaAdvertencias = it }
        viewModel.listaTests.observe(viewLifecycleOwner) { listaTests = it }
        viewModel.listaNotas.observe(viewLifecycleOwner) { listaNotas = it }
        viewModel.notificacionesNoLeidas.observe(viewLifecycleOwner) {
            listaNotificaciones = it
            actualizarTextoBotonNotificaciones()
        }

        viewModel.listaUsuarios.observe(viewLifecycleOwner) { usuarios ->
            listaUsuarios = usuarios
            alumno = usuarios.find { it.idUsuario == idUsuario }
        }

        viewModel.enviarNotificacionStatus.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        // Botones
        btnVerAsignaturas.setOnClickListener { mostrarAsignaturas() }
        btnVerAdvertencias.setOnClickListener { mostrarAdvertencias() }
        btnVerHorario.setOnClickListener {
            Log.d("educontrol", "🟢 Botón de horario pulsado para ID $idUsuario")
            viewModel.getHorarioAlumno(idUsuario, token)
        }
        btnVerExamenes.setOnClickListener { mostrarExamenes() }
        btnEnviarNoti.setOnClickListener { mostrarDialogoNotificacion() }
        btnVerNotificaciones.setOnClickListener { mostrarNotificacionesNoLeidas() }
        btnTodasNotificaciones.setOnClickListener { mostrarTodasLasNotificaciones()}

        btnNfc.setOnClickListener {
            toast("Acerca tu móvil al lector para registrar tu asistencia por NFC")
        }

        viewModel.horarioAlumno.observe(viewLifecycleOwner) { response ->
            if (response.isNullOrEmpty()) {
                toast("No tienes clases registradas")
                return@observe
            }

            val dias = mapOf(
                "1" to "Lunes", "2" to "Martes", "3" to "Miércoles",
                "4" to "Jueves", "5" to "Viernes", "6" to "Sábado", "7" to "Domingo"
            )

            val detalles = mutableListOf<String>()
            for ((diaKey, clases) in response) {
                val diaNombre = dias[diaKey] ?: "Día $diaKey"
                clases.forEach { clase ->
                    detalles.add(
                        "$diaNombre - ${clase.hora_inicio} a ${clase.hora_fin}\n" +
                                "📚 ${clase.asignatura}\n" +
                                "📍 Aula: ${clase.ubicacion}\n" +
                                "👨‍🏫 Profesor: ${clase.profesor}\n" +
                                "📘 Curso: ${clase.curso}\n"

                    )
                }
            }
            mostrarDialogo("Mi Horario", detalles.sorted().toTypedArray())
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        if (nfcAdapter == null) {
            toast("Este dispositivo no soporta NFC")
        } else {
            val intent = Intent(requireContext(), requireActivity()::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    }


    override fun onResume() {
        super.onResume()

        val intent = Intent(requireActivity().applicationContext, requireActivity()::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        pendingIntent = PendingIntent.getActivity(
            requireActivity().applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        val techList = arrayOf(
            arrayOf(MifareClassic::class.java.name),
            arrayOf(NfcA::class.java.name)
        )

        nfcAdapter?.enableForegroundDispatch(requireActivity(), pendingIntent, filters, techList)

        Log.d("educontrol", "📶 ForegroundDispatch ACTIVADO para MifareClassic y NfcA")
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(requireActivity())
        Log.d("educontrol", "📴 ForegroundDispatch desactivado")
    }


    fun handleNfcIntent(intent: Intent?) {
        Log.d("educontrol", "📲 handleNfcIntent recibido: $intent")

        val tag = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag == null) {
            Log.w("educontrol", "❌ No se recibió tag NFC. Aborting.")
            return
        }

        Log.d("educontrol", "✅ Tag detectado con techs: ${tag.techList.joinToString()}")

        val mifare = MifareClassic.get(tag)
        if (mifare != null) {
            try {
                mifare.connect()

                val builder = StringBuilder()
                var bloquesLeidos = 0

                for (sector in 0 until mifare.sectorCount) {
                    val auth = mifare.authenticateSectorWithKeyA(sector, MifareClassic.KEY_DEFAULT)
                    if (!auth) continue

                    val blockStart = mifare.sectorToBlock(sector)
                    val blockEnd = blockStart + mifare.getBlockCountInSector(sector)

                    for (block in blockStart until blockEnd) {
                        val data = mifare.readBlock(block)
                        bloquesLeidos++

                        val texto = data.map { if (it in 32..126) it.toInt().toChar() else '.' }
                            .joinToString("")
                        builder.append("🔢 Bloque $block: $texto\n")
                    }
                }

                mifare.close()

                if (bloquesLeidos > 0) {
                    Toast.makeText(requireContext(), "📍 Aula registrada", Toast.LENGTH_LONG).show()
                    Log.i("educontrol", "✅ Lectura completa:\n${builder.toString()}")
                } else {
                    Toast.makeText(requireContext(), "⚠️ Tag sin bloques accesibles", Toast.LENGTH_LONG).show()
                    Log.w("educontrol", "⚠️ No se pudieron leer bloques:\n${builder.toString()}")
                }

            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error leyendo MifareClassic: ${e.message}")
            }
            return
        }

        Log.w("educontrol", "⚠️ Tag no soporta MifareClassic")
    }



    private fun mostrarNotificacionesNoLeidas() {
        if (listaNotificaciones.isEmpty()) {
            toast("No tienes notificaciones nuevas")
            return
        }

        val listView = ListView(requireContext())
        val adapter = NotificacionAdapter(requireContext(), listaNotificaciones.toMutableList()) { noti ->
            viewModel.marcarNotificacionLeida(noti.id_notificacion, getToken())
            toast("Notificación marcada como leída")
            viewModel.getNotificacionesNoLeidas(idUsuario, getToken()) // refrescar online
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

    private fun mostrarAsignaturas() {
        val user = alumno ?: return toast("Usuario no encontrado")
        val subjectsFiltrados = user.subjects?.filterNotNull()
        val acsAlumno = if (!subjectsFiltrados.isNullOrEmpty()) {
            listaACS.filter { it.id in subjectsFiltrados }
        } else {
            listaACS.filter { it.courseId == user.courseId }
        }

        val asignaturasAlumno = listaAsignaturas.filter { asignatura ->
            acsAlumno.any { it.subjectId == asignatura.id_asignatura }
        }

        val detallesAsignaturas = asignaturasAlumno.map { asignatura ->
            val acs = acsAlumno.find { it.subjectId == asignatura.id_asignatura }
            val profesor = listaUsuarios.find { it.idUsuario == acs?.profesorId }
            val curso = listaCursos.find { it.id_curso == acs?.courseId }
            "📚 ${asignatura.nombre}\n - Curso: ${curso?.nombre ?: "-"}\n - Profesor: ${profesor?.nombre ?: "-"}"
        }.toTypedArray()

        if (detallesAsignaturas.isEmpty()) toast("No tienes asignaturas asignadas")
        else mostrarDialogo("Mis Asignaturas", detallesAsignaturas)
    }

    private fun mostrarAdvertencias() {
        if (listaAdvertencias.isEmpty()) {
            toast("No tienes advertencias registradas")
            return
        }

        val detallesAdvertencias = listaAdvertencias.map {
            "📚 ${it.nombreAsignatura}\n👨‍🏫 Profesor: ${it.nombreProfesor}\n📝 Motivo: ${it.mensaje}\n"
        }.toTypedArray()

        mostrarDialogo("Mis Advertencias", detallesAdvertencias)
    }

    private fun mostrarExamenes() {
        val user = alumno ?: return toast("Usuario no encontrado")
        val testsAlumno = listaTests.filter { test -> user.subjects?.contains(test.id_acs) == true }

        if (testsAlumno.isEmpty()) {
            toast("No tienes exámenes disponibles")
            return
        }

        val detalles = testsAlumno.map { test ->
            val acs = listaACS.find { it.id == test.id_acs }
            val asignatura = listaAsignaturas.find { it.id_asignatura == acs?.subjectId }?.nombre ?: "Asignatura desconocida"
            val fecha = test.fecha?.substring(0, 10) ?: "-"
            val notaObj = listaNotas.find {
                it.id_acs == test.id_acs && it.descripcion == test.descripcion
            }
            val nota = notaObj?.nota?.toString() ?: "-"
            val fechaNota = notaObj?.fecha?.substring(0, 10) ?: "-"

            "📚 $asignatura\n📝 ${test.descripcion}\n📅 Fecha examen: $fecha\n📈 Nota: $nota\n"
        }.toTypedArray()

        mostrarDialogo("Mis Exámenes", detalles)
    }

    private fun mostrarDialogoNotificacion() {
        val view = layoutInflater.inflate(R.layout.dialog_enviar_notificacion, null)
        val spinner = view.findViewById<Spinner>(R.id.spinnerDestinatario)
        val tituloInput = view.findViewById<EditText>(R.id.editTituloNoti)
        val mensajeInput = view.findViewById<EditText>(R.id.editMensajeNoti)

        val posiblesDestinatarios = listaUsuarios.filter {
            it.idUsuario != idUsuario && (it.idRol == 1 || it.idRol == 2)
        }

        val nombres = listOf("Selecciona destinatario") + posiblesDestinatarios.map {
            val icono = if (it.idRol == 1) "👨‍🎓" else "👨‍🏫"
            "$icono ${it.nombre} ${it.primerApellido}"
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("Enviar Notificación")
            .setView(view)
            .setPositiveButton("Enviar") { _, _ ->
                val destinatario = posiblesDestinatarios.getOrNull(spinner.selectedItemPosition - 1)
                val titulo = tituloInput.text.toString().trim()
                val mensaje = mensajeInput.text.toString().trim()

                if (destinatario == null || titulo.isEmpty() || mensaje.isEmpty()) {
                    toast("Completa todos los campos")
                    return@setPositiveButton
                }

                val noti = NotificacionRequest(destinatario.idUsuario, titulo, mensaje)
                viewModel.enviarNotificacion(noti, getToken())
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun mostrarDialogo(titulo: String, lista: Array<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setItems(lista, null)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun getToken(): String {
        return requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null) ?: ""
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarTodasLasNotificaciones() {
        // Lanzamos la petición una sola vez
        viewModel.getTodasNotificaciones(idUsuario, token)

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
                .setTitle("\uD83D\uDCE5 Todas las Notificaciones")
                .setView(listView)
                .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                .create()

            dialog.show()
        }
    }



}

