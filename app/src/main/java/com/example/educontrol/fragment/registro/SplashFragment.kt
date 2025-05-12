package com.example.educontrol.fragment.registro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.educontrol.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d("educontrol", "onCreateView: Inflando el diseño de fragment_splash")
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("educontrol", "onViewCreated: Iniciando Splash Screen")

        // Usamos lifecycleScope en lugar de Handler para evitar crashes en findNavController()
        lifecycleScope.launch {
            delay(3000) // Espera 3 segundos
            if (isAdded) { // Evita crashes si el fragmento ya no está disponible
                Log.d("educontrol", "Navegando a LoginFragment")
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            } else {
                Log.d("educontrol", "Fragmento no está agregado, evitando navegación")
            }
        }
    }
}
