package com.example.educontrol.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

import androidx.room.OnConflictStrategy


@Dao
interface UsuarioDao {

    // ✅ Insertar usuario con estrategia de reemplazo si ya existe
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: UsuarioEntity)

    // ✅ Eliminar todos los usuarios de la tabla
    @Query("DELETE FROM usuarios")
    fun deleteAllUsers()

    // ✅ (Opcional) Obtener todos los usuarios
    @Query("SELECT * FROM usuarios")
    fun getAllUsuarios(): List<UsuarioEntity>

    // ✅ (Opcional) Obtener un usuario por su ID
    @Query("SELECT * FROM usuarios WHERE id = :userId")
    fun getUserById(userId: Int): UsuarioEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertColegios(colegios: List<ColegioEntity>)

    // Obtener todos los colegios guardados en Room
    @Query("SELECT * FROM colegios")
    fun getAllColegios(): List<ColegioEntity>

    // Eliminar todos los colegios
    @Query("DELETE FROM colegios")
    fun deleteAllColegios()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUbicaciones(ubicaciones: List<UbicacionEntity>)

    @Query("DELETE FROM ubicaciones")
    fun deleteAllUbicaciones()

    @Query("SELECT * FROM ubicaciones")
    fun getAllUbicaciones(): List<UbicacionEntity>
}