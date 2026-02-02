package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de autenticación. Define las operaciones de autenticación disponibles.
 */
interface AuthRepository {

    /**
     * Iniciar sesión con email y contraseña.
     * @param email Email del usuario
     * @param password Contraseña
     * @return Flow con el resultado del login
     */
    fun login(email: String, password: String): Flow<Resource<Usuario>>

    /**
     * Registrar un nuevo usuario.
     * @param nombre Nombre del usuario
     * @param email Email del usuario
     * @param password Contraseña
     * @param telefono Teléfono (opcional)
     * @param ubicacion Ubicación (opcional)
     * @return Flow con el resultado del registro
     */
    fun registro(
            nombre: String,
            email: String,
            password: String,
            telefono: String?,
            ubicacion: String?
    ): Flow<Resource<Usuario>>

    /**
     * Obtener el usuario actual desde el almacenamiento local.
     * @return Usuario actual o null si no hay sesión
     */
    suspend fun getCurrentUser(): Usuario?

    /**
     * Verificar si hay una sesión activa.
     * @return true si hay sesión válida
     */
    suspend fun isLoggedIn(): Boolean

    /** Cerrar la sesión actual. */
    suspend fun logout()
}
