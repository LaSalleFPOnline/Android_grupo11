package com.example.educontrol.fragment.registro

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.educontrol.R
import com.example.educontrol.api.SignUpRequest
import com.example.educontrol.viewmodel.DataViewModel
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class RegistroUsuarioFragment : Fragment() {

    private lateinit var viewModel: DataViewModel
    private lateinit var ivPhotoPreview: ImageView
    private lateinit var spinnerRoles: Spinner
    private lateinit var spinnerTutor: Spinner

    private var filePath: File? = null
    private var nombreArchivoFoto: String? = null
    private var rolesMap: MutableMap<String, Int> = mutableMapOf()
    private var tutoresMap: MutableMap<String, Int> = mutableMapOf()
    private var guardianIdSeleccionado: Int? = null
    private var etCodigoOCR: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_registro_usuario, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DataViewModel::class.java]

        ivPhotoPreview = view.findViewById(R.id.ivPhotoPreview)
        spinnerRoles = view.findViewById(R.id.spinnerRoles)
        spinnerTutor = view.findViewById(R.id.spinnerTutor)
        val btnTakePhoto = view.findViewById<Button>(R.id.btnTakePhoto)
        val btnSignUp = view.findViewById<Button>(R.id.btnSignUp)

        Log.d("educontrol", "🔄 Iniciando RegistroUsuarioFragment")

        val token = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", "") ?: ""
        viewModel.getGuardians(token)

        // 🧠 Observador de tutores
        viewModel.guardians.observe(viewLifecycleOwner) { tutores ->
            val nombres = tutores.map { "${it.nombre} ${it.primerApellido}" }
            tutoresMap.clear()
            tutores.forEach {
                tutoresMap["${it.nombre} ${it.primerApellido}"] = it.idUsuario
            }

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, nombres)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerTutor.adapter = adapter

            spinnerTutor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val seleccionado = parent.getItemAtPosition(position).toString()
                    guardianIdSeleccionado = tutoresMap[seleccionado]
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }

        // 🧠 Mostrar u ocultar spinnerTutor según el rol
        spinnerRoles.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val rolSeleccionado = parent.getItemAtPosition(position).toString()
                spinnerTutor.visibility = if (rolSeleccionado.equals("alumno", ignoreCase = true)) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnTakePhoto.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                abrirCamara()
            }
        }

        btnSignUp.setOnClickListener {
            registrarUsuario()
        }
    }

    private fun abrirCamara() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureLauncher.launch(takePictureIntent)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.extras?.getParcelable("data", Bitmap::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.extras?.get("data") as? Bitmap
            }

            imageBitmap?.let { guardaFotoLocal(it) }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) abrirCamara()
        else Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
    }

    private fun guardaFotoLocal(bitmap: Bitmap) {
        val dir = File(requireContext().filesDir, "EduControl")
        if (!dir.exists()) dir.mkdirs()

        val timestamp = System.currentTimeMillis()
        val nombreArchivo = "foto_$timestamp.jpeg"
        val filePath = File(dir, nombreArchivo)
        nombreArchivoFoto = nombreArchivo

        try {
            FileOutputStream(filePath).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }

            // También guarda el base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

            val base64File = File(dir, "foto_$timestamp.txt")
            FileOutputStream(base64File).use { out -> out.write(base64String.toByteArray()) }
            val nuevoCodigo = generarCodigoActivacion()
            etCodigoOCR?.setText(nuevoCodigo)


        } catch (e: Exception) {
            Log.e("educontrol", "Error al guardar la imagen", e)
        }
    }

    private fun registrarUsuario() {
        val email = view?.findViewById<EditText>(R.id.etEmailSignUp)?.text.toString().trim()
        val password = view?.findViewById<EditText>(R.id.etPasswordSignUp)?.text.toString().trim()
        val name = view?.findViewById<EditText>(R.id.etNameSignUp)?.text.toString().trim()
        val middleName = view?.findViewById<EditText>(R.id.etMiddleNameSignUp)?.text.toString().trim()
        val lastName = view?.findViewById<EditText>(R.id.etLastNameSignUp)?.text.toString().trim()
        val phone = view?.findViewById<EditText>(R.id.etPhoneSignUp)?.text.toString().trim()
        val selectedRole = spinnerRoles.selectedItem.toString()
        val selectedRoleId = rolesMap[selectedRole]

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || selectedRoleId == null) {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val newUser = SignUpRequest(
            email = email,
            password = password,
            name = name,
            middleName = middleName,
            lastName = lastName,
            phone = phone,
            photo = nombreArchivoFoto ?: "default_photo.jpg",
            photoType = "image/jpeg",
            rolId = selectedRoleId,
            guardians = guardianIdSeleccionado?.let { listOf(it) } ?: emptyList()
        )

        Log.d("educontrol", "📤 Enviando datos de registro: ${Gson().toJson(newUser)}")

        viewModel.signUp(newUser) { success, message ->
            if (success) {
                Toast.makeText(requireContext(), "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generarCodigoActivacion(): String {
        return (100000..999999).random().toString()
    }
}





