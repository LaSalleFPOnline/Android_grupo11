package com.example.educontrol.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educontrol.api.ACS
import com.example.educontrol.api.ACSAdd
import com.example.educontrol.api.Advertencia
import com.example.educontrol.api.AdvertenciaAlumno
import com.example.educontrol.api.ApiService
import com.example.educontrol.api.Asignatura
import com.example.educontrol.api.AsignaturaAdd
import com.example.educontrol.api.Clase
import com.example.educontrol.api.ClaseAdd
import com.example.educontrol.api.Colegio
import com.example.educontrol.api.ColegioAdd
import com.example.educontrol.api.Curso
import com.example.educontrol.api.CursoAdd
import com.example.educontrol.api.Estado
import com.example.educontrol.api.Evento
import com.example.educontrol.api.EventoAdd
import com.example.educontrol.api.ForgotPasswordRequest
import com.example.educontrol.api.HorarioResponse
import com.example.educontrol.api.LoginRequest
import com.example.educontrol.api.Multimedia
import com.example.educontrol.api.NotaAlumno
import com.example.educontrol.api.NoteRequest
import com.example.educontrol.api.NoteResponse
import com.example.educontrol.api.NotificacionRequest
import com.example.educontrol.api.NotificacionResponse
import com.example.educontrol.api.Profesor
import com.example.educontrol.api.ResetPasswordRequest
import com.example.educontrol.api.RetrofitInstance
import com.example.educontrol.api.RetrofitInstance.api
import com.example.educontrol.api.RolResponse
import com.example.educontrol.api.Semestre
import com.example.educontrol.api.SemestreAdd
import com.example.educontrol.api.SignUpRequest
import com.example.educontrol.api.TestRequest
import com.example.educontrol.api.Ubicacion
import com.example.educontrol.api.UbicacionAdd
import com.example.educontrol.api.Test

import com.example.educontrol.api.Usuario
import com.example.educontrol.api.UsuarioAdd
import com.example.educontrol.api.UsuarioUpdate
import com.example.educontrol.api.WarningRequest
import com.example.educontrol.api.WarningResponse
import com.example.educontrol.database.AppDatabase
import com.example.educontrol.database.ColegioEntity
import com.example.educontrol.database.UsuarioEntity
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import jcifs.smb.SmbFileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import jcifs.CIFSContext
import java.util.Properties

class DataViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Result<Int>>()
    val loginResult: LiveData<Result<Int>> get() = _loginResult

    private val _listaColegios = MutableLiveData<List<Colegio>>()
    val listaColegios: LiveData<List<Colegio>> get() = _listaColegios

    private val _listaUbicaciones = MutableLiveData<List<Ubicacion>>()
    val listaUbicaciones: LiveData<List<Ubicacion>> get() = _listaUbicaciones

    private val _listaEventos = MutableLiveData<List<Evento>>()
    val listaEventos: LiveData<List<Evento>> get() = _listaEventos


    private val _listaCursos = MutableLiveData<List<Curso>>()
    val listaCursos: LiveData<List<Curso>> get() = _listaCursos

    private val _listaSemestres = MutableLiveData<List<Semestre>>()
    val listaSemestres: LiveData<List<Semestre>> get() = _listaSemestres

    private val _listaAsignaturas = MutableLiveData<List<Asignatura>>()
    val listaAsignaturas: LiveData<List<Asignatura>> get() = _listaAsignaturas

    val listaAcs = MutableLiveData<List<ACS>>()

    private val _listaUsuarios = MutableLiveData<List<Usuario>>()
    val listaUsuarios: LiveData<List<Usuario>> get() = _listaUsuarios

    private val _listaRoles = MutableLiveData<List<RolResponse>>()
    val listaRoles: LiveData<List<RolResponse>> get() = _listaRoles

    val listaProfesores = MutableLiveData<List<Profesor>>()

    val listaEstados = MutableLiveData<List<Estado>>()

    private val _listaClases = MutableLiveData<List<Clase>>()
    val listaClases: LiveData<List<Clase>> = _listaClases

    val usuarioAccionResultado = MutableLiveData<String?>()

    private val _insertWarningStatus = MutableLiveData<String>()
    val insertWarningStatus: LiveData<String> get() = _insertWarningStatus

    private val _insertTestStatus = MutableLiveData<String>()
    val insertTestStatus: LiveData<String> get() = _insertTestStatus

//    private val _insertNoteStatus = MutableLiveData<String>()
//    val insertNoteStatus: LiveData<String> get() = _insertNoteStatus

    private val _listaTests = MutableLiveData<List<Test>>()
    val listaTests: LiveData<List<Test>> get() = _listaTests

    private val _listaAdvertencias = MutableLiveData<List<WarningResponse>>()
    val listaAdvertencias: LiveData<List<WarningResponse>> = _listaAdvertencias

    private val _horarioAlumno = MutableLiveData<HorarioResponse>()
    val horarioAlumno: LiveData<HorarioResponse> = _horarioAlumno

    val listaNotas = MutableLiveData<List<NoteResponse>>()
    val insertNoteStatus = MutableLiveData<String>()

    val enviarNotificacionStatus = MutableLiveData<String>()

    val notificacionesNoLeidas = MutableLiveData<List<NotificacionResponse>>()

    val listaAdvertenciasTutor = MutableLiveData<List<AdvertenciaAlumno>>()
    val listaNotasTutor = MutableLiveData<List<NotaAlumno>>()

    val guardians = MutableLiveData<List<Usuario>>()

    val listaTodasNotificaciones = MutableLiveData<List<NotificacionResponse>>()


    /**
     * 🔹 Función para iniciar sesión y guardar en Room
     */
    fun iniciarSesion(context: Context, email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("educontrol", "📨 Enviando solicitud de login con: $email")
                val response = api.login(LoginRequest(email, password))

                if (response.isSuccessful) {
                    response.body()?.let { loginResponse ->
                        val user = loginResponse.user
                        val token = loginResponse.token

                        // ✅ Log del token recibido
                        Log.d("educontrol", "✅ Token recibido: $token")

                        // ✅ Log de usuario completo (Serializado)
                        Log.d("educontrol", "🧍 Usuario recibido: $user")

                        // ✅ Logs individuales del usuario
                        Log.d("educontrol", "🔹 ID: ${user.idUsuario}")
                        Log.d("educontrol", "🔹 Nombre: ${user.nombre} ${user.primerApellido} ${user.segundoApellido}")
                        Log.d("educontrol", "🔹 Email: ${user.email}")
                        Log.d("educontrol", "🔹 Rol ID: ${user.idRol}")
                        Log.d("educontrol", "🔹 Rol Nombre: ${user.idRol}")
                        Log.d("educontrol", "🔹 Colegio ID: ${user.idColegio}")
                        Log.d("educontrol", "🔹 Estado: ${user.estadoUsuario}")
                        Log.d("educontrol", "🔹 Código de activación: ${user.codigoActivacion}")
                        Log.d("educontrol", "🔹 Fecha de registro: ${user.fechaRegistro}")
                        Log.d("educontrol", "🔹 Fecha de actualización: ${user.fechaActualizacion}")
                        Log.d("educontrol", "🔹 App Mode: ${user.appMode}")
                        Log.d("educontrol", "🔹 Contraseña: ${user.contrasena}")
                        Log.d("educontrol", "🔹 Enlace: ${user.photo}")


                        Log.d("educontrol", "🔹 Foto (Base64): ${user.photo?.take(30)}...")

                        // ✅ Guardar el token en SharedPreferences
                        saveToken(context, token)
                        Log.d("educontrol", "✅ Token guardado correctamente")

                        // ✅ Guardar datos del usuario en SharedPreferences
                        val sharedPrefs = context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                        with(sharedPrefs.edit()) {
                            putString("USER_NAME", "${user.nombre} ${user.primerApellido}")
                            putString("USER_ROL", user.rol)
                            putString("USER_PHOTO", user.photo) // En base64 con prefijo "data:image/png;base64,"
                            putInt("USER_COLEGIO_ID", user.idColegio ?: -1) // 👈 Asegúrate de guardar el id_colegio
                            putInt("USER_ID", user.idUsuario)
                            apply()
                        }

                        // Aquí reactivar la parte de Room

                        withContext(Dispatchers.Main) {
                            _loginResult.value = Result.success(user.idRol)
                        }

                    } ?: run {
                        withContext(Dispatchers.Main) {
                            Log.e("educontrol", "❌ Error: Respuesta sin cuerpo (user nulo)")
                            _loginResult.value = Result.failure(Exception("❌ Datos de usuario no válidos"))
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Sin detalles"
                    withContext(Dispatchers.Main) {
                        Log.e("educontrol", "❌ Error en login (response error): $errorBody")
                        _loginResult.value = Result.failure(Exception(errorBody))
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Log.e("educontrol", "❌ Error de conexión: ${e.message}")
                    _loginResult.value = Result.failure(Exception("Error de conexión: ${e.message}"))
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Log.e("educontrol", "❌ Error HTTP: ${e.message()}")
                    _loginResult.value = Result.failure(Exception("Error HTTP: ${e.message()}"))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("educontrol", "❌ Error desconocido: ${e.message}")
                    _loginResult.value = Result.failure(Exception("Error desconocido: ${e.message}"))
                }
            }
        }
    }



    fun saveToken(context: Context, token: String) {
        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("auth_token", token)
            apply()
        }
    }


    /**
     * 🔹 Función para registrar un usuario con logs detallados.
     */
    fun signUp(user: SignUpRequest, onResult: (Boolean, String?) -> Unit) {
        Log.d("educontrol", "📩 Iniciando registro con usuario: ${user.email}")

        viewModelScope.launch {
            try {
                val response = api.signUp(user)

                Log.d("educontrol", "🔹 Código de respuesta: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()

                    if (responseBody == null) {
                        Log.d("educontrol", "⚠️ Respuesta no es JSON, pero registro exitoso")
                        onResult(true, "Registro exitoso")
                    } else {
                        Log.d("educontrol", "✅ Registro exitoso")
                        onResult(true, "Registro exitoso")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("educontrol", "❌ Error en signUp: ${response.code()}, Detalle: $errorBody")
                    onResult(false, "Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("educontrol", "🚨 Excepción en signUp: ${e.message}")
                onResult(false, "Error de conexión: ${e.message}")
            }
        }
    }

    /**
     * 🔹 Función para enviar el correo de recuperación de contraseña
     */
    fun forgotPassword(email: String, onResult: (Boolean, String) -> Unit) {
        Log.d("educontrol", "📩 Enviando solicitud para recuperar contraseña con email: $email")

        viewModelScope.launch {
            try {
                val requestBody = ForgotPasswordRequest(email)
                val response = api.forgotPassword(requestBody)

                if (response.isSuccessful) {
                    Log.d("educontrol", "✅ Correo enviado correctamente")
                    onResult(true, "Correo enviado correctamente")
                } else {
                    // Captura el cuerpo del error si el servidor lo envió
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("educontrol", "❌ Error al enviar correo: ${response.code()}, Detalle: $errorBody")
                    onResult(false, "Error: ${response.code()} - $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("educontrol", "❌ HttpException en forgotPassword: ${e.message()}")
                onResult(false, "Error HTTP: ${e.message()}")
            } catch (e: IOException) {
                Log.e("educontrol", "❌ IOException en forgotPassword: ${e.message}")
                onResult(false, "Error de conexión: ${e.message}")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción en forgotPassword: ${e.message}")
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    /**
     * 🔹 Función para restablecer la contraseña con el token de autenticación
     */
    fun resetPassword(token: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        Log.d("educontrol", "🔄 Restableciendo contraseña con token: $token")

        viewModelScope.launch {
            try {
                val response = api.resetPassword("Bearer $token", ResetPasswordRequest(newPassword))

                if (response.isSuccessful) {
                    Log.d("educontrol", "✅ Contraseña restablecida correctamente")
                    onResult(true, "Contraseña restablecida correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("educontrol", "❌ Error al restablecer contraseña: ${response.code()}, Detalle: $errorBody")
                    onResult(false, "Error: ${response.code()} - $errorBody")
                }
            } catch (e: HttpException) {
                Log.e("educontrol", "❌ HttpException en resetPassword: ${e.message()}")
                onResult(false, "Error HTTP: ${e.message()}")
            } catch (e: IOException) {
                Log.e("educontrol", "❌ IOException en resetPassword: ${e.message}")
                onResult(false, "Error de conexión: ${e.message}")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción en resetPassword: ${e.message}")
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    // 🔹 Obtener roles desde la API
    // 🔹 Obtener roles desde la API con token
    fun getRoles(token: String) {
        viewModelScope.launch {
            try {
                val authHeader = "Bearer $token"
                val response = api.getRoles(authHeader)

                if (response.isSuccessful) {
                    _listaRoles.postValue(response.body() ?: emptyList())
                } else {
                    Log.e("EduControl", "❌ Error al obtener roles: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "❌ Excepción al obtener roles: ${e.message}")
            }
        }
    }
    /**
     * 🔹 Obtener la lista de colegios desde la API
     */
    fun getSchool(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getColegios("Bearer $token")
                if (response.isSuccessful) {
                    _listaColegios.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener colegios: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción al obtener colegios: ${e.message}")
            }
        }
    }

    /**
     * 🔹 Agregar un nuevo colegio
     */
    fun addSchool(colegio: ColegioAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // ✅ Agregar el prefijo "Bearer" aquí
                val response = api.addColegio(colegio, "Bearer $token")
                if (response.isSuccessful) {
                    onResult(true, "Colegio insertado correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /**
     * 🔹 Actualizar colegio existente
     */
    fun updateSchool(id: Int, colegio: Colegio, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateColegio(id, colegio, "Bearer $token")
                if (response.isSuccessful) {
                    getSchool(token) // Actualiza la lista después de la modificación
                    onResult(true, "Colegio actualizado correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    /**
     * 🔹 Eliminar un colegio
     */
    fun deleteSchool(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteColegio(id, "Bearer $token")
                if (response.isSuccessful) {
                    getSchool(token)
                    onResult(true, "Colegio eliminado correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    /**
     * 🔹 Función para obtener las ubicaciones desde la API
     */
    fun getLocations(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getUbicaciones("Bearer $token")
                if (response.isSuccessful) {
                    _listaUbicaciones.postValue(response.body())
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener ubicaciones: $error")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción al obtener ubicaciones: ${e.message}")
            }
        }
    }

    /**
     * 🔹 Función para agregar una nueva ubicación
     */
    fun addLocation(context: Context, ubicacion: UbicacionAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addUbicacion(ubicacion, "Bearer $token")
                if (response.isSuccessful) {
                    getLocations(token)
                    onResult(true, "Ubicación agregada correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /**
     * 🔹 Función para actualizar una ubicación existente
     */
    fun updateLocations(id: Int, ubicacion: UbicacionAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateUbicacion(id, ubicacion, "Bearer $token")
                if (response.isSuccessful) {
                    getLocations(token)
                    onResult(true, "Ubicación actualizada correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /**
     * 🔹 Función para eliminar una ubicación existente
     */
    fun deleteLocation(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteUbicacion(id, "Bearer $token")
                if (response.isSuccessful) {
                    getLocations(token)
                    onResult(true, "Ubicación eliminada correctamente")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, errorBody)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Obtener Eventos
    fun getEvent(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getEventos("Bearer $token")
                if (response.isSuccessful) {
                    _listaEventos.postValue(response.body())
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener eventos: $error")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción al obtener eventos: ${e.message}")
            }
        }
    }


    // 🔹 Agregar Evento
    fun addEvent(evento: EventoAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addEvento(evento, "Bearer $token")
                if (response.isSuccessful) {
                    getEvent(token)
                    onResult(true, "Evento agregado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Actualizar Evento
    fun updateEvent(id: Int, evento: EventoAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateEvento(id, evento, "Bearer $token")
                if (response.isSuccessful) {
                    getEvent(token)
                    onResult(true, "Evento actualizado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Eliminar Evento
    fun deleteEvent(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteEvento(id, "Bearer $token")
                if (response.isSuccessful) {
                    getEvent(token)
                    onResult(true, "Evento eliminado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    /*fun sendPhotoAPC(localFilePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(localFilePath)

            // 🔹 Verificar si el archivo local existe antes de enviarlo
            if (!file.exists()) {
                Log.e("educontrol", "❌ Error: El archivo no existe en la ruta local: $localFilePath")
                return@launch
            }

            val fileName = file.name
            val smbUrl = "smb://192.168.1.129/uploads/$fileName"
            val usuario = "smbuser"
            val password = "123456"

            try {
                // 🔹 Configurar JCIFS-NG
                val properties = Properties().apply {
                    setProperty("jcifs.smb.client.dfs.disabled", "true")
                    setProperty("jcifs.smb.client.soTimeout", "30000") // Timeout de 30s
                }
                val config = PropertyConfiguration(properties)

                // 🔹 Crear contexto CIFS con autenticación
                val auth = NtlmPasswordAuthenticator(usuario, password)
                val cifsContext: CIFSContext = BaseContext(config).withCredentials(auth)

                // 🔹 Crear referencia al archivo SMB
                val smbFile = SmbFile(smbUrl, cifsContext)

                // 🔹 Escribir el archivo en la carpeta SMB
                FileInputStream(file).use { inputStream ->
                    SmbFileOutputStream(smbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Log.d("educontrol", "✅ Archivo Base64 enviado exitosamente a PC: $smbUrl")

            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al enviar el archivo a SMB: ${e.message}", e)
            }
        }
    }*/

    // 🔹 Obtener todos los usuarios (sin token)
    fun getUser(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getUser("Bearer $token")
                if (response.isSuccessful) {
                    _listaUsuarios.postValue(response.body())
                } else {
                    Log.e("EduControl", "❌ Error al obtener usuarios: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "❌ Excepción al obtener usuarios: ${e.message}")
            }
        }
    }

    fun addUser(usuario: UsuarioAdd, token: String) {
        viewModelScope.launch {
            try {
                val response = api.addUser(usuario, "Bearer $token")
                if (response.isSuccessful) {
                    usuarioAccionResultado.postValue("✅ Usuario registrado correctamente")
                    getUser(token)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "❌ Error desconocido"
                    usuarioAccionResultado.postValue(errorBody)
                }
            } catch (e: Exception) {
                usuarioAccionResultado.postValue("❌ Error en la red: ${e.message}")
            }
        }
    }

    fun updateUsuario(id: Int, usuario: UsuarioAdd, token: String) {
        viewModelScope.launch {
            try {
                val response = api.updateUser(id, usuario, "Bearer $token")
                if (response.isSuccessful) {
                    usuarioAccionResultado.postValue("✅ Usuario actualizado correctamente")
                    getUser(token)
                } else {
                    usuarioAccionResultado.postValue("❌ ${response.errorBody()?.string() ?: "Error al actualizar"}")
                }
            } catch (e: Exception) {
                usuarioAccionResultado.postValue("❌ ${e.message}")
            }
        }
    }

    fun deleteUsuario(id: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.deleteUser(id, "Bearer $token")
                if (response.isSuccessful) {
                    usuarioAccionResultado.postValue("✅ Usuario eliminado correctamente")
                    getUser(token)
                } else {
                    usuarioAccionResultado.postValue("❌ ${response.errorBody()?.string() ?: "Error al eliminar"}")
                }
            } catch (e: Exception) {
                usuarioAccionResultado.postValue("❌ ${e.message}")
            }
        }
    }


    fun getCursos(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getCursos("Bearer $token")
                if (response.isSuccessful) {
                    _listaCursos.postValue(response.body())
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener cursos: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "❌ Error al obtener cursos: ${e.message}")
            }
        }
    }

    fun addCurso(context: Context, curso: CursoAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addCurso(curso, "Bearer $token")
                if (response.isSuccessful) {
                    getCursos(token)
                    onResult(true, "Curso agregado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateCurso(id: Int, curso: CursoAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateCurso(id, curso, "Bearer $token")
                if (response.isSuccessful) {
                    getCursos(token)
                    onResult(true, "Curso actualizado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun deleteCurso(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteCurso(id, "Bearer $token")
                if (response.isSuccessful) {
                    getCursos(token)
                    onResult(true, "Curso eliminado correctamente")
                } else {
                    onResult(false, response.errorBody()?.string())
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Obtener todos los semestres
    fun getSemestres(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getSemestres("Bearer $token")
                if (response.isSuccessful) {
                    _listaSemestres.postValue(response.body())
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener semestres: $error")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción al obtener semestres: ${e.message}")
            }
        }
    }

    // 🔹 Agregar semestre
    fun addSemestre(
        semestre: SemestreAdd,
        token: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.addSemestre(semestre, "Bearer $token")
                if (response.isSuccessful) {
                    getSemestres(token)
                    onResult(true, "Semestre agregado correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Actualizar semestre
    fun updateSemestre(
        id: Int,
        semestre: SemestreAdd,
        token: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.updateSemestre(id, semestre, "Bearer $token")
                if (response.isSuccessful) {
                    getSemestres(token)
                    onResult(true, "Semestre actualizado correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Eliminar semestre
    fun deleteSemestre(
        id: Int,
        token: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = api.deleteSemestre(id, "Bearer $token")
                if (response.isSuccessful) {
                    getSemestres(token)
                    onResult(true, "Semestre eliminado correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Obtener asignaturas
    fun getSubjects(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getSubjects("Bearer $token")
                if (response.isSuccessful) {
                    _listaAsignaturas.postValue(response.body())
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("EduControl", "Error al obtener asignaturas: $error")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción al obtener asignaturas: ${e.message}")
            }
        }
    }

    // 🔹 Agregar asignatura
    fun addSubject(asignatura: AsignaturaAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addSubject(asignatura, "Bearer $token")
                if (response.isSuccessful) {
                    getSubjects(token)
                    onResult(true, "Asignatura agregada correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Actualizar asignatura
    fun updateSubject(id: Int, asignatura: AsignaturaAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateSubject(id, asignatura, "Bearer $token")
                if (response.isSuccessful) {
                    getSubjects(token)
                    onResult(true, "Asignatura actualizada correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // 🔹 Eliminar asignatura
    fun deleteSubject(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteSubject(id, "Bearer $token")
                if (response.isSuccessful) {
                    getSubjects(token)
                    onResult(true, "Asignatura eliminada correctamente")
                } else {
                    val error = response.errorBody()?.string() ?: "Error desconocido"
                    onResult(false, error)
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }


    fun getAcs(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getAllACS(token)
                if (response.isSuccessful) {
                    listaAcs.postValue(response.body())
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Error al obtener ACS", e)
            }
        }
    }

    fun addAcs(acs: ACSAdd, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addACS(token, acs)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateAcs(id: Int, acs: ACSAdd, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateACS(token, id, acs)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteAcs(id: Int, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteACS(token, id)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }


    fun getMultimediaNombre(id: Int, token: String, callback: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.getAllMultimedia("Bearer $token")
                if (response.isSuccessful) {
                    val multimedia = response.body()?.firstOrNull { it.id_multimedia == id }
                    val nombre = multimedia?.originalEnlace?.substringAfterLast("/")
                    Log.d("educontrol", "✅ Multimedia $id => $nombre")
                    callback(nombre)
                } else {
                    Log.e("educontrol", "❌ Error multimedia $id: ${response.code()}")
                    callback(null)
                }
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción multimedia $id: ${e.message}")
                callback(null)
            }
        }
    }

    fun getState(token: String) {
        viewModelScope.launch {
            try {
                Log.d("educontrol", "📡 Enviando petición: GET /estado_usuario")
                val response = api.getState("Bearer $token")
                if (response.isSuccessful) {
                    listaEstados.postValue(response.body())
                    Log.d("educontrol", "✅ Estados recibidos: ${response.body()?.size}")
                } else {
                    Log.e("educontrol", "❌ Error estados ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción estados: ${e.message}")
            }
        }
    }
    fun getProfesores(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getProfesores("Bearer $token")
                if (response.isSuccessful) {
                    listaProfesores.postValue(response.body())
                } else {
                    Log.e("EduControl", "Error al obtener profesores: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("EduControl", "Excepción en getProfesores", e)
            }
        }
    }

    // OBTENER TODAS LAS CLASES
    fun getClases(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getClases("Bearer $token")
                if (response.isSuccessful) {
                    _listaClases.postValue(response.body())
                } else {
                    Log.e("DataViewModel", "❌ Error al obtener clases: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DataViewModel", "❌ Excepción al obtener clases: ${e.message}", e)
            }
        }
    }

    // AÑADIR CLASE
    fun addClase(clase: ClaseAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addClase(clase, "Bearer $token")
                if (response.isSuccessful) {
                    getClases(token)
                    onResult(true, "✅ Clase creada correctamente")
                } else {
                    onResult(false, "❌ Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // ACTUALIZAR CLASE
    fun updateClase(id: Int, clase: ClaseAdd, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.updateClase(id, clase, "Bearer $token")
                if (response.isSuccessful) {
                    getClases(token)
                    onResult(true, "✅ Clase actualizada correctamente")
                } else {
                    onResult(false, "❌ Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    // ELIMINAR CLASE
    fun deleteClase(id: Int, token: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.deleteClase(id, "Bearer $token")
                if (response.isSuccessful) {
                    getClases(token)
                    onResult(true, "🗑️ Clase eliminada correctamente")
                } else {
                    onResult(false, "❌ Error: ${response.code()}")
                }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun insertWarning(warning: WarningRequest, token: String) {
        viewModelScope.launch {
            try {
                val response = api.insertWarning(warning, token)
                if (response.isSuccessful) {
                    _insertWarningStatus.value = "Advertencia registrada correctamente"
                    Log.d("educontrol", "✅ insertWarning OK")
                } else {
                    val error = response.errorBody()?.string()
                    _insertWarningStatus.value = "Error al registrar advertencia: $error"
                    Log.e("educontrol", "❌ insertWarning error: $error")
                }
            } catch (e: Exception) {
                _insertWarningStatus.value = "Error de red: ${e.message}"
                Log.e("educontrol", "❌ insertWarning exception: ${e.message}")
            }
        }
    }

    fun insertTest(test: TestRequest, idAcs: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.insertTestByACS(idAcs, test, token)
                if (response.isSuccessful) {
                    _insertTestStatus.value = "Examen registrado correctamente"
                    Log.d("educontrol", "✅ insertTest OK")
                } else {
                    val error = response.errorBody()?.string()
                    _insertTestStatus.value = "Error al registrar examen: $error"
                    Log.e("educontrol", "❌ insertTest error: $error")
                }
            } catch (e: Exception) {
                _insertTestStatus.value = "Error de red: ${e.message}"
                Log.e("educontrol", "❌ insertTest exception: ${e.message}")
            }
        }
    }


    fun getTestsPorProfesor(idProfesor: Int, token: String) {
        viewModelScope.launch {
            try {
                val acsResponse = api.getAllACS(token)
                if (acsResponse.isSuccessful) {
                    val acsDelProfesor = acsResponse.body()?.filter { it.profesorId == idProfesor } ?: listOf()
                    val testsAcumulados = mutableListOf<Test>()

                    for (acs in acsDelProfesor) {
                        val response = api.getTestsByACS(acs.id ?: continue, token)
                        if (response.isSuccessful) {
                            response.body()?.let { testsAcumulados.addAll(it) }
                        } else {
                            Log.e("educontrol", "⚠️ Error al obtener tests del ACS ${acs.id}: ${response.code()}")
                        }
                    }

                    _listaTests.value = testsAcumulados
                    Log.d("educontrol", "📥 Total tests cargados: ${testsAcumulados.size}")
                } else {
                    Log.e("educontrol", "❌ Error al obtener ACS: ${acsResponse.code()}")
                }

            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción al obtener tests del profesor: ${e.message}")
            }
        }
    }

    fun getWarningsByUsuario(userId: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getAllWarningsByRol(userId, token)
                if (response.isSuccessful) {
                    _listaAdvertencias.value = response.body() ?: emptyList()
                } else {
                    Log.e("educontrol", "❌ Error al obtener advertencias: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción en advertencias: ${e.message}")
            }
        }
    }

    fun getHorarioAlumno(userId: Int, token: String) {
        Log.d("educontrol", "🔄 Solicitando horario desde API...")
        viewModelScope.launch {
            try {
                val response = api.getHorarioAlumnoNuevo(userId, token)
                if (response.isSuccessful) {
                    Log.d("educontrol", "✅ Horario recibido: ${response.body()}")
                    _horarioAlumno.value = response.body()
                } else {
                    Log.e("educontrol", "❌ Error horario: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción horario: ${e.message}")
            }
        }
    }

    fun getTestsByUser(userId: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getAllTestsByUser(userId, "Bearer $token")
                if (response.isSuccessful) {
                    _listaTests.value = response.body() ?: emptyList()
                    Log.d("educontrol", "✅ Exámenes del usuario $userId cargados")
                } else {
                    Log.e("educontrol", "❌ Error exámenes user: ${response.code()}")
                    _listaTests.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Excepción al obtener exámenes user: ${e.message}")
                _listaTests.value = emptyList()
            }
        }
    }
    fun getNotasByUsuario(id: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getNotasByUsuario(id, "Bearer $token")
                listaNotas.postValue(response)
                Log.d("educontrol", "✅ Notas recibidas: $response")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al obtener notas", e)
            }
        }
    }

    fun insertNote(note: NoteRequest, token: String) {
        viewModelScope.launch {
            try {
                api.insertNote(note, "Bearer $token")
                insertNoteStatus.postValue("✅ Nota registrada correctamente")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al registrar nota", e)
                insertNoteStatus.postValue("❌ Error al registrar nota")
            }
        }
    }

    fun enviarNotificacion(request: NotificacionRequest, token: String) {
        viewModelScope.launch {
            try {
                val response = api.enviarNotificacion(request, "Bearer $token")
                if (response.isSuccessful) {
                    enviarNotificacionStatus.postValue("✅ Notificación enviada")
                } else {
                    enviarNotificacionStatus.postValue("❌ Error al enviar notificación")
                }
            } catch (e: Exception) {
                enviarNotificacionStatus.postValue("❌ Error de red")
            }
        }
    }

    fun getNotificacionesNoLeidas(idUsuario: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getNotificacionesNoLeidas(idUsuario, "Bearer $token")
                notificacionesNoLeidas.postValue(response)
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al obtener notificaciones no leídas: ${e.message}")
            }
        }
    }

    fun marcarNotificacionLeida(idNotificacion: Int, token: String) {
        viewModelScope.launch {
            try {
                api.marcarNotificacionLeida(idNotificacion, "Bearer $token")
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al marcar notificación como leída: ${e.message}")
            }
        }
    }

    fun getAdvertenciasPorTutor(id: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getAdvertenciasPorTutor("Bearer $token", id)
                listaAdvertenciasTutor.postValue(response)
            } catch (e: Exception) {
                Log.e("educontrol", "Error al cargar advertencias tutor", e)
            }
        }
    }

    fun getNotasPorTutor(id: Int, token: String) {
        viewModelScope.launch {
            try {
                val response = api.getNotasPorTutor("Bearer $token", id)
                listaNotasTutor.postValue(response)
            } catch (e: Exception) {
                Log.e("educontrol", "Error al cargar notas tutor", e)
            }
        }
    }

    fun getGuardians(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getAllGuardians("Bearer $token")
                guardians.postValue(response)
            } catch (e: Exception) {
                Log.e("educontrol", "❌ Error al cargar tutores", e)
            }
        }
    }

    fun getTodasNotificaciones(idUsuario: Int, token: String) {
        viewModelScope.launch {
            try {
                val resultado = api.getNotificacionesPorUsuario(idUsuario, "Bearer $token")
                listaTodasNotificaciones.postValue(resultado)
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
    }

}








