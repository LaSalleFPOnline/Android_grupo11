package com.example.educontrol.fragment.registro


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.educontrol.R
import com.example.educontrol.viewmodel.DataViewModel


class OlvidoContrasenaFragment : Fragment() {

    private lateinit var authViewModel: DataViewModel
    private lateinit var emailEditText: EditText
    private lateinit var enviarCorreoButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("educontrol", "🟢 onCreateView: Inflando fragment_olvido_contrasena")
        return inflater.inflate(R.layout.fragment_olvido_contrasena, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("educontrol", "🟢 onViewCreated: Inicializando vistas")

        authViewModel = ViewModelProvider(this).get(DataViewModel::class.java)

        emailEditText = view.findViewById(R.id.correoEditText)
        enviarCorreoButton = view.findViewById(R.id.enviarcorreoButton)

        enviarCorreoButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Log.e("educontrol", "❌ Error: El campo de correo está vacío")
                Toast.makeText(requireContext(), "❌ El correo es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("educontrol", "📤 Enviando solicitud para restablecer contraseña para el email: $email")

            authViewModel.forgotPassword(email) { success, message ->
                requireActivity().runOnUiThread {
                    if (success) {
                        Log.d("educontrol", "✅ Éxito: $message")
                        Toast.makeText(requireContext(), "✅ $message", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.loginFragment)
                    } else {
                        Log.e("educontrol", "❌ Error en la API: $message")
                        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
