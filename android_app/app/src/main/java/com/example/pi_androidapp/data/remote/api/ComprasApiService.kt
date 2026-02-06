package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.compras.ComprasResultData
import com.example.pi_androidapp.data.remote.dto.compras.ConfirmCompraResultData
import com.example.pi_androidapp.data.remote.dto.compras.CreateCompraResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/** Servicio de API para compras usando JSON-RPC de Odoo. */
interface ComprasApiService {

    /** Listar todas las compras del usuario. */
    @POST("api/v1/compras")
    suspend fun listarCompras(
            @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<ComprasResultData>>

    /** Crear una nueva compra. */
    @POST("api/v1/compras/crear")
    suspend fun crearCompra(
            @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<CreateCompraResultData>>

    /** Confirmar una compra (solo vendedor). */
    @POST("api/v1/compras/{compra_id}/confirmar")
    suspend fun confirmarCompra(
            @Body request: JsonRpcRequest,
            @Path("compra_id") compraId: Int
    ): Response<JsonRpcResponse<ConfirmCompraResultData>>

    /** Rechazar una compra (solo vendedor). */
    @POST("api/v1/compras/{compra_id}/rechazar")
    suspend fun rechazarCompra(
            @Body request: JsonRpcRequest,
            @Path("compra_id") compraId: Int
    ): Response<JsonRpcResponse<ConfirmCompraResultData>>

    /** Cancelar una compra (solo comprador, solo si está pendiente). */
    @POST("api/v1/compras/{compra_id}/cancelar")
    suspend fun cancelarCompra(
            @Body request: JsonRpcRequest,
            @Path("compra_id") compraId: Int
    ): Response<JsonRpcResponse<ConfirmCompraResultData>>
}
