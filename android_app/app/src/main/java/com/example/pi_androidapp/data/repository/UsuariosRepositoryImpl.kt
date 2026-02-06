package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.UsuariosApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.UsuariosRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Implementación del repositorio de usuarios. Usa JSON-RPC para comunicarse con la API de Odoo. */
@Singleton
class UsuariosRepositoryImpl
@Inject
constructor(private val usuariosApiService: UsuariosApiService) : UsuariosRepository {

    override fun obtenerUsuario(usuarioId: Int): Flow<Resource<Usuario>> = flow {
        emit(Resource.Loading())
        try {
            val request = JsonRpcRequest()
            val response = usuariosApiService.obtenerUsuario(request, usuarioId)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result != null) {
                    emit(Resource.Success(result.toDomain()))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error del servidor"))
                } else {
                    emit(Resource.Error("Usuario no encontrado"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }
}
