package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.productos.CreateProductoResultData
import com.example.pi_androidapp.data.remote.dto.productos.ProductoDto
import com.example.pi_androidapp.data.remote.dto.productos.ProductosResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Servicio de API para productos usando JSON-RPC de Odoo.
 * Todos los endpoints usan POST con el wrapper JSON-RPC.
 */
interface ProductosApiService {

    /**
     * Listar productos con filtros opcionales.
     * Los parámetros van en el body JSON-RPC.
     */
    @POST("api/v1/productos/listar")
    suspend fun listarProductos(
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<ProductosResultData>>

    /**
     * Obtener detalle de un producto.
     */
    @POST("api/v1/productos/{producto_id}")
    suspend fun obtenerProducto(
        @Body request: JsonRpcRequest,
        @retrofit2.http.Path("producto_id") productoId: Int
    ): Response<JsonRpcResponse<ProductoDto>>

    /**
     * Crear un nuevo producto.
     */
    @POST("api/v1/productos")
    suspend fun crearProducto(
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<CreateProductoResultData>>

    /**
     * Actualizar un producto existente.
     */
    @POST("api/v1/productos/{producto_id}")
    suspend fun actualizarProducto(
        @Body request: JsonRpcRequest,
        @retrofit2.http.Path("producto_id") productoId: Int
    ): Response<JsonRpcResponse<CreateProductoResultData>>

    /**
     * Eliminar un producto.
     */
    @POST("api/v1/productos/{producto_id}")
    suspend fun eliminarProducto(
        @Body request: JsonRpcRequest,
        @retrofit2.http.Path("producto_id") productoId: Int
    ): Response<JsonRpcResponse<Map<String, String>>>
}
