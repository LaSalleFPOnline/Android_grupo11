package com.example.educontrol.fragment.acciones




import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.adapter.ColegioAdapter
import com.example.educontrol.api.Colegio
import com.example.educontrol.api.ColegioAdd
import com.example.educontrol.viewmodel.DataViewModel
class ColegioFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: ColegioAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""
    private var colegioId: Int = -1

    private var listaColegios = listOf<Colegio>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_colegio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewColegios)
        btnAgregar = view.findViewById(R.id.btnAgregarColegio)
        searchView = view.findViewById(R.id.searchViewColegios)

        adapter = ColegioAdapter(
            onUpdate = { mostrarDialogoColegio(it) },
            onDelete = { eliminarColegio(it) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return
        colegioId = obtenerColegioUsuario()

        viewModel.getSchool(token)
        viewModel.listaColegios.observe(viewLifecycleOwner) {
            listaColegios = it.filter { col -> col.id_colegio == colegioId }
            adapter.submitList(listaColegios)
        }

        btnAgregar.setOnClickListener { mostrarDialogoColegio() }
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrar(query)
                cerrarTeclado()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrar(newText)
                return true
            }
        })
    }

    private fun filtrar(query: String?) {
        val texto = query.orEmpty().lowercase()
        val filtrado = listaColegios.filter {
            it.nombre.contains(texto, true) || it.direccion.contains(texto, true)
        }
        adapter.submitList(filtrado)
    }

    private fun mostrarDialogoColegio(colegio: Colegio? = null) {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_actualizar_colegio, null)
        val etNombre = view.findViewById<EditText>(R.id.etNuevoNombre)
        val etDireccion = view.findViewById<EditText>(R.id.etNuevaDireccion)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizar)

        etNombre.setText(colegio?.nombre ?: "")
        etDireccion.setText(colegio?.direccion ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (colegio == null) "Agregar Colegio" else "Actualizar Colegio")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val direccion = etDireccion.text.toString().trim()

            if (nombre.isEmpty() || direccion.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datos = ColegioAdd(nombre, direccion)

            if (colegio == null) {
                viewModel.addSchool(datos, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getSchool(token)
                        dialog.dismiss()
                        cerrarTeclado()
                    }
                }
            } else {
                val actualizado = colegio.copy(nombre = nombre, direccion = direccion)
                viewModel.updateSchool(colegio.id_colegio, actualizado, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getSchool(token)
                        dialog.dismiss()
                        cerrarTeclado()
                    }
                }
            }
        }
    }

    private fun eliminarColegio(colegio: Colegio) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Colegio")
            .setMessage("¿Estás seguro de eliminar este colegio?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.deleteSchool(colegio.id_colegio, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) viewModel.getSchool(token)
                }
            }
            .setNegativeButton("No", null)
            .show()
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
        return context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }
}


