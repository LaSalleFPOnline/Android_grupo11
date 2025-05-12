package com.example.educontrol.fragment.acciones


import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.example.educontrol.adapter.EventoAdapter
import com.example.educontrol.api.Evento
import com.example.educontrol.api.EventoAdd
import com.example.educontrol.api.Ubicacion
import com.example.educontrol.viewmodel.DataViewModel
import java.util.*

class EventoFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventoAdapter
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""
    private var colegioId: Int = -1

    private var listaUbicaciones: List<Ubicacion> = emptyList()
    private var listaEventos: List<Evento> = emptyList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_evento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewEventos)
        btnAgregar = view.findViewById(R.id.btnAgregarEvento)
        searchView = view.findViewById(R.id.searchViewEventos)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = EventoAdapter(
            onUpdate = { mostrarDialogoEvento(it) },
            onDelete = { eliminarEvento(it) }
        )
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return
        colegioId = requireContext().getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)

        // Obtener y filtrar ubicaciones del colegio actual
        viewModel.getLocations(token)
        viewModel.listaUbicaciones.observe(viewLifecycleOwner) {
            listaUbicaciones = it.filter { ub -> ub.id_colegio == colegioId }
        }

        // Obtener eventos (sin filtrar porque ya están asociados a ubicaciones)
        viewModel.getEvent(token)
        viewModel.listaEventos.observe(viewLifecycleOwner) {
            listaEventos = it
            adapter.submitList(it)
        }

        btnAgregar.setOnClickListener { mostrarDialogoEvento() }
        configurarBuscador()
    }

    private fun configurarBuscador() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also { filtrar(query) }
            override fun onQueryTextChange(newText: String?) = true.also { filtrar(newText) }
        })
    }

    private fun filtrar(query: String?) {
        val texto = query.orEmpty().lowercase()
        val filtrado = listaEventos.filter {
            it.concepto.contains(texto, ignoreCase = true)
        }
        adapter.submitList(filtrado)
    }

    private fun mostrarDialogoEvento(evento: Evento? = null) {
        val view = layoutInflater.inflate(R.layout.dialog_actualizar_evento, null)

        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)
        val etConcepto = view.findViewById<EditText>(R.id.etTituloEvento)
        val etFecha = view.findViewById<EditText>(R.id.etDescripcionEvento)
        val spinnerUbicaciones = view.findViewById<Spinner>(R.id.spinnerUbicaciones)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarEvento)

        tvTitulo.text = if (evento == null) "Nuevo Evento" else "Editar Evento"
        btnGuardar.text = if (evento == null) "Guardar" else "Actualizar"

        val nombresUbicaciones = listOf("Selecciona una ubicación") + listaUbicaciones.map { it.nombre }
        spinnerUbicaciones.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresUbicaciones)
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        etFecha.setOnClickListener { showDatePicker(etFecha) }

        evento?.let {
            etConcepto.setText(it.concepto)
            etFecha.setText(it.fecha)
            val indexUbicacion = listaUbicaciones.indexOfFirst { ub -> ub.id_ubicacion == it.id_ubicacion } + 1
            if (indexUbicacion > 0) spinnerUbicaciones.setSelection(indexUbicacion)
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.show()

        btnGuardar.setOnClickListener {
            val concepto = etConcepto.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val indexUb = spinnerUbicaciones.selectedItemPosition
            val ubicacionSeleccionada = if (indexUb > 0) listaUbicaciones.getOrNull(indexUb - 1) else null

            if (concepto.isEmpty() || fecha.isEmpty() || ubicacionSeleccionada == null) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val eventoAdd = EventoAdd(
                id_ubicacion = ubicacionSeleccionada.id_ubicacion,
                fecha = fecha,
                concepto = concepto
            )

            if (evento == null) {
                viewModel.addEvent(eventoAdd, token) { success, msg ->
                    toast(msg)
                    if (success) dialog.dismiss()
                }
            } else {
                viewModel.updateEvent(evento.id_evento, eventoAdd, token) { success, msg ->
                    toast(msg)
                    if (success) dialog.dismiss()
                }
            }

            cerrarTeclado()
        }
    }

    private fun eliminarEvento(evento: Evento) {
        viewModel.deleteEvent(evento.id_evento, token) { success, msg ->
            toast(msg)
        }
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale("es", "ES"), "%04d-%02d-%02dT00:00:00.000Z", year, month + 1, dayOfMonth)
                editText.setText(formatted)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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
}


