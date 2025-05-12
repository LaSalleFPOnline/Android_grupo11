package com.example.educontrol.fragment.acciones

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.adapter.CursoAdapter
import com.example.educontrol.api.Colegio
import com.example.educontrol.api.Curso
import com.example.educontrol.api.CursoAdd
import com.example.educontrol.viewmodel.DataViewModel

class CourseFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: CursoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var btnAgregar: Button
    private var token: String = ""
    private var colegioId: Int = -1

    private var listaCursos = listOf<Curso>()
    private var listaColegios = listOf<Colegio>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewCursos)
        searchView = view.findViewById(R.id.searchViewCursos)
        btnAgregar = view.findViewById(R.id.btnAgregarCurso)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = CursoAdapter(
            onUpdate = { mostrarDialogoCurso(it) },
            onDelete = { eliminarCurso(it) }
        )
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return
        colegioId = obtenerColegioUsuario()

        viewModel.getSchool(token)
        viewModel.listaColegios.observe(viewLifecycleOwner) {
            listaColegios = it.filter { col -> col.id_colegio == colegioId }
        }

        viewModel.getCursos(token)
        viewModel.listaCursos.observe(viewLifecycleOwner) {
            listaCursos = it.filter { curso -> curso.id_colegio == colegioId }
            adapter.submitList(listaCursos)
        }

        btnAgregar.setOnClickListener { mostrarDialogoCurso() }
        setupSearchView()
    }

    private fun mostrarDialogoCurso(curso: Curso? = null) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_actualizar_curso, null)
        val etNombre = view.findViewById<EditText>(R.id.etNuevoNombreCurso)
        val etGrupo = view.findViewById<EditText>(R.id.etNuevoGrupoCurso)
        val spColegios = view.findViewById<Spinner>(R.id.spinnerColegiosCurso)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarCurso)

        val nombreColegio = listaColegios.firstOrNull()?.nombre ?: "Desconocido"
        val nombresColegios = listOf("Selecciona un colegio", nombreColegio)
        spColegios.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresColegios).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        if (curso != null) {
            etNombre.setText(curso.nombre)
            etGrupo.setText(curso.grupo)
            spColegios.setSelection(1)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (curso == null) "Nuevo Curso" else "Actualizar Curso")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val grupo = etGrupo.text.toString().trim()
            val seleccionValida = spColegios.selectedItemPosition == 1

            if (nombre.isEmpty() || grupo.isEmpty() || !seleccionValida) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datos = CursoAdd(nombre, grupo, colegioId)

            if (curso == null) {
                viewModel.addCurso(requireContext(), datos, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getCursos(token)
                        cerrarTeclado()
                        dialog.dismiss()
                    }
                }
            } else {
                viewModel.updateCurso(curso.id_curso, datos, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getCursos(token)
                        cerrarTeclado()
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun eliminarCurso(curso: Curso) {
        viewModel.deleteCurso(curso.id_curso, token) { success, msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            if (success) viewModel.getCursos(token)
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                filtrar(query)
                cerrarTeclado()
            }

            override fun onQueryTextChange(newText: String?) = true.also {
                filtrar(newText)
            }
        })
    }

    private fun filtrar(query: String?) {
        val texto = query.orEmpty().lowercase()
        val filtrado = listaCursos.filter {
            it.nombre.contains(texto, true) || it.grupo.contains(texto, true) || it.nombre_colegio?.contains(texto, true) == true
        }
        adapter.submitList(filtrado)
    }

    private fun cerrarTeclado() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun obtenerColegioUsuario(): Int {
        return requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)
    }

    private fun getToken(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).getString("auth_token", null)
    }
}


