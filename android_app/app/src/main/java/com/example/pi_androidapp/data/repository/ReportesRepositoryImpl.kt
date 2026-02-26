package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.remote.api.ReportesApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.repository.ReportesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Implementación del repositorio de reportes. */
@Singleton
class ReportesRepositoryImpl
@Inject
constructor(
    private val reportesApiService: ReportesApiService
) : ReportesRepository {

    override fun reportarProducto(
        productoId: Int,
        motivo: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val params = mapOf<String, Any?>(
                "tipo_reporte" to "producto",
                "motivo" to motivo,
                "producto_id" to productoId
            )
            val request = JsonRpcRequest(params = params)
            val response = reportesApiService.crearReporte(request)

            if (response.isSuccessful) {
                if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al enviar reporte"))
                } else {
                    emit(Resource.Success(Unit))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun reportarComentario(
        comentarioId: Int,
        motivo: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val params = mapOf<String, Any?>(
                "tipo_reporte" to "comentario",
                "motivo" to motivo,
                "comentario_id" to comentarioId
            )
            val request = JsonRpcRequest(params = params)
            val response = reportesApiService.crearReporte(request)

            if (response.isSuccessful) {
                if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al reportar comentario"))
                } else {
                    emit(Resource.Success(Unit))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }
}
