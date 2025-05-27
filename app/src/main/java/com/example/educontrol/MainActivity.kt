package com.example.educontrol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.example.educontrol.databinding.ActivityMainBinding
import com.example.educontrol.fragment.usuarios.AlumnoFragment
import com.google.android.material.appbar.MaterialToolbar

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var tvUserName: TextView


    // Almacenamiento de datos del usuario
    private val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("USER_SESSION", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Navigation Controller
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Obtener referencias a la barra superior y al TextView del nombre de usuario
        topAppBar = findViewById(R.id.topAppBar)
        tvUserName = findViewById(R.id.tvUserName)

        // Configurar Toolbar como barra de acción
        setSupportActionBar(topAppBar)

        // 🔹 Cargar el nombre del usuario autenticado
        actualizarNombreUsuario()

        // 🔹 Escuchar cambios en la navegación para ocultar la barra en ciertas pantallas
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.splashFragment, R.id.olvidoContrasenaFragment -> {
                    topAppBar.visibility = View.GONE
                }
                else -> {
                    topAppBar.visibility = View.VISIBLE
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("educontrol", "📲 onNewIntent recibido: $intent")

        // ⚠️ NO extraemos el tag aquí. Dejamos que lo haga el fragmento.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentFragment = navHostFragment
            ?.childFragmentManager
            ?.fragments
            ?.firstOrNull { it.isVisible }

        if (currentFragment is AlumnoFragment) {
            Log.d("educontrol", "➡️ Reenviando intent al AlumnoFragment")
            currentFragment.handleNfcIntent(intent)
        } else {
            Log.w("educontrol", "⚠️ currentFragment no es AlumnoFragment o está oculto")
        }
    }




    // 🔹 Inflar el menú en la barra superior
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_usuario, menu)
        return true
    }

    // 🔹 Manejar clics en los ítems del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_cambiar_contrasena -> {
                navController.navigate(R.id.olvidoContrasenaFragment)
                true
            }
            R.id.menu_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 🔹 Función para actualizar el nombre del usuario
    private fun actualizarNombreUsuario() {
        val sharedPreferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE)

        val nombreUsuario = sharedPreferences.getString("USER_NAME", "Usuario") ?: "Usuario"
        val rol = sharedPreferences.getString("USER_ROL", "Usuario") ?: "Usuario"
        val base64Foto = sharedPreferences.getString("USER_PHOTO", null)

        val saludo = when (rol.lowercase()) {
            "profesor" -> "👨‍🏫"
            "alumno" -> "👨‍🎓"
            "administrativo" -> "🛠️"
            else -> "Hola"
        }

        tvUserName.text = "$saludo, $nombreUsuario"

        val imageView = findViewById<ImageView>(R.id.imgUserPhoto)

        if (!base64Foto.isNullOrEmpty()) {
            try {
                val base64 = base64Foto.substringAfter(",") // Elimina "data:image/png;base64,"
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)

                Glide.with(this)
                    .asBitmap()
                    .load(imageBytes)
                    .circleCrop()
                    .into(imageView)

                Log.d("educontrol", "✅ Imagen del usuario cargada desde base64")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al cargar imagen base64: ${e.message}")
                imageView.setImageResource(R.drawable.ic_account_profile)
            }
        } else {
            Log.w("educontrol", "⚠️ No hay imagen del usuario, se usará imagen por defecto")
            imageView.setImageResource(R.drawable.ic_account_profile)
        }
    }


    // 🔹 Función para cerrar sesión
    private fun cerrarSesion() {
        Toast.makeText(this, "🔓 Cerrando sesión...", Toast.LENGTH_SHORT).show()

        // Borrar datos de sesión
        sharedPreferences.edit().clear().apply()

        // Redirigir al login
        navController.navigate(R.id.loginFragment)
    }

    fun refrescarDatosUsuario() {
        actualizarNombreUsuario()
    }
}


