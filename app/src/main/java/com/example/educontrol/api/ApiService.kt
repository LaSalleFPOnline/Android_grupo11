package com.example.educontrol.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<ResponseBody>

    @POST("/forgot-password")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<ResponseBody>

    // 🔹 Restablecer contraseña con token
    @POST("/reset-password")
    suspend fun resetPassword(
        @Header("Authorization") token: String, // El token se debe pasar en los headers
        @Body request: ResetPasswordRequest
    ): Response<Void>

    // 🔹 Obtener todos los roles
    @GET("/rol")
    suspend fun getRoles(
        @Header("Authorization") token: String
    ): Response<List<RolResponse>>

    @GET("/school")
    suspend fun getColegios(
        @Header("Authorization") token: String
    ): Response<List<Colegio>>



    // 🔹 Agregar un nuevo colegio
    @POST("/school")
    suspend fun addColegio(
        @Body colegio: ColegioAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Actualizar un colegio
    @PUT("/school/{id}")
    suspend fun updateColegio(
        @Path("id") id: Int,
        @Body colegio: Colegio,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Eliminar un colegio
    @DELETE("/school/{id}")
    suspend fun deleteColegio(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/location")
    suspend fun getUbicaciones(
        @Header("Authorization") token: String
    ): Response<List<Ubicacion>>

    @POST("/location")
    suspend fun addUbicacion(
        @Body ubicacion: UbicacionAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("/location/{id}")
    suspend fun updateUbicacion(
        @Path("id") id: Int,
        @Body ubicacion: UbicacionAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("/location/{id}")
    suspend fun deleteUbicacion(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Obtener todos los eventos
    @GET("/event")
    suspend fun getEventos(
        @Header("Authorization") token: String
    ): Response<List<Evento>>

    // 🔹 Agregar nuevo evento
    @POST("/event")
    suspend fun addEvento(
        @Body evento: EventoAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Actualizar evento
    @PUT("/event/{id}")
    suspend fun updateEvento(
        @Path("id") id: Int,
        @Body evento: EventoAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    // 🔹 Eliminar evento
    @DELETE("/event/{id}")
    suspend fun deleteEvento(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/user")
    suspend fun getUser(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    @POST("/user")
    suspend fun addUser(
        @Body usuario: UsuarioAdd,
        @Header("Authorization") token: String
    ): Response<Unit>


    @PUT("/user/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body usuario: UsuarioAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("/user/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/course")
    suspend fun getCursos(
        @Header("Authorization") token: String
    ): Response<List<Curso>>

    @POST("/course")
    suspend fun addCurso(
        @Body curso: CursoAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("/course/{id}")
    suspend fun updateCurso(
        @Path("id") id: Int,
        @Body curso: CursoAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("/course/{id}")
    suspend fun deleteCurso(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/semester")
    suspend fun getSemestres(@Header("Authorization") token: String): Response<List<Semestre>>

    @POST("/semester")
    suspend fun addSemestre(
        @Body semestre: SemestreAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("/semester/{id}")
    suspend fun updateSemestre(
        @Path("id") id: Int,
        @Body semestre: SemestreAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("/semester/{id}")
    suspend fun deleteSemestre(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/subject")
    suspend fun getSubjects(@Header("Authorization") token: String): Response<List<Asignatura>>

    @POST("/subject")
    suspend fun addSubject(
        @Body asignatura: AsignaturaAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @PUT("/subject/{id}")
    suspend fun updateSubject(
        @Path("id") id: Int,
        @Body asignatura: AsignaturaAdd,
        @Header("Authorization") token: String
    ): Response<Unit>

    @DELETE("/subject/{id}")
    suspend fun deleteSubject(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("/subject_course_semester")
    suspend fun getAllACS(@Header("Authorization") token: String): Response<List<ACS>>

    @POST("/subject_course_semester")
    suspend fun addACS(@Header("Authorization") token: String, @Body request: ACSAdd): Response<Void>

    @PUT("/subject_course_semester/{id}")
    suspend fun updateACS(@Header("Authorization") token: String, @Path("id") id: Int, @Body request: ACSAdd): Response<Void>

    @DELETE("/subject_course_semester/{id}")
    suspend fun deleteACS(@Header("Authorization") token: String, @Path("id") id: Int): Response<Void>

    @GET("/profesor")
    suspend fun getProfesores(
        @Header("Authorization") token: String
    ): Response<List<Profesor>>

    // 🔹 Obtener todas las clases
    @GET("/class")
    suspend fun getClases(
        @Header("Authorization") token: String
    ): Response<List<Clase>>

    // 🔹 Crear nueva clase
    @POST("/class")
    suspend fun addClase(
        @Body clase: ClaseAdd,
        @Header("Authorization") token: String
    ): Response<Void>

    // 🔹 Actualizar clase existente
    @PUT("/class/{id}")
    suspend fun updateClase(
        @Path("id") id: Int,
        @Body clase: ClaseAdd,
        @Header("Authorization") token: String
    ): Response<Void>

    // 🔹 Eliminar clase por ID
    @DELETE("/class/{id}")
    suspend fun deleteClase(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>


    @POST("/warning")
    suspend fun insertWarning(
        @Body warning: WarningRequest,
        @Header("Authorization") token: String
    ): Response<Void>

    @POST("/subjectTest/{id}")
    suspend fun insertTestByACS(
        @Path("id") id: Int,
        @Body test: TestRequest,
        @Header("Authorization") token: String
    ): Response<Void>

    @POST("/subjectTestNote")
    suspend fun insertNoteForTest(
        @Body note: NoteRequest,
        @Header("Authorization") token: String
    ): Response<Void>

    // GET
    @GET("warning/user/{id}")
    suspend fun getAllWarningsByRol(
        @Path("id") userId: Int,
        @Header("Authorization") token: String
    ): Response<List<WarningResponse>>

    @GET("subjectMedia/{id}")
    suspend fun getAllSubjectMedia(
        @Path("id") acsId: Int,
        @Header("Authorization") token: String
    ): Response<List<Multimedia>>

    @GET("subjectTest/{id}")
    suspend fun getTestsByACS(
        @Path("id") acsId: Int,
        @Header("Authorization") token: String
    ): Response<List<Test>>

    @GET("subjectNotes/{id}")
    suspend fun getNotesByACS(
        @Path("id") acsId: Int,
        @Header("Authorization") token: String
    ): Response<List<NoteRequest>>

    @GET("user/timetable/{id}")
    suspend fun getHorarioAlumnoNuevo(
        @Path("id") userId: Int,
        @Header("Authorization") token: String
    ): Response<HorarioResponse>

    @GET("subjectTest/user/{id}")
    suspend fun getAllTestsByUser(
        @Path("id") userId: Int,
        @Header("Authorization") token: String
    ): Response<List<Test>>

    @GET("subjectNotes/user/{id}")
    suspend fun getNotasByUsuario(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): List<NoteResponse>

    @POST("subjectTestNote")
    suspend fun insertNote(
        @Body noteRequest: NoteRequest,
        @Header("Authorization") token: String
    ): Response<Unit>

    @POST("notification")
    suspend fun enviarNotificacion(
        @Body request: NotificacionRequest,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("notification/unreaded/{id}")
    suspend fun getNotificacionesNoLeidas(
        @Path("id") idUsuario: Int,
        @Header("Authorization") token: String
    ): List<NotificacionResponse>

    @POST("notification/markAsRead/{id}")
    suspend fun marcarNotificacionLeida(
        @Path("id") idNotificacion: Int,
        @Header("Authorization") token: String
    ): Response<Unit>

    @GET("notification/{id}")
    suspend fun getNotificacionesPorUsuario(
        @Path("id") idUsuario: Int,
        @Header("Authorization") token: String
    ): List<NotificacionResponse>


    @GET("/multimedia")
        suspend fun getAllMultimedia(
            @Header("Authorization") token: String
        ): Response<List<Multimedia>>

    @GET("/userState")
    suspend fun getState(
        @Header("Authorization") token: String
    ): Response<List<Estado>>

    @GET("warning/user/{id}")
    suspend fun getAdvertenciasPorTutor(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<AdvertenciaAlumno>

    @GET("subjectNotes/user/{id}")
    suspend fun getNotasPorTutor(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): List<NotaAlumno>

    @GET("guardian")
    suspend fun getAllGuardians(
        @Header("Authorization") token: String
    ): List<Usuario>




}

