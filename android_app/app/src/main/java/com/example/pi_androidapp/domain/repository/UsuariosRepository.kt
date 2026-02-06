package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz del repositorio de usuarios. Define las operaciones para obtener perfiles de usuarios.
 */
interface UsuariosRepository {

    /**
     * Obtener el perfil público de un usuario.
     * @param usuarioId ID del usuario
     * @return Flow con el resultado
     */
    fun obtenerUsuario(usuarioId: Int): Flow<Resource<Usuario>>
}
