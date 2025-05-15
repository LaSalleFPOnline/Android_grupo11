// ✅ ARCHIVO: UsuarioFragment.kt (con LiveData para acciones)

package com.example.educontrol.fragment.acciones

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.educontrol.R
import com.example.educontrol.adapter.UsuarioAdapter
import com.example.educontrol.api.*
import com.example.educontrol.viewmodel.DataViewModel
import java.io.ByteArrayOutputStream

class UsuarioFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UsuarioAdapter
    private lateinit var btnAgregar: Button

    private var token: String = ""
    private var colegioId: Int = -1
    private var listaUsuarios = listOf<Usuario>()
    private var listaRoles = listOf<RolResponse>()
    private var listaCursos = listOf<Curso>()
    private var listaAsignaturas = listOf<Asignatura>()
    private var listaEstados = listOf<Estado>()
    private var listaACS = listOf<ACS>()
    private lateinit var camaraLauncher: ActivityResultLauncher<Intent>
    private var fotoBase64: String? = null
    private var ivFotoActual: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        camaraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val bitmap = result.data?.extras?.get("data") as? Bitmap ?: return@registerForActivityResult
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            fotoBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            ivFotoActual?.setImageBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_usuario, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewUsuarios)
        btnAgregar = view.findViewById(R.id.btnAgregarUsuario)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = UsuarioAdapter(
            onEdit = { mostrarDialogoUsuario(it) },
            onDelete = { eliminarUsuario(it) }
        )
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: ""
        colegioId = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)

        viewModel.getUser(token)
        viewModel.getRoles(token)
        viewModel.getCursos(token)
        viewModel.getSubjects(token)
        viewModel.getState(token)
        viewModel.getAcs(token)

        viewModel.listaUsuarios.observe(viewLifecycleOwner) {
            listaUsuarios = it.filter { u -> u.idColegio == colegioId }
            adapter.submitList(listaUsuarios)
        }

        viewModel.listaRoles.observe(viewLifecycleOwner) { listaRoles = it }
        viewModel.listaCursos.observe(viewLifecycleOwner) {
            listaCursos = it.filter { c -> c.id_colegio == colegioId }
        }
        viewModel.listaAsignaturas.observe(viewLifecycleOwner) { listaAsignaturas = it }
        viewModel.listaEstados.observe(viewLifecycleOwner) { listaEstados = it }
        viewModel.listaAcs.observe(viewLifecycleOwner) { listaACS = it }

        viewModel.usuarioAccionResultado.observe(viewLifecycleOwner) { mensaje ->
            mensaje?.let {
                toast(it)
                viewModel.usuarioAccionResultado.value = null
                viewModel.getUser(token)
            }
        }

        btnAgregar.setOnClickListener { mostrarDialogoUsuario() }
    }

    private fun mostrarDialogoUsuario(usuario: Usuario? = null) {
        val view = layoutInflater.inflate(R.layout.dialog_actualizar_usuario, null)
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etApellido1 = view.findViewById<EditText>(R.id.etApellido1)
        val etApellido2 = view.findViewById<EditText>(R.id.etApellido2)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etMovil = view.findViewById<EditText>(R.id.etTelefono)
//        val etCodigo = view.findViewById<EditText>(R.id.etCodigoActivacion)
//        val btnGenerarCodigo = view.findViewById<ImageButton>(R.id.btnGenerarCodigo)
        val spinnerRol = view.findViewById<Spinner>(R.id.spinnerRol)
        val spinnerCurso = view.findViewById<Spinner>(R.id.spinnerCurso)
        val spinnerEstado = view.findViewById<Spinner>(R.id.spinnerEstado)
        val tvAsignaturas = view.findViewById<TextView>(R.id.tvAsignaturasSeleccionadas)
        val btnFoto = view.findViewById<Button>(R.id.btnFotoUsuario)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarUsuario)
        val ivFoto = view.findViewById<ImageView>(R.id.ivFotoUsuario)
        ivFotoActual = ivFoto


        val asignaturasSeleccionadas = mutableListOf<Int>()

        val nombresRoles = listOf("Selecciona un rol") + listaRoles.map { it.nombre }
        val nombresCursos = listOf("Selecciona un curso") + listaCursos.map { "${it.nombre} - ${it.grupo}" }
        val nombresEstados = listOf("Selecciona un estado") + listaEstados.map { it.nombre }

        spinnerRol.adapter = crearAdapter(nombresRoles)
        spinnerCurso.adapter = crearAdapter(nombresCursos)
        spinnerEstado.adapter = crearAdapter(nombresEstados)

//        btnGenerarCodigo.setOnClickListener {
//            val nuevoCodigo = generarCodigoActivacion()
//            etCodigo.setText(nuevoCodigo)
//            Toast.makeText(requireContext(), "Código generado: $nuevoCodigo", Toast.LENGTH_SHORT).show()
//        }

        btnFoto.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            camaraLauncher.launch(intent)
        }

        spinnerCurso.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val curso = listaCursos.getOrNull(position - 1)
                if (curso != null) {
                    val asignaturasFiltradas = listaACS.filter { it.courseId == curso.id_curso }.map { it.subjectId }
                    asignaturasSeleccionadas.clear()
                    asignaturasSeleccionadas.addAll(asignaturasFiltradas)
                    val nombres = listaAsignaturas.filter { it.id_asignatura in asignaturasSeleccionadas }.joinToString { it.nombre }
                    tvAsignaturas.text = nombres
                }
            }
        }

        tvAsignaturas.setOnClickListener {
            val cursoSeleccionado = listaCursos.getOrNull(spinnerCurso.selectedItemPosition - 1)
            val asignaturasCurso = listaACS
                .filter { it.courseId == cursoSeleccionado?.id_curso }
                .mapNotNull { acs -> listaAsignaturas.find { it.id_asignatura == acs.subjectId } }

            val nombresAsignaturas = asignaturasCurso.map { it.nombre }.toTypedArray()
            val seleccionados = asignaturasCurso.map { asignaturasSeleccionadas.contains(it.id_asignatura) }.toBooleanArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Selecciona asignaturas")
                .setMultiChoiceItems(nombresAsignaturas, seleccionados) { _, index, isChecked ->
                    val id = asignaturasCurso[index].id_asignatura
                    if (isChecked) {
                        if (!asignaturasSeleccionadas.contains(id)) asignaturasSeleccionadas.add(id)
                    } else {
                        asignaturasSeleccionadas.remove(id)
                    }
                }
                .setPositiveButton("Aceptar") { _, _ ->
                    val nombres = listaAsignaturas
                        .filter { it.id_asignatura in asignaturasSeleccionadas }
                        .joinToString { it.nombre }
                    tvAsignaturas.text = nombres
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        usuario?.let {
            etNombre.setText(it.nombre)
            etApellido1.setText(it.primerApellido)
            etApellido2.setText(it.segundoApellido ?: "")
            etEmail.setText(it.email)
            etMovil.setText(it.movil ?: "")
//            etCodigo.setText(it.codigoActivacion ?: "")
            fotoBase64 = null

            spinnerRol.setSelection(listaRoles.indexOfFirst { r -> r.id == it.idRol } + 1)
            spinnerCurso.setSelection(listaCursos.indexOfFirst { c -> c.id_curso == it.courseId } + 1)
            spinnerEstado.setSelection(listaEstados.indexOfFirst { e -> e.id_estado == it.idEstado } + 1)

            val fotoParaMostrar = it.photo
            if (!fotoParaMostrar.isNullOrEmpty()) {
                try {
                    val base64Clean = if (fotoParaMostrar.contains(",")) fotoParaMostrar.substringAfter(",") else fotoParaMostrar
                    val decodedBytes = Base64.decode(base64Clean, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ivFoto.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    ivFoto.setImageResource(R.drawable.ic_account_profile)
                }
            } else {
                ivFoto.setImageResource(R.drawable.ic_account_profile)
            }

            asignaturasSeleccionadas.clear()
            asignaturasSeleccionadas.addAll(it.subjects ?: emptyList())
            val nombres = listaAsignaturas.filter { a -> a.id_asignatura in asignaturasSeleccionadas }.joinToString { a -> a.nombre }
            tvAsignaturas.text = nombres
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (usuario == null) "Nuevo Usuario" else "Editar Usuario")
            .setView(view)
            .create()

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellido1 = etApellido1.text.toString().trim()
            val apellido2 = etApellido2.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val movil = etMovil.text.toString().trim()
            val codigo ="1234"
            val rol = listaRoles.getOrNull(spinnerRol.selectedItemPosition - 1)
            val curso = listaCursos.getOrNull(spinnerCurso.selectedItemPosition - 1)
            val estado = listaEstados.getOrNull(spinnerEstado.selectedItemPosition - 1)

            if (nombre.isEmpty() || apellido1.isEmpty() || email.isEmpty() || rol == null || estado == null) {
                toast("Completa los campos obligatorios")
                return@setOnClickListener
            }

            val idsACS = listaACS.filter {
                it.courseId == curso?.id_curso && asignaturasSeleccionadas.contains(it.subjectId)
            }.mapNotNull { it.id }

            if (idsACS.isEmpty()) {
                toast("No se encontraron asignaciones (ACS) válidas para las asignaturas seleccionadas.")
                return@setOnClickListener
            }

            val finalPhoto = fotoBase64 ?: usuario?.photo

            val data = UsuarioAdd(
                email = email,
                name = nombre,
                middlename = apellido1,
                lastname = apellido2,
                phone = movil,
                multimediaId = null,
                rolId = rol.id,
                stateId = estado.id_estado,
                schoolId = colegioId,
                photo = finalPhoto,
                phototype = "image/jpeg",
                codigoActivacion = codigo,
                subjects = idsACS,
                courseId = curso?.id_curso
            )

            if (usuario == null) {
                viewModel.addUser(data, token)
            } else {
                viewModel.updateUsuario(usuario.idUsuario, data, token)
                val usuarioActualizado = usuario.copy(
                    nombre = data.name,
                    primerApellido = data.middlename,
                    segundoApellido = data.lastname,
                    email = data.email,
                    movil = data.phone,
                    photo = data.photo,
                    courseId = data.courseId,
                    subjects = data.subjects
                )
                listaUsuarios = listaUsuarios.map {
                    if (it.idUsuario == usuario.idUsuario) usuarioActualizado else it
                }
                adapter.submitList(listaUsuarios.toList())
                adapter.notifyDataSetChanged()
            }
//            val prefs = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
//            prefs.edit().putString("CODIGO_ACTIVACION", codigo).apply()


            dialog.dismiss()
            cerrarTeclado()
        }

        dialog.show()
    }


    private fun crearAdapter(lista: List<String>): ArrayAdapter<String> {
        return ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, lista).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun eliminarUsuario(usuario: Usuario) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar usuario")
            .setMessage("¿Seguro que quieres eliminar este usuario?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.deleteUsuario(usuario.idUsuario, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cerrarTeclado() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun toast(msg: String?) {
        Toast.makeText(requireContext(), msg ?: "Error", Toast.LENGTH_SHORT).show()
    }

    private fun getToken(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

//    private fun generarCodigoActivacion(): String {
//        return (100000..999999).random().toString()
//    }
}












