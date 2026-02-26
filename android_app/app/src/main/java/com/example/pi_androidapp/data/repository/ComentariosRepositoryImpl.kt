package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.remote.api.ComentariosApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Comentario
import com.example.pi_androidapp.domain.repository.ComentariosRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Implementación del repositorio de comentarios. Hace peticiones directas a la API (sin caché). */
@Singleton
class ComentariosRepositoryImpl
@Inject
constructor(
    private val comentariosApiService: ComentariosApiService
) : ComentariosRepository {

    override fun obtenerComentarios(
        productoId: Int,
        offset: Int,
        limit: Int
    ): Flow<Resource<List<Comentario>>> = flow {
        emit(Resource.Loading())
        try {
            val params = mapOf<String, Any?>("offset" to offset, "limit" to limit)
            val request = JsonRpcRequest(params = params)
            val response = comentariosApiService.obtenerComentarios(productoId, request)

            if (response.isSuccessful) {
                val result = response.body()?.result
                if (result != null) {
                    val comentarios = result.comentarios?.map { dto ->
                        Comentario(
                            id = dto.id,
                            idComentario = dto.idComentario ?: "",
                            texto = dto.texto ?: "",
                            fecha = dto.fecha,
                            editado = dto.editado ?: false,
                            usuarioId = dto.usuario?.id ?: 0,
                            usuarioNombre = dto.usuario?.nombre ?: "",
                            totalReportes = dto.totalReportes ?: 0
                        )
                    } ?: emptyList()
                    emit(Resource.Success(comentarios))
                } else if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error del servidor"))
                } else {
                    emit(Resource.Success(emptyList()))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun crearComentario(
        productoId: Int,
        texto: String
    ): Flow<Resource<Comentario>> = flow {
        emit(Resource.Loading())
        try {
            val params = mapOf<String, Any?>("texto" to texto)
            val request = JsonRpcRequest(params = params)
            val response = comentariosApiService.crearComentario(productoId, request)

            if (response.isSuccessful) {
                val result = response.body()?.result
                val dto = result?.comentario
                if (dto != null) {
                    emit(Resource.Success(
                        Comentario(
                            id = dto.id,
                            idComentario = dto.idComentario ?: "",
                            texto = dto.texto ?: "",
                            fecha = dto.fecha,
                            editado = dto.editado ?: false,
                            usuarioId = dto.usuario?.id ?: 0,
                            usuarioNombre = dto.usuario?.nombre ?: "",
                            totalReportes = dto.totalReportes ?: 0
                        )
                    ))
                } else if (response.body()?.error != null) {
                    // Error a nivel protocolo JSON-RPC (ej: error interno de Odoo)
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al crear comentario"))
                } else if (result?.error != null) {
                    // Error de lógica de negocio devuelto por el backend en el campo result.error
                    emit(Resource.Error(result.error))
                } else {
                    emit(Resource.Error("Error al crear comentario"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }
}
