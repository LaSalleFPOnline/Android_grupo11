package com.example.educontrol.fragment.acciones

import android.app.AlertDialog
import android.app.DatePickerDialog
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
import com.example.educontrol.adapter.SemestreAdapter
import com.example.educontrol.api.Semestre
import com.example.educontrol.api.SemestreAdd
import com.example.educontrol.viewmodel.DataViewModel
import java.util.*

class SemestreFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var adapter: SemestreAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var searchView: SearchView
    private var token: String = ""

    private var listaSemestres = listOf<Semestre>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_semestre, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewSemesters)
        btnAgregar = view.findViewById(R.id.btnAgregarSemestre)
        searchView = view.findViewById(R.id.searchViewSemesters)

        adapter = SemestreAdapter(
            onUpdate = { mostrarDialogoSemestre(it) },
            onDelete = { eliminarSemestre(it) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        token = getToken(requireContext()) ?: return

        viewModel.getSemestres(token)
        viewModel.listaSemestres.observe(viewLifecycleOwner) {
            listaSemestres = it
            adapter.submitList(it)
        }

        btnAgregar.setOnClickListener { mostrarDialogoSemestre() }
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
        val resultado = listaSemestres.filter {
            it.numero_semestre.toString().contains(texto) ||
                    it.fecha_inicio.contains(texto, true) ||
                    it.fecha_fin.contains(texto, true)
        }
        adapter.submitList(resultado)
    }

    private fun mostrarDialogoSemestre(semestre: Semestre? = null) {
        val view = layoutInflater.inflate(R.layout.dialog_actualizar_semestre, null)
        val etNumero = view.findViewById<EditText>(R.id.etNuevoNumeroSemestre)
        val etInicio = view.findViewById<EditText>(R.id.etNuevaFechaInicioSemestre)
        val etFin = view.findViewById<EditText>(R.id.etNuevaFechaFinSemestre)
        val btnGuardar = view.findViewById<Button>(R.id.btnActualizarSemestre)
        val tvTitulo = view.findViewById<TextView>(R.id.tvTituloDialogo)

        etInicio.setOnClickListener { showDatePicker(etInicio) }
        etFin.setOnClickListener { showDatePicker(etFin) }

        tvTitulo.text = if (semestre == null) "Nuevo Semestre" else "Editar Semestre"
        btnGuardar.text = if (semestre == null) "Guardar" else "Actualizar"

        semestre?.let {
            etNumero.setText(it.numero_semestre.toString())
            etInicio.setText(it.fecha_inicio)
            etFin.setText(it.fecha_fin)
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(view).create()
        dialog.show()

        btnGuardar.setOnClickListener {
            val num = etNumero.text.toString().trim()
            val inicio = etInicio.text.toString().trim()
            val fin = etFin.text.toString().trim()

            if (num.isEmpty() || inicio.isEmpty() || fin.isEmpty()) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val datos = SemestreAdd(num.toInt(), inicio, fin)

            if (semestre == null) {
                viewModel.addSemestre(datos, token) { success, msg ->
                    toast(msg)
                    if (success) dialog.dismiss()
                }
            } else {
                viewModel.updateSemestre(semestre.id_semestre, datos, token) { success, msg ->
                    toast(msg)
                    if (success) dialog.dismiss()
                }
            }
        }
    }

    private fun eliminarSemestre(semestre: Semestre) {
        viewModel.deleteSemestre(semestre.id_semestre, token) { success, msg ->
            toast(msg)
        }
    }

    private fun showDatePicker(et: EditText) {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            et.setText(String.format(Locale("es", "ES"), "%04d-%02d-%02dT00:00:00.000Z", y, m + 1, d))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
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


