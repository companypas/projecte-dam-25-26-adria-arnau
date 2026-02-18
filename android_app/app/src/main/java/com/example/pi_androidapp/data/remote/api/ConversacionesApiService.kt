package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.conversaciones.ConversacionesResultData
import com.example.pi_androidapp.data.remote.dto.conversaciones.EnviarMensajeResultData
import com.example.pi_androidapp.data.remote.dto.conversaciones.IniciarChatResultData
import com.example.pi_androidapp.data.remote.dto.conversaciones.MensajesResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/** Servicio de API para conversaciones y mensajes usando JSON-RPC de Odoo. */
interface ConversacionesApiService {

    /** Listar conversaciones del usuario autenticado. */
    @POST("api/v1/conversaciones")
    suspend fun listarConversaciones(
            @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<ConversacionesResultData>>

    /** Obtener mensajes de una conversación. */
    @POST("api/v1/conversaciones/{conversacion_id}/mensajes/listar")
    suspend fun obtenerMensajes(
            @Body request: JsonRpcRequest,
            @Path("conversacion_id") conversacionId: Int
    ): Response<JsonRpcResponse<MensajesResultData>>

    /** Enviar un mensaje en una conversación. */
    @POST("api/v1/conversaciones/{conversacion_id}/mensajes/enviar")
    suspend fun enviarMensaje(
            @Body request: JsonRpcRequest,
            @Path("conversacion_id") conversacionId: Int
    ): Response<JsonRpcResponse<EnviarMensajeResultData>>

    /** Iniciar un chat sobre un producto. */
    @POST("api/v1/productos/{producto_id}/iniciar-chat")
    suspend fun iniciarChat(
            @Body request: JsonRpcRequest,
            @Path("producto_id") productoId: Int
    ): Response<JsonRpcResponse<IniciarChatResultData>>
}
