package com.example.educontrol.fragment.registro


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.educontrol.MainActivity
import com.example.educontrol.R
import com.example.educontrol.viewmodel.DataViewModel


class LoginFragment : Fragment() {

    private lateinit var dataViewModel: DataViewModel
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var recoverPasswordTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataViewModel = ViewModelProvider(this)[DataViewModel::class.java]

        emailEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        recoverPasswordTextView = view.findViewById(R.id.recoverPassword)
        progressBar = view.findViewById(R.id.progressBar)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "❌ Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            dataViewModel.iniciarSesion(requireContext(), email, password)
        }

        recoverPasswordTextView.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_olvidoContrasenaFragment)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        dataViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            progressBar.visibility = View.GONE
            result.onSuccess { idRol ->
                Toast.makeText(requireContext(), "✅ Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                // 🔁 Refrescar los datos del usuario en MainActivity
                (requireActivity() as? MainActivity)?.refrescarDatosUsuario()

                navigateToDashboard(idRol)
            }.onFailure { error ->
                Toast.makeText(requireContext(), "❌ Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToDashboard(idRol: Int) {
        val navMap = mapOf(
            1 to R.id.action_loginFragment_to_profesorFragment,
            2 to R.id.action_loginFragment_to_alumnoFragment,
            3 to R.id.action_loginFragment_to_tutorFragment,
            4 to R.id.action_loginFragment_to_adminFragment
        )

        navMap[idRol]?.let { destino ->
            findNavController().navigate(destino)
        } ?: run {
            Toast.makeText(requireContext(), "⚠️ Usuario no autorizado", Toast.LENGTH_SHORT).show()
        }
    }


}











