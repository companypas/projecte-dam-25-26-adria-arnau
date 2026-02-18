package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.ConversacionesApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Conversacion
import com.example.pi_androidapp.domain.model.Mensaje
import com.example.pi_androidapp.domain.repository.ConversacionesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Implementación del repositorio de conversaciones. Usa JSON-RPC para comunicarse con la API. */
@Singleton
class ConversacionesRepositoryImpl
@Inject
constructor(private val conversacionesApiService: ConversacionesApiService) :
        ConversacionesRepository {

    override fun listarConversaciones(offset: Int, limit: Int): Flow<Resource<List<Conversacion>>> =
            flow {
                emit(Resource.Loading())
                try {
                    val params =
                            mapOf<String, Any?>(
                                    "offset" to offset,
                                    "limit" to limit
                            )
                    val request = JsonRpcRequest(params = params)
                    val response = conversacionesApiService.listarConversaciones(request)

                    if (response.isSuccessful) {
                        val jsonRpcResponse = response.body()
                        val result = jsonRpcResponse?.result

                        if (result != null) {
                            val conversaciones =
                                    result.conversaciones?.map { it.toDomain() } ?: emptyList()
                            emit(Resource.Success(conversaciones))
                        } else if (jsonRpcResponse?.error != null) {
                            emit(
                                    Resource.Error(
                                            jsonRpcResponse.error.message ?: "Error del servidor"
                                    )
                            )
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

    override fun obtenerMensajes(
            conversacionId: Int,
            offset: Int,
            limit: Int
    ): Flow<Resource<List<Mensaje>>> = flow {
        emit(Resource.Loading())
        try {
            val params =
                    mapOf<String, Any?>(
                            "offset" to offset,
                            "limit" to limit
                    )
            val request = JsonRpcRequest(params = params)
            val response = conversacionesApiService.obtenerMensajes(request, conversacionId)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result != null) {
                    val mensajes = result.mensajes?.map { it.toDomain() } ?: emptyList()
                    emit(Resource.Success(mensajes))
                } else if (jsonRpcResponse?.error != null) {
                    emit(
                            Resource.Error(
                                    jsonRpcResponse.error.message ?: "Error del servidor"
                            )
                    )
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

    override fun enviarMensaje(conversacionId: Int, contenido: String): Flow<Resource<Mensaje>> =
            flow {
                emit(Resource.Loading())
                try {
                    val params = mapOf<String, Any?>("contenido" to contenido)
                    val request = JsonRpcRequest(params = params)
                    val response =
                            conversacionesApiService.enviarMensaje(request, conversacionId)

                    if (response.isSuccessful) {
                        val jsonRpcResponse = response.body()
                        val result = jsonRpcResponse?.result

                        if (result?.error != null) {
                            emit(Resource.Error(result.error))
                        } else if (result?.data != null) {
                            emit(Resource.Success(result.data.toDomain()))
                        } else if (jsonRpcResponse?.error != null) {
                            emit(
                                    Resource.Error(
                                            jsonRpcResponse.error.message
                                                    ?: "Error al enviar mensaje"
                                    )
                            )
                        } else {
                            emit(Resource.Error("Error al enviar mensaje"))
                        }
                    } else {
                        emit(Resource.Error("Error: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
                }
            }

    override fun iniciarChat(productoId: Int): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val request = JsonRpcRequest()
            val response = conversacionesApiService.iniciarChat(request, productoId)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result?.error != null) {
                    emit(Resource.Error(result.error))
                } else if (result?.conversacionId != null) {
                    emit(Resource.Success(result.conversacionId))
                } else if (jsonRpcResponse?.error != null) {
                    emit(
                            Resource.Error(
                                    jsonRpcResponse.error.message ?: "Error al iniciar chat"
                            )
                    )
                } else {
                    emit(Resource.Error("Error al iniciar chat"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }
}
