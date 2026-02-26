package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.reportes.CrearReporteResultData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Servicio de API para reportes usando JSON-RPC de Odoo.
 */
interface ReportesApiService {

    /**
     * Crea un reporte (de producto, usuario o comentario).
     */
    @POST("api/v1/reportes")
    suspend fun crearReporte(
        @Body request: JsonRpcRequest
    ): Response<JsonRpcResponse<CrearReporteResultData>>
}
