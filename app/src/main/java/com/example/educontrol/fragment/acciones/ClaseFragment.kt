package com.example.educontrol.fragment.acciones


import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educontrol.R
import com.example.educontrol.adapter.ClaseAdapter
import com.example.educontrol.api.Clase
import com.example.educontrol.api.ClaseAdd
import com.example.educontrol.viewmodel.DataViewModel
import java.util.*


class ClaseFragment : Fragment() {

    private val viewModel: DataViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAgregar: Button
    private lateinit var adapter: ClaseAdapter
    private lateinit var token: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_clase, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerViewClases)
        btnAgregar = view.findViewById(R.id.btnAgregarClase)

        token = requireContext()
            .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", "") ?: ""

        adapter = ClaseAdapter(
            ubicaciones = emptyList(), // se actualiza más abajo
            onEdit = { clase -> mostrarDialogClase(clase) },
            onDelete = { clase ->
                viewModel.deleteClase(clase.id_clase, token) { _, msg -> toast(msg) }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        btnAgregar.setOnClickListener { mostrarDialogClase() }

        viewModel.listaClases.observe(viewLifecycleOwner) { clases ->
            adapter.submitList(clases)
        }

        viewModel.listaUbicaciones.observe(viewLifecycleOwner) { ubicaciones ->
            adapter.updateUbicaciones(ubicaciones)
        }

        viewModel.getClases(token)
        viewModel.getLocations(token)
        viewModel.getAcs(token)
    }

    private fun toast(msg: String?) {
        Toast.makeText(requireContext(), msg ?: "Ocurrió un error", Toast.LENGTH_SHORT).show()
    }

    private fun mostrarTimePicker(target: EditText) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                target.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun mostrarDialogClase(clase: Clase? = null) {
        val ubicaciones = viewModel.listaUbicaciones.value
        val acs = viewModel.listaAcs.value

        Log.d("educontrol", "📦 Total ubicaciones: ${ubicaciones?.size ?: 0}")
        Log.d("educontrol", "📦 Total ACS: ${acs?.size ?: 0}")

        if (ubicaciones == null || acs == null) {
            Log.w("educontrol", "⚠️ Ubicaciones o ACS no disponibles aún.")
            Toast.makeText(
                requireContext(),
                "⏳ Cargando datos, espera un momento...",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_clase_form, null)
        builder.setView(view)

        val spinnerUbicacion = view.findViewById<Spinner>(R.id.spinnerLocation)
        val spinnerAcs = view.findViewById<Spinner>(R.id.spinnerACS)
        val etStartTime = view.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = view.findViewById<EditText>(R.id.etEndTime)
        val btnGuardar = view.findViewById<Button>(R.id.btnGuardarClase)

        val checkBoxes = listOf(
            view.findViewById(R.id.cbMonday),
            view.findViewById(R.id.cbTuesday),
            view.findViewById(R.id.cbWednesday),
            view.findViewById(R.id.cbThursday),
            view.findViewById(R.id.cbFriday),
            view.findViewById(R.id.cbSaturday),
            view.findViewById<CheckBox>(R.id.cbSunday)
        )
        val dayIndexMap = listOf(1, 2, 3, 4, 5, 6, 7)

        val colegioId = requireContext()
            .getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
            .getInt("USER_COLEGIO_ID", -1)

        val ubicacionesFiltradas = ubicaciones.filter { it.id_colegio == colegioId }
        val acsFiltradas = acs

        // Placeholders al inicio de la lista
        val nombresUbicaciones =
            listOf("Selecciona una ubicación...") + ubicacionesFiltradas.map { it.nombre }
        val nombresAcs = listOf("Selecciona una asignación...") + acsFiltradas.map {
            "${it.nombre_asignatura} - ${it.nombre_curso} - ${it.fecha_semestre} - ${it.nombre_profesor}"
        }

        spinnerUbicacion.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            nombresUbicaciones
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        spinnerAcs.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombresAcs).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        clase?.let {
            etStartTime.setText(it.hora_inicio)
            etEndTime.setText(it.hora_fin)

            spinnerUbicacion.setSelection(ubicacionesFiltradas.indexOfFirst { ub -> ub.id_ubicacion == it.id_ubicacion } + 1)
            spinnerAcs.setSelection(acsFiltradas.indexOfFirst { a -> a.id == it.id_acs } + 1)

            it.dia_semana.forEach { dia ->
                dayIndexMap.indexOf(dia).takeIf { i -> i >= 0 }
                    ?.let { idx -> checkBoxes[idx].isChecked = true }
            }
        }

        etStartTime.setOnClickListener { mostrarTimePicker(etStartTime) }
        etEndTime.setOnClickListener { mostrarTimePicker(etEndTime) }

        val dialog = builder.create()

        btnGuardar.setOnClickListener {
            val startTime = etStartTime.text.toString()
            val endTime = etEndTime.text.toString()

            val ubicacionSeleccionada =
                ubicacionesFiltradas.getOrNull(spinnerUbicacion.selectedItemPosition - 1)
            val acsSeleccionada = acsFiltradas.getOrNull(spinnerAcs.selectedItemPosition - 1)
            val diasSeleccionados =
                checkBoxes.mapIndexedNotNull { i, cb -> if (cb.isChecked) dayIndexMap[i] else null }

            Log.d("educontrol", "📤 Guardando clase con:")
            Log.d("educontrol", "📌 Ubicación: ${ubicacionSeleccionada?.nombre}")
            Log.d("educontrol", "📌 ACS: ${acsSeleccionada?.nombre_asignatura}")
            Log.d("educontrol", "📅 Días seleccionados: $diasSeleccionados")
            Log.d("educontrol", "⏰ Horario: $startTime - $endTime")

            if (ubicacionSeleccionada == null || acsSeleccionada == null || diasSeleccionados.isEmpty()
                || startTime.isBlank() || endTime.isBlank()
            ) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val claseAdd = ClaseAdd(
                id_acs = acsSeleccionada.id,
                locationId = ubicacionSeleccionada.id_ubicacion,
                week_days = diasSeleccionados,
                startTime = startTime,
                endTime = endTime
            )

            if (clase == null) {
                viewModel.addClase(claseAdd, token) { success, msg -> toast(msg) }
            } else {
                viewModel.updateClase(
                    clase.id_clase,
                    claseAdd,
                    token
                ) { success, msg -> toast(msg) }
            }

            dialog.dismiss()
        }

        dialog.show()
    }
}