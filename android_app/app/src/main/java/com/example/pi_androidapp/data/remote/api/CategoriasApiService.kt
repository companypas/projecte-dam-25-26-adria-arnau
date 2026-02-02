package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.categorias.CategoriasResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Servicio de API para categorías usando JSON-RPC de Odoo.
 */
interface CategoriasApiService {

    /**
     * Listar todas las categorías disponibles.
     */
    @POST("api/v1/categorias")
    suspend fun listarCategorias(
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<CategoriasResultData>>
}
