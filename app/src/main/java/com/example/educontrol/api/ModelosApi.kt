package com.example.educontrol.api


import com.google.gson.annotations.SerializedName

// Modelo para el request del login
data class LoginRequest(
    val email: String,
    val password: String
)

// Modelo para la respuesta del login
data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("primer_apellido") val primerApellido: String,
    @SerializedName("segundo_apellido") val segundoApellido: String,
    @SerializedName("movil") val movil: String,
    @SerializedName("email") val email: String,
    @SerializedName("id_rol") val idRol: Int,
    @SerializedName("rol") val rol: String,
    @SerializedName("estado_usuario") val estadoUsuario: String,
    @SerializedName("codigo_activacion") val codigoActivacion: String?,
    @SerializedName("id_colegio") val idColegio: Int?,
    @SerializedName("id_estado") val idEstado: Int?,
    @SerializedName("id_multimedia") val idMultimedia: Int?,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: String,
    @SerializedName("app_mode") val appMode: String?,
    @SerializedName("contrasena") val contrasena: String?,
    @SerializedName("photo") val photo: String?
)


// Modelo para el request del sign-up
data class SignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("name") val name: String,
    @SerializedName("middleName") val middleName: String?,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("photo") val photo: String,
    @SerializedName("photoType") val photoType: String,
    @SerializedName("rolId") val rolId: Int,
    @SerializedName("guardians") val guardians: List<Int>? = null // ✅ lista de tutores
)


data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val password: String
)

data class RolResponse(
    @SerializedName("id_rol") val id: Int,        // ID del rol
    @SerializedName("rol") val nombre: String // Nombre del rol
)

data class Colegio(
    val id_colegio: Int,
    val nombre: String,
    val direccion: String
)

data class ColegioAdd(
    val nombre: String,
    val direccion: String
)

data class Ubicacion(
    val id_ubicacion: Int,
    val id_colegio: Int,
    val tipo: String,
    val nombre: String,
    val nombre_colegio: String
)

data class UbicacionAdd(
    val nombre: String,
    val tipo: String,
    val id_colegio: Int,

)

data class Evento(
    val id_evento: Int,
    val concepto: String,
    val fecha: String,
    val id_ubicacion: Int,
    val nombre_ubicacion: String,
    val nombre_colegio: String?,
    val tipo_ubicacion: String
)

data class EventoAdd(
    val id_ubicacion: Int,
    val fecha: String,
    val concepto: String,

)

data class Usuario(
    @SerializedName("id_usuario") val idUsuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("primer_apellido") val primerApellido: String,
    @SerializedName("segundo_apellido") val segundoApellido: String?,
    @SerializedName("movil") val movil: String?,
    @SerializedName("email") val email: String,
    @SerializedName("id_rol") val idRol: Int,
    @SerializedName("codigo_activacion") val codigoActivacion: String?,
    @SerializedName("id_colegio") val idColegio: Int?,
    @SerializedName("id_estado") val idEstado: Int?,
    @SerializedName("id_multimedia") val idMultimedia: Int?,
    @SerializedName("fecha_registro") val fechaRegistro: String,
    @SerializedName("fecha_actualizacion") val fechaActualizacion: String,
    @SerializedName("app_mode") val appMode: String?,
    @SerializedName("contrasena") val contrasena: String?,
    @SerializedName("apellidos") val apellidos: String?,
    @SerializedName("enlace") val photo: String?, // Foto codificada en base64
    @SerializedName("rol_usuario") val rolUsuario: String,
    @SerializedName("colegio_usuario") val colegioUsuario: String?,
    @SerializedName("subjects") val subjects: List<Int>?, // IDs de asignaturas
    @SerializedName("courseId") val courseId: Int? // Curso seleccionado
)

data class UsuarioAdd(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("middlename") val middlename: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("multimediaId") val multimediaId: Int? = null,
    @SerializedName("rolId") val rolId: Int,
    @SerializedName("stateId") val stateId: Int,
    @SerializedName("schoolId") val schoolId: Int,
    @SerializedName("photo") val photo: String?,
    @SerializedName("phototype") val phototype: String?,
    @SerializedName("codigo_activacion") val codigoActivacion: String? = null,
    @SerializedName("subjects") val subjects: List<Int>?,
    @SerializedName("courseId") val courseId: Int?
)

data class UsuarioUpdate(
    @SerializedName("name") val name: String,
    @SerializedName("middlename") val middlename: String,
    @SerializedName("lastname") val lastname: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("rolId") val rolId: Int,
    @SerializedName("stateId") val stateId: Int,
    @SerializedName("schoolId") val schoolId: Int,
    @SerializedName("photo") val photo: String?, // Base64 si es necesario
    @SerializedName("phototype") val phototype: String?,
    @SerializedName("codigo_activacion") val codigoActivacion: String?,
    @SerializedName("subjects") val subjects: List<Int>?,
    @SerializedName("courseId") val courseId: Int?
)

data class Curso(
    val id_curso: Int,
    val nombre: String,
    val grupo: String,
    val id_colegio: Int,
    val nombre_colegio: String? = null // opcional si lo devuelve el backend
)

data class CursoAdd(
    val nombre: String,
    val grupo: String,
    val id_colegio: Int
)

data class Semestre(
    val id_semestre: Int,
    val numero_semestre: Int,
    val fecha_inicio: String,
    val fecha_fin: String
)

data class SemestreAdd(
    val numero_semestre: Int,
    val fecha_inicio: String,
    val fecha_fin: String
)

data class Asignatura(
    val id_asignatura: Int,
    val nombre: String
)

data class AsignaturaAdd(
    val nombre: String
)


data class ACS(
    @SerializedName("id_acs") val id: Int? = null,
    @SerializedName("id_asignatura") val subjectId: Int,
    @SerializedName("id_curso") val courseId: Int,
    @SerializedName("id_semestre") val semesterId: Int,
    @SerializedName("id_profesor") val profesorId: Int,
    @SerializedName("id_colegio") val id_colegio: Int,

    // Campos de información adicional para mostrar
    @SerializedName("nombre_asignatura") val nombre_asignatura: String? = null,
    @SerializedName("nombre_curso") val nombre_curso: String? = null,
    @SerializedName("grupo_curso") val grupo_curso: String? = null,
    @SerializedName("curso_grupo") val curso_grupo: String? = null,
    @SerializedName("fecha_semestre") val fecha_semestre: String? = null,
    @SerializedName("nombre_profesor") val nombre_profesor: String? = null,
    @SerializedName("nombre") val nombre_colegio: String? = null, // Este es el nombre del colegio
)


data class ACSAdd(
    @SerializedName("profesorId") val profesorId: Int,
    @SerializedName("courseId") val courseId: Int,
    @SerializedName("subjectId") val subjectId: Int,
    @SerializedName("semesterId") val semesterId: Int
)

data class Profesor(
    @SerializedName("id_profesor") val id_profesor: Int,
    @SerializedName("id_usuario") val id_usuario: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("primer_apellido") val primer_apellido: String,
    @SerializedName("segundo_apellido") val segundo_apellido: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("movil") val movil: String?,
    @SerializedName("id_rol") val id_rol: Int,
    @SerializedName("id_estado") val id_estado: Int,
    @SerializedName("id_colegio") val id_colegio: Int, // 🔴 Este es el que necesitas
    @SerializedName("id_multimedia") val id_multimedia: Int?,
    @SerializedName("codigo_activacion") val codigo_activacion: String?,
    @SerializedName("fecha_registro") val fecha_registro: String?,
    @SerializedName("fecha_actualizacion") val fecha_actualizacion: String?,
    @SerializedName("app_mode") val app_mode: String?,
    @SerializedName("contrasena") val contrasena: String?
)


data class Multimedia(
    val id_multimedia: Int,
    val enlace: String,
    val nombre: String,
    val originalEnlace: String?,
    val tipo: String
)

data class Estado(
    @SerializedName("id_estado") val id_estado: Int,
    @SerializedName("estado") val nombre: String
)

data class Clase(
    val id_clase: Int,
    val id_acs: Int,
    val id_ubicacion: Int,
    val dia_semana: List<Int>,
    val hora_inicio: String?,
    val hora_fin: String?,
    val ubicacion: String?, // 👈 este campo debe existir y mapearse desde el JSON
    val nombre_asignatura_acs: String?, // opcional
    val curso_grupo: String?,           // opcional
    val fecha_semestre: String?,        // opcional
    val nombre_profesor: String?        // opcional
)


data class ClaseAdd(
    val id_acs: Int?,             // opcional
    val locationId: Int,          // requerido
    val week_days: List<Int>,  // requerido
    val startTime: String,        // requerido
    val endTime: String           // requerido
)

data class WarningRequest(
    val usersIds: List<Int>,
    val id_acs: Int,
    val message: String
)

data class TestRequest(
    val descripcion: String,
    val fecha: String,
    val photo: String? = null,
    val phototype: String? = null
)


data class WarningResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("id_acs") val idAcs: Int,
    @SerializedName("nombre") val nombreAsignatura: String,
    @SerializedName("nombre_alumno") val nombreAlumno: String,
    @SerializedName("nombre_profesor") val nombreProfesor: String
)

//data class Test(
//    @SerializedName("id_test") val idTest: Int,
//    @SerializedName("id_acs") val id_acs: Int,
//    @SerializedName("nombre") val nombre: String,
//    @SerializedName("descripcion") val descripcion: String,
//    @SerializedName("fecha") val fecha: String,
//    @SerializedName("porcentaje") val porcentaje: Double
//)

//data class NoteRequest(
//    @SerializedName("id_usuario") val idUsuario: Int,
//    @SerializedName("id_examen") val idAcs: Int, // 👈 OJO AQUÍ
//    @SerializedName("nota") val nota: Double,
//    @SerializedName("photo") val photo: String? = null,
//    @SerializedName("phototype") val phototype: String? = null
//)

data class Advertencia(
    @SerializedName("id_acs") val id_acs: Int,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("descripcion") val descripcion: String
)

typealias HorarioResponse = Map<String, List<ClaseHorario>>

data class ClaseHorario(
    val asignatura: String,
    val profesor: String,
    val hora_inicio: String,
    val hora_fin: String,
    val ubicacion: String,
    val curso: String
)

sealed class HorarioUiItem {
    data class DiaHeader(val dia: String) : HorarioUiItem()
    data class ClaseItem(val clase: ClaseHorario) : HorarioUiItem()
}

data class Test(
    @SerializedName("id_examen") val id_test: Int,
    val descripcion: String,
    val fecha: String,
    val id_acs: Int,
    val nombreAsignatura: String? = null
)

data class NoteRequest(
    @SerializedName("id_usuario") val idUsuario: String,     // ¡string! como en backend
    @SerializedName("id_examen") val idExamen: Int,
    @SerializedName("photo") val photo: String?,
    @SerializedName("phototype") val phototype: String?,
    @SerializedName("nota") val nota: Double,
    @SerializedName("id_acs") val idAcs: Int
)

data class NoteResponse(
    val id_usuario: Int,
    val id_acs: Int,
    val nota: Double,
    val nombre_asignatura: String?,
    val descripcion: String?,
    val fecha: String?
)

data class NotificacionRequest(
    val id_usuario: Int,
    val titulo: String,
    val mensaje: String,
    val leido: Boolean = false // ✅ añádelo aquí
)

data class NotificacionResponse(
    val id_notificacion: Int,
    val id_usuario: Int,
    val titulo: String,
    val mensaje: String,
    val fecha_registro: String,
    val leido: Boolean? // ← importante que sea nullable
)

data class AdvertenciaAlumno(
    val id_warning: Int,
    val id_acs: Int,
    val nombre: String,           // nombre de la asignatura
    val nombre_alumno: String,
    val nombre_profesor: String,
    @SerializedName("mensaje") val message: String,
    val fecha_registro: String
)

data class NotaAlumno(
    val id_nota: Int,
    val id_examen: Int,
    val nombre_alumno: String,
    val nombre_profesor: String,
    val nombre_asignatura: String,
    val descripcion_examen: String,
    val nota: Double,
    val fecha_examen: String,
    val fecha_registro: String
)

