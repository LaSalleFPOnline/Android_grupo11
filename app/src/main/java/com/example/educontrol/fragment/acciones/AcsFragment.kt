package com.example.educontrol.fragment.acciones

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.adapter.AcsAdapter
import com.example.educontrol.api.*
import com.example.educontrol.viewmodel.DataViewModel


class AcsFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: AcsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""

    private var listaAcs = listOf<ACS>()
    private var profesores = listOf<Profesor>()
    private var cursos = listOf<Curso>()
    private var asignaturas = listOf<Asignatura>()
    private var semestres = listOf<Semestre>()
    private var colegioId: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_acs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewAcs)
        btnAgregar = view.findViewById(R.id.btnAgregarAcs)
        searchView = view.findViewById(R.id.searchViewAcs)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AcsAdapter(
            onUpdate = { mostrarDialogoAcs(it) },
            onDelete = { eliminarAcs(it) }
        )
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return

        colegioId = obtenerColegioIdDesdePrefs()
        Log.d("educontrol", "🏫 ID del colegio actual: $colegioId")

        cargarDatos()
        btnAgregar.setOnClickListener { mostrarDialogoAcs() }
        setupSearchView()
    }

    private fun cargarDatos() {
        viewModel.getAcs(token)
        viewModel.listaAcs.observe(viewLifecycleOwner) {
            val filtrado = it.filter { acs -> acs.id_colegio == colegioId }
            Log.d("educontrol", "🎯 ACS filtrados: ${filtrado.size} / Total: ${it.size}")
            listaAcs = filtrado
            adapter.submitList(filtrado)
        }

        viewModel.getProfesores(token)
        viewModel.listaProfesores.observe(viewLifecycleOwner) {
            profesores = it.filter { prof -> prof.id_colegio == colegioId }
            Log.d("educontrol", "👨‍🏫 Profesores filtrados: ${profesores.size}")
        }

        viewModel.getCursos(token)
        viewModel.listaCursos.observe(viewLifecycleOwner) {
            cursos = it.filter { curso -> curso.id_colegio == colegioId }
            Log.d("educontrol", "📘 Cursos filtrados: ${cursos.size}")
        }

        viewModel.getSubjects(token)
        viewModel.listaAsignaturas.observe(viewLifecycleOwner) {
            asignaturas = it
        }

        viewModel.getSemestres(token)
        viewModel.listaSemestres.observe(viewLifecycleOwner) {
            semestres = it
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { filtrar(query) }
            override fun onQueryTextChange(newText: String?) = true.also { filtrar(newText) }
        })
    }

    private fun filtrar(query: String?) {
        val texto = query.orEmpty().lowercase()
        val filtrado = listaAcs.filter {
            it.nombre_profesor?.contains(texto, true) == true ||
                    it.nombre_curso?.contains(texto, true) == true ||
                    it.nombre_asignatura?.contains(texto, true) == true ||
                    it.fecha_semestre?.contains(texto, true) == true
        }
        adapter.submitList(filtrado)
    }

    private fun mostrarDialogoAcs(acs: ACS? = null) {
        val view = layoutInflater.inflate(R.layout.dialog_actualizar_acs, null)
        val spProfesor = view.findViewById<Spinner>(R.id.spinnerProfesor)
        val spCurso = view.findViewById<Spinner>(R.id.spinnerCurso)
        val spAsignatura = view.findViewById<Spinner>(R.id.spinnerAsignatura)
        val spSemestre = view.findViewById<Spinner>(R.id.spinnerSemestre)

        // Agregamos los placeholders al principio de cada lista
        val nombreProfesores = listOf("Selecciona un profesor...") + profesores.map { it.nombre }
        val nombreCursos = listOf("Selecciona un curso...") + cursos.map { it.nombre }
        val nombreAsignaturas = listOf("Selecciona una asignatura...") + asignaturas.map { it.nombre }
        val nombreSemestres = listOf("Selecciona un semestre...") + semestres.map { it.numero_semestre }

        // Adaptadores con placeholder
        spProfesor.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombreProfesores).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spCurso.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombreCursos).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spAsignatura.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombreAsignaturas).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        spSemestre.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombreSemestres).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Si se va a editar, seleccionamos los valores (offset +1 por el placeholder)
        acs?.let {
            spProfesor.setSelection(profesores.indexOfFirst { it.id_profesor == acs.profesorId } + 1)
            spCurso.setSelection(cursos.indexOfFirst { it.id_curso == acs.courseId } + 1)
            spAsignatura.setSelection(asignaturas.indexOfFirst { it.id_asignatura == acs.subjectId } + 1)
            spSemestre.setSelection(semestres.indexOfFirst { it.id_semestre == acs.semesterId } + 1)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (acs == null) "Nueva Asignación" else "Actualizar Asignación")
            .setView(view)
            .setPositiveButton(if (acs == null) "Guardar" else "Actualizar") { _, _ ->
                // Obtenemos seleccionados (restamos 1 por el placeholder)
                val profesor = profesores.getOrNull(spProfesor.selectedItemPosition - 1)
                val curso = cursos.getOrNull(spCurso.selectedItemPosition - 1)
                val asignatura = asignaturas.getOrNull(spAsignatura.selectedItemPosition - 1)
                val semestre = semestres.getOrNull(spSemestre.selectedItemPosition - 1)

                if (profesor == null || curso == null || asignatura == null || semestre == null) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nuevoAcs = ACSAdd(
                    profesorId = profesor.id_profesor,
                    courseId = curso.id_curso,
                    subjectId = asignatura.id_asignatura,
                    semesterId = semestre.id_semestre
                )

                if (acs == null) {
                    viewModel.addAcs(nuevoAcs, token) { viewModel.getAcs(token) }
                } else {
                    viewModel.updateAcs(acs.id!!, nuevoAcs, token) { viewModel.getAcs(token) }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun eliminarAcs(acs: ACS) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Asignación")
            .setMessage("¿Estás seguro de eliminar esta asignación?")
            .setPositiveButton("Sí") { _, _ ->
                Log.d("educontrol", "🗑️ Eliminando ACS con ID: ${acs.id}")
                viewModel.deleteAcs(acs.id!!, token) {
                    Log.d("educontrol", "✅ ACS eliminado, refrescando")
                    viewModel.getAcs(token)
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }

    private fun obtenerColegioIdDesdePrefs(): Int {
        val prefs = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
        return prefs.getInt("USER_COLEGIO_ID", -1)
    }
}


