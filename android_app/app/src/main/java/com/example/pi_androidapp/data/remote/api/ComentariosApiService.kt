package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.comentarios.ComentariosResultData
import com.example.pi_androidapp.data.remote.dto.comentarios.CrearComentarioResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.POST

/**
 * Servicio de API para comentarios usando JSON-RPC de Odoo.
 */
interface ComentariosApiService {

    /**
     * Obtiene los comentarios de un producto.
     */
    @POST("api/v1/productos/{producto_id}/comentarios")
    suspend fun obtenerComentarios(
        @Path("producto_id") productoId: Int,
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<ComentariosResultData>>

    /**
     * Crea un comentario en un producto.
     */
    @POST("api/v1/productos/{producto_id}/comentarios/crear")
    suspend fun crearComentario(
        @Path("producto_id") productoId: Int,
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<CrearComentarioResultData>>
}
