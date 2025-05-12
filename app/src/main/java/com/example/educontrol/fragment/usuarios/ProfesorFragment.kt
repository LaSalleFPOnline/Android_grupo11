package com.example.educontrol.fragment.usuarios

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.educontrol.R
import com.example.educontrol.adapter.NotificacionAdapter
import com.example.educontrol.api.ACS
import com.example.educontrol.api.Curso
import com.example.educontrol.api.NoteRequest
import com.example.educontrol.api.NotificacionRequest
import com.example.educontrol.api.NotificacionResponse
import com.example.educontrol.api.Test
import com.example.educontrol.api.TestRequest
import com.example.educontrol.api.Usuario
import com.example.educontrol.api.WarningRequest
import com.example.educontrol.viewmodel.DataViewModel
import java.time.LocalDateTime




class ProfesorFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()

    private lateinit var btnAdvertencia: Button
    private lateinit var btnVerTodosExamenes: Button
    private lateinit var btnExamen: Button
    private lateinit var btnNota: Button
    private lateinit var btnEnviarNoti: Button
    private lateinit var btnVerNotificaciones: Button
    private lateinit var btntodasNotificaciones: Button


    private var listaACS = listOf<ACS>()
    private var listaUsuarios = listOf<Usuario>()
    private var listaTests = listOf<Test>()
    private var listaCursos = listOf<Curso>()
    private var fotoBase64: String? = null
    private var fotoMimeType: String? = null
    private var listaNotificaciones = listOf<NotificacionResponse>()
    private var notificacionAdapter: NotificacionAdapter? = null



    private var token = ""
    private var idProfesor = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profesor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnAdvertencia = view.findViewById(R.id.btnAdvertencia)
        btnExamen = view.findViewById(R.id.btnExamen)
        btnNota = view.findViewById(R.id.btnNota)
        btnVerTodosExamenes = view.findViewById(R.id.btnVerTodosExamenes)
        btnEnviarNoti = view.findViewById(R.id.btnEnviarNoti)
        btnVerNotificaciones = view.findViewById(R.id.btnVerNotificaciones)
        btntodasNotificaciones = view.findViewById(R.id.btntodasNotificaciones)


        btnEnviarNoti.setOnClickListener { mostrarDialogoEnviarNotificacion() }
        btnVerNotificaciones.setOnClickListener { mostrarNotificacionesNoLeidas() }
        btnVerTodosExamenes.setOnClickListener { mostrarTodosLosExamenes() }
        btntodasNotificaciones.setOnClickListener { mostrarTodasLasNotificaciones()}

        token = getToken(requireContext()) ?: ""
        idProfesor = getUserId(requireContext())

        viewModel.getAcs(token)
        viewModel.getUser(token)
        viewModel.getCursos(token)
        viewModel.getTestsPorProfesor(idProfesor, token)
        viewModel.listaAcs.observe(viewLifecycleOwner) { listaACS = it }
        viewModel.listaUsuarios.observe(viewLifecycleOwner) { listaUsuarios = it }
        viewModel.listaCursos.observe(viewLifecycleOwner) { listaCursos = it }
        viewModel.listaTests.observe(viewLifecycleOwner) { listaTests = it }
        viewModel.notificacionesNoLeidas.observe(viewLifecycleOwner) {
            listaNotificaciones = it
            actualizarTextoBotonNotificaciones()
            notificacionAdapter?.updateData(it.toMutableList())
        }
        viewModel.enviarNotificacionStatus.observe(viewLifecycleOwner) { toast(it)} // muestra el mensaje que devuelve el servidor
        viewModel.getNotificacionesNoLeidas(idProfesor, token)
        viewModel.insertWarningStatus.observe(viewLifecycleOwner) { toast(it) }
        viewModel.insertTestStatus.observe(viewLifecycleOwner) {
            toast(it)
            viewModel.getTestsPorProfesor(idProfesor, token) //  Refrescar lista tras insertar
        }
        viewModel.insertNoteStatus.observe(viewLifecycleOwner) { toast(it) }



        //viewModel.enviarNotificacionStatus.observe(viewLifecycleOwner) { toast(it) }

        btnAdvertencia.setOnClickListener { mostrarDialogoAdvertencia() }
        btnExamen.setOnClickListener { mostrarDialogoExamen() }
        btnNota.setOnClickListener { mostrarDialogoNota() }
    }

    private fun mostrarDialogoAdvertencia() {
        val acsProfesor = listaACS.filter { it.profesorId == idProfesor }
        val cursosProfesor = acsProfesor.map { it.courseId }.distinct()
        val alumnos = listaUsuarios.filter { it.idRol == 2 && it.courseId in cursosProfesor }

        if (alumnos.isEmpty()) {
            toast("No hay alumnos disponibles")
            return
        }

        if (acsProfesor.isEmpty()) {
            toast("No tienes asignaturas asignadas")
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_nueva_advertencia, null)
        val spinnerAlumno = dialogView.findViewById<Spinner>(R.id.spinnerAlumno)
        val spinnerACS = dialogView.findViewById<Spinner>(R.id.spinnerACS)
        val motivoInput = dialogView.findViewById<EditText>(R.id.editMotivo)

        val nombresAlumnos = listOf("👥 Todos los alumnos") + alumnos.map { "👤 ${it.nombre} ${it.primerApellido}" }
        val alumnoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresAlumnos)
        alumnoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAlumno.adapter = alumnoAdapter

        spinnerAlumno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val alumnosFiltrados = if (pos == 0) alumnos else listOfNotNull(alumnos.getOrNull(pos - 1))
                val cursos = alumnosFiltrados.mapNotNull { it.courseId }.distinct()
                val acsFiltradas = acsProfesor.filter { it.courseId in cursos }

                val nombresAcs = listOf("Selecciona una asignatura") + acsFiltradas.map { acs ->
                    val asignatura = acs.nombre_asignatura ?: ""
                    val cursoObj = listaCursos.find { it.id_curso == acs.courseId }
                    val nombreCurso = cursoObj?.nombre ?: ""
                    val grupo = cursoObj?.grupo ?: ""

                    val cursoGrupo = listOfNotNull(nombreCurso.takeIf { it.isNotBlank() }, grupo.takeIf { it.isNotBlank() })
                        .joinToString(" ")

                    listOfNotNull(asignatura.takeIf { it.isNotBlank() }, cursoGrupo.takeIf { it.isNotBlank() })
                        .joinToString(" - ")
                }

                val acsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresAcs)
                acsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerACS.adapter = acsAdapter
                spinnerACS.tag = acsFiltradas
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva Advertencia")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val todosSeleccionado = spinnerAlumno.selectedItemPosition == 0
                val alumnosDestino = if (todosSeleccionado) alumnos else listOfNotNull(alumnos.getOrNull(spinnerAlumno.selectedItemPosition - 1))

                val acsLista = spinnerACS.tag as? List<ACS>
                val acs = acsLista?.getOrNull(spinnerACS.selectedItemPosition - 1)
                val motivo = motivoInput.text.toString().trim()

                if (alumnosDestino.isEmpty() || acs == null || motivo.isEmpty()) {
                    toast("Completa todos los campos antes de guardar")
                    return@setPositiveButton
                }

                val warning = WarningRequest(
                    usersIds = alumnosDestino.map { it.idUsuario },
                    id_acs = acs.id ?: 0,
                    message = motivo
                )

                viewModel.insertWarning(warning, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }





    private fun mostrarDialogoExamen() {
        val view = layoutInflater.inflate(R.layout.dialog_nuevo_examen, null)
        val spinnerACS = view.findViewById<Spinner>(R.id.spinnerACS)
        val descripcion = view.findViewById<EditText>(R.id.editDescripcionExamen)
        val fechaExamen = view.findViewById<EditText>(R.id.editFechaExamen)
        val btnImagen = view.findViewById<Button>(R.id.btnSeleccionarImagen)
        val imageExamen = view.findViewById<ImageView>(R.id.imageExamen)

        var fechaSeleccionada: String? = null

        val acsProfesor = listaACS.filter { it.profesorId == idProfesor }

        val nombresAcs = listOf("Selecciona una asignatura") + acsProfesor.map { acs ->
            val asignatura = acs.nombre_asignatura ?: ""
            val cursoObj = listaCursos.find { it.id_curso == acs.courseId }
            val nombreCurso = cursoObj?.nombre ?: ""
            val grupo = cursoObj?.grupo ?: ""

            listOfNotNull(asignatura, "$nombreCurso $grupo".trim()).joinToString(" - ")

        }

        spinnerACS.adapter = crearAdapter(nombresAcs)

        // 📅 Selección de fecha
        fechaExamen.setOnClickListener {
            val datePicker = DatePickerDialog(requireContext())
            datePicker.setOnDateSetListener { _, year, month, dayOfMonth ->
                fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                fechaExamen.setText(fechaSeleccionada)
            }
            datePicker.show()
        }

        btnImagen.setOnClickListener {
            abrirGaleria.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nuevo Examen")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val acs = acsProfesor.getOrNull(spinnerACS.selectedItemPosition - 1)
                val descripcionTexto = descripcion.text.toString().trim()

                if (acs == null || descripcionTexto.isEmpty() || fechaSeleccionada.isNullOrEmpty()) {
                    toast("Completa todos los campos correctamente")
                    return@setPositiveButton
                }

                val test = TestRequest(
                    descripcion = descripcionTexto,
                    fecha = fechaSeleccionada!!,
                    photo = fotoBase64,
                    phototype = fotoMimeType
                )

                viewModel.insertTest(test, acs.id ?: 0, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun mostrarDialogoNota() {
        val view = layoutInflater.inflate(R.layout.dialog_nueva_nota, null)
        val spinnerAlumno = view.findViewById<Spinner>(R.id.spinnerAlumno)
        val spinnerTest = view.findViewById<Spinner>(R.id.spinnerTest)
        val notaInput = view.findViewById<EditText>(R.id.editNota)

        val alumnos = listaUsuarios.filter { it.idRol == 2 && it.subjects?.isNotEmpty() == true }

        if (alumnos.isEmpty()) {
            Log.w("NotaDebug", "No hay alumnos con asignaturas asignadas")
            toast("No hay alumnos con asignaturas asignadas")
            return
        }

        val nombresAlumnos = listOf("Selecciona un alumno") + alumnos.map { "👤 ${it.nombre} ${it.primerApellido}" }
        val alumnoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresAlumnos)
        alumnoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAlumno.adapter = alumnoAdapter

        spinnerAlumno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    spinnerTest.adapter = crearAdapter(listOf("Selecciona un examen"))
                    spinnerTest.tag = emptyList<Test>()
                    return
                }

                val alumno = alumnos[pos - 1]
                Log.d("NotaDebug", "Alumno seleccionado: ${alumno.nombre} (${alumno.idUsuario})")

                val tests = listaTests.filter { test ->
                    val match = test.id_test > 0 &&
                            alumno.subjects?.contains(test.id_acs) == true &&
                            listaACS.any { acs -> acs.id == test.id_acs && acs.profesorId == idProfesor }

                    Log.d("NotaDebug", "Evaluando test: ${test.descripcion} -> $match")
                    match
                }

                Log.d("NotaDebug", "Tests disponibles: ${tests.map { it.descripcion }}")
                val nombresTests = listOf("Selecciona un examen") + tests.map { it.descripcion }
                spinnerTest.adapter = crearAdapter(nombresTests)
                spinnerTest.tag = tests
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva Nota")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val alumno = alumnos.getOrNull(spinnerAlumno.selectedItemPosition - 1)
                val tests = spinnerTest.tag as? List<Test>
                val test = tests?.getOrNull(spinnerTest.selectedItemPosition - 1)
                val notaTexto = notaInput.text.toString().trim()
                val nota = notaTexto.toDoubleOrNull()

                Log.d("NotaDebug", "Token: $token")
                Log.d("NotaDebug", "Nota texto: '$notaTexto', convertida: $nota")

                if (alumno == null || test == null || nota == null) {
                    Log.w("NotaDebug", "Campos inválidos: alumno=$alumno, test=$test, nota=$nota")
                    toast("⚠️ Completa todos los campos correctamente")
                    return@setPositiveButton
                }

                val notaRequest = NoteRequest(
                    idUsuario = alumno.idUsuario.toString(),
                    idExamen = test.id_test,
                    idAcs = test.id_acs,
                    nota = nota,
                    photo = fotoBase64,
                    phototype = fotoMimeType
                )

                Log.d("NotaDebug", "Enviando nota: $notaRequest")
                viewModel.insertNote(notaRequest, token)

                val nombreAlumno = "${alumno.nombre} ${alumno.primerApellido}"
                val mensajeNoti = "📈 Se ha registrado la nota del examen '${test.descripcion}' para $nombreAlumno"
                val noti = NotificacionRequest(
                    id_usuario = alumno.idUsuario,
                    titulo = "Nueva nota registrada",
                    mensaje = mensajeNoti,
                    leido = false
                )
                Log.d("NotaDebug", "Enviando notificación al alumno: $noti")
                viewModel.enviarNotificacion(noti, token)

                val tutor = listaUsuarios.find { it.idRol == 3 && it.courseId == alumno.courseId }

                if (tutor != null) {
                    val mensajeNotiTutor = "📚 El alumno $nombreAlumno ha recibido una nota en '${test.descripcion}'"
                    val notiTutor = NotificacionRequest(
                        id_usuario = tutor.idUsuario,
                        titulo = "Nota registrada para un alumno",
                        mensaje = mensajeNotiTutor,
                        leido = false
                    )
                    Log.d("NotaDebug", "Enviando notificación al tutor: $notiTutor")
                    viewModel.enviarNotificacion(notiTutor, token)
                } else {
                    Log.d("NotaDebug", "No se encontró tutor para el alumno $nombreAlumno")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }






    private fun crearAdapter(lista: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun getToken(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    private fun getUserId(context: Context): Int {
        return context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_ID", -1)
    }

    private fun toast(msg: String?) {
        Toast.makeText(requireContext(), msg ?: "Error", Toast.LENGTH_SHORT).show()
    }

    private val abrirGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val inputStream = requireContext().contentResolver.openInputStream(it)
            val bytes = inputStream?.readBytes()
            fotoBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            fotoMimeType = requireContext().contentResolver.getType(it)

            view?.findViewById<ImageView>(R.id.imageExamen)?.setImageURI(it)
        }
    }

    private fun mostrarTodosLosExamenes() {
        val testsProfesor = listaTests.filter { test ->
            listaACS.any { acs -> acs.id == test.id_acs && acs.profesorId == idProfesor }
        }

        if (testsProfesor.isEmpty()) {
            toast("No has creado ningún examen")
            return
        }

        val detalles = testsProfesor.map { test ->
            val acs = listaACS.find { it.id == test.id_acs }
            val asignatura = acs?.nombre_asignatura ?: "Asignatura desconocida"
            val fecha = test.fecha?.substring(0, 10) ?: "-"
            "📚 $asignatura\n📝 ${test.descripcion}\n📅 Fecha: $fecha\n"
        }.toTypedArray()

        mostrarDialogo("Todos mis Exámenes", detalles)
    }

    private fun mostrarDialogo(titulo: String, lista: Array<String>) {
        AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setItems(lista, null)
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

    private fun mostrarNotificacionesNoLeidas() {
        if (listaNotificaciones.isEmpty()) {
            toast("No tienes notificaciones nuevas")
            return
        }

        val listView = ListView(requireContext())
        notificacionAdapter = NotificacionAdapter(requireContext(), listaNotificaciones.toMutableList()) { noti ->
            viewModel.marcarNotificacionLeida(noti.id_notificacion, token)
            toast("Notificación marcada como leída")
            viewModel.getNotificacionesNoLeidas(idProfesor, token)
        }
        listView.adapter = notificacionAdapter

        AlertDialog.Builder(requireContext())
            .setTitle("Notificaciones no leídas")
            .setView(listView)
            .setPositiveButton("Cerrar", null)
            .show()
    }

    private fun mostrarDialogoEnviarNotificacion() {
        val view = layoutInflater.inflate(R.layout.dialog_enviar_notificacion, null)
        val spinnerTipo = view.findViewById<Spinner>(R.id.spinnerTipo)
        val spinnerDestinatario = view.findViewById<Spinner>(R.id.spinnerDestinatario)
        val tituloInput = view.findViewById<EditText>(R.id.editTituloNoti)
        val mensajeInput = view.findViewById<EditText>(R.id.editMensajeNoti)

        val alumnos = listaUsuarios.filter { it.idRol == 2 } // Alumnos
        val tutores = listaUsuarios.filter { it.idRol == 3 } // Tutores

        val tipos = listOf("Selecciona tipo de destinatario", "👨‍🏫 Tutor", "👤 Alumno")
        val tipoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos)
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipo.adapter = tipoAdapter

        val destinatarios = mutableListOf<Usuario?>()
        val nombresDestinatarios = mutableListOf<String>()

        spinnerTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                destinatarios.clear()
                nombresDestinatarios.clear()

                when (position) {
                    1 -> { // Tutor
                        tutores.forEach {
                            nombresDestinatarios.add("👨‍🏫 ${it.nombre} ${it.primerApellido}")
                            destinatarios.add(it)
                        }
                    }
                    2 -> { // Alumno
                        nombresDestinatarios.add("👥 Todos los alumnos")
                        destinatarios.add(null) // marcador especial para "todos"
                        alumnos.forEach {
                            nombresDestinatarios.add("👤 ${it.nombre} ${it.primerApellido}")
                            destinatarios.add(it)
                        }
                    }
                }

                val destAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
                    listOf("Selecciona destinatario") + nombresDestinatarios)
                destAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDestinatario.adapter = destAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Enviar Notificación")
            .setView(view)
            .setPositiveButton("Enviar") { _, _ ->
                val tipoSeleccionado = spinnerTipo.selectedItemPosition
                val usuarioSeleccionado = spinnerDestinatario.selectedItemPosition
                val titulo = tituloInput.text.toString().trim()
                val mensaje = mensajeInput.text.toString().trim()

                if (tipoSeleccionado == 0 || usuarioSeleccionado == 0 || titulo.isEmpty() || mensaje.isEmpty()) {
                    toast("Completa todos los campos")
                    return@setPositiveButton
                }

                val usuariosDestino = when {
                    tipoSeleccionado == 2 && usuarioSeleccionado == 1 -> alumnos // "Todos los alumnos"
                    else -> listOfNotNull(destinatarios[usuarioSeleccionado - 1]) // uno solo
                }

                usuariosDestino.forEach {
                    val noti = NotificacionRequest(it.idUsuario, titulo, mensaje, leido = false)
                    viewModel.enviarNotificacion(noti, token)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarTodasLasNotificaciones() {
        // Lanzamos la petición una sola vez
        viewModel.getTodasNotificaciones(idProfesor, token)

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


