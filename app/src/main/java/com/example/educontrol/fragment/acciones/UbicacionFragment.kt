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
import com.example.educontrol.adapter.UbicacionAdapter
import com.example.educontrol.api.Colegio
import com.example.educontrol.api.Ubicacion
import com.example.educontrol.api.UbicacionAdd
import com.example.educontrol.viewmodel.DataViewModel

class UbicacionFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UbicacionAdapter
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""
    private var colegioId: Int = -1

    private var listaUbicaciones: List<Ubicacion> = listOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_ubicacion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewUbicaciones)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        btnAgregar = view.findViewById(R.id.btnAgregarUbicacion)
        searchView = view.findViewById(R.id.searchViewUbicaciones)

        adapter = UbicacionAdapter(
            onUpdate = { mostrarDialogoUbicacion(it) },
            onDelete = { eliminarUbicacion(it) }
        )
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return

        // ✅ Obtener ID del colegio desde SharedPreferences
        colegioId = requireContext()
            .getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)

        viewModel.getLocations(token)
        viewModel.listaUbicaciones.observe(viewLifecycleOwner) { ubicaciones ->
            listaUbicaciones = ubicaciones.filter { it.id_colegio == colegioId }
            adapter.submitList(listaUbicaciones)
        }

        btnAgregar.setOnClickListener { mostrarDialogoUbicacion() }
        configurarBuscador()
    }

    private fun mostrarDialogoUbicacion(ubicacion: Ubicacion? = null) {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_actualizar_ubicacion, null)
        builder.setView(view)

        val etNombre = view.findViewById<EditText>(R.id.etNuevoNombreUbicacion)
        val etTipo = view.findViewById<EditText>(R.id.etNuevoTipoUbicacion)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarUbicacion)

        ubicacion?.let {
            etNombre.setText(it.nombre)
            etTipo.setText(it.tipo)
        }

        val dialog = builder.create()
        dialog.show()

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val tipo = etTipo.text.toString().trim()

            if (nombre.isEmpty() || tipo.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = UbicacionAdd(nombre, tipo, colegioId)

            if (ubicacion == null) {
                viewModel.addLocation(requireContext(), data, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getLocations(token)
                        dialog.dismiss()
                        cerrarTeclado()
                    }
                }
            } else {
                viewModel.updateLocations(ubicacion.id_ubicacion, data, token) { success, msg ->
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.getLocations(token)
                        dialog.dismiss()
                        cerrarTeclado()
                    }
                }
            }
        }
    }

    private fun eliminarUbicacion(ubicacion: Ubicacion) {
        viewModel.deleteLocation(ubicacion.id_ubicacion, token) { success, msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            if (success) viewModel.getLocations(token)
        }
    }

    private fun configurarBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { filtrar(query) }
            override fun onQueryTextChange(newText: String?) = true.also { filtrar(newText) }
        })
    }

    private fun filtrar(query: String?) {
        val texto = query.orEmpty().lowercase()
        val filtrado = listaUbicaciones.filter {
            it.nombre?.contains(texto, true) == true ||
                    it.tipo?.contains(texto, true) == true ||
                    it.nombre_colegio?.contains(texto, true) == true
        }
        adapter.submitList(filtrado)
    }

    private fun cerrarTeclado() {
        val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("auth_token", null)
    }
}

