package com.example.educontrol.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: Int,
    val nombre: String,
    val primer_apellido: String,
    val segundo_apellido: String,
    val movil: String,
    val email: String,
    val foto: String? = null,              // Opcional
    val fechaRegistro: String? = null,     // Opcional
    val codigoNfc: String? = null,         // Opcional
    val idColegio: String? = null,         // Opcional
    val idRol: String,                     // Este viene como String
    val activo: Boolean = true             // Valor por defecto
)

@Entity(tableName = "colegios")
data class ColegioEntity(
    @PrimaryKey val id_colegio: Int,
    val nombre: String,
    val direccion: String
)

@Entity(tableName = "ubicaciones")
data class UbicacionEntity(
    @PrimaryKey val id_ubicacion: Int,
    val id_colegio: Int,
    val tipo: String,
    val nombre: String,
    val nombre_colegio: String
)

