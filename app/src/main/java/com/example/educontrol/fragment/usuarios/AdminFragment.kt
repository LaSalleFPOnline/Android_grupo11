package com.example.educontrol.fragment.usuarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.educontrol.R
import com.google.android.material.button.MaterialButton

class AdminFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Lista de botones en el GridLayout
        val buttonIds = listOf(
            R.id.btnUsuarios, R.id.btnColegios, R.id.btnUbicaciones,
            R.id.btnEventos, R.id.btnCurso , R.id.btnSemestre, R.id.btnAsignatura,
            R.id.btnAcs, R.id.btnClase // Se mantiene el botón de registrar usuario
        )

        // Configuración de cada botón con su acción
        buttonIds.forEach { buttonId ->
            view.findViewById<MaterialButton>(buttonId).setOnClickListener {
                when (buttonId) {
                    R.id.btnUsuarios -> findNavController().navigate(R.id.action_adminFragment_to_usuarioFragment)
                    R.id.btnColegios -> findNavController().navigate(R.id.action_adminFragment_to_colegioFragment)
                    R.id.btnUbicaciones -> findNavController().navigate(R.id.action_adminFragment_to_ubicacionFragment)
                    R.id.btnEventos -> findNavController().navigate(R.id.action_adminFragment_to_eventoFragment)
                    R.id.btnCurso -> findNavController().navigate(R.id.action_adminFragment_to_courseFragment)
                    R.id.btnSemestre -> findNavController().navigate(R.id.action_adminFragment_to_semestreFragment)
                    R.id.btnAsignatura -> findNavController().navigate(R.id.action_adminFragment_to_asignaturaFragment)
                    R.id.btnAcs -> findNavController().navigate(R.id.action_adminFragment_to_acsFragment)
                    R.id.btnClase -> findNavController().navigate(R.id.action_adminFragment_to_claseFragment)

                    else -> {
                        // Mensaje temporal para botones sin funcionalidad aún
                        Toast.makeText(requireContext(), "Función en desarrollo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
