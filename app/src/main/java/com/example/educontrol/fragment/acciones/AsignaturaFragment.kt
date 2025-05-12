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
import com.example.educontrol.adapter.AsignaturaAdapter
import com.example.educontrol.api.Asignatura
import com.example.educontrol.api.AsignaturaAdd
import com.example.educontrol.viewmodel.DataViewModel

class AsignaturaFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AsignaturaAdapter
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""
    private var colegioId: Int = -1
    private var listaAsignaturas = listOf<Asignatura>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_asignatura, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewSubjects)
        btnAgregar = view.findViewById(R.id.btnAgregarAsignatura)
        searchView = view.findViewById(R.id.searchViewSubjects)

        token = getToken(requireContext()) ?: return
        colegioId = obtenerColegioId()

        adapter = AsignaturaAdapter(
            onUpdate = { mostrarDialogoAsignatura(it) },
            onDelete = { eliminarAsignatura(it) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.getSubjects(token)
        viewModel.listaAsignaturas.observe(viewLifecycleOwner) { asignaturas ->
            // Si quieres filtrar por colegio:
            // listaAsignaturas = asignaturas.filter { it.id_colegio == colegioId }

            listaAsignaturas = asignaturas // o el filtrado si lo tienes
            adapter.submitList(listaAsignaturas)
        }

        btnAgregar.setOnClickListener {
            mostrarDialogoAsignatura()
        }

        setupSearchView()
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
        val filtrado = listaAsignaturas.filter {
            it.nombre.lowercase().contains(texto)
        }
        adapter.submitList(filtrado)
    }

    private fun mostrarDialogoAsignatura(asignatura: Asignatura? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_actualizar_asignatura, null)

        val etNombre = view.findViewById<EditText>(R.id.etNuevoNombreAsignatura)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarAsignatura)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)

        tvTitulo.text = if (asignatura == null) "Nueva Asignatura" else "Actualizar Asignatura"
        btnGuardar.text = if (asignatura == null) "Guardar" else "Actualizar"
        etNombre.setText(asignatura?.nombre ?: "")
        etNombre.hint = "Nombre de la asignatura"

        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            if (nombre.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val nueva = AsignaturaAdd(nombre)

            if (asignatura == null) {
                viewModel.addSubject(nueva, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        cerrarTeclado()
                        dialog.dismiss()
                    }
                }
            } else {
                viewModel.updateSubject(asignatura.id_asignatura, nueva, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        cerrarTeclado()
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun eliminarAsignatura(asignatura: Asignatura) {
        viewModel.deleteSubject(asignatura.id_asignatura, token) { success, msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarTeclado() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getToken(context: Context): String? {
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    private fun obtenerColegioId(): Int {
        return requireContext()
            .getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)
    }
}
