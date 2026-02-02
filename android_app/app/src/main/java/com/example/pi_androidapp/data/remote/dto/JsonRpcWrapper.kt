package com.example.pi_androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wrapper para peticiones JSON-RPC de Odoo.
 * Todas las peticiones a la API deben usar este formato.
 */
data class JsonRpcRequest(
    @SerializedName("jsonrpc") val jsonrpc: String = "2.0",
    @SerializedName("method") val method: String = "call",
    @SerializedName("params") val params: Map<String, Any?> = emptyMap(),
    @SerializedName("id") val id: Int? = null
)

/**
 * Wrapper para respuestas JSON-RPC de Odoo.
 * Todas las respuestas de la API vienen en este formato.
 */
data class JsonRpcResponse<T>(
    @SerializedName("jsonrpc") val jsonrpc: String?,
    @SerializedName("id") val id: Int?,
    @SerializedName("result") val result: T?,
    @SerializedName("error") val error: JsonRpcError?
)

/**
 * Representación de errores JSON-RPC.
 */
data class JsonRpcError(
    @SerializedName("code") val code: Int?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: JsonRpcErrorData?
)

/**
 * Datos adicionales del error JSON-RPC.
 */
data class JsonRpcErrorData(
    @SerializedName("name") val name: String?,
    @SerializedName("debug") val debug: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("arguments") val arguments: List<String>?
)
