package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.ComprasApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Compra
import com.example.pi_androidapp.domain.repository.ComprasRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de compras.
 * Usa JSON-RPC para comunicarse con la API de Odoo.
 */
@Singleton
class ComprasRepositoryImpl @Inject constructor(
    private val comprasApiService: ComprasApiService
) : ComprasRepository {

    override fun listarCompras(
        offset: Int,
        limit: Int,
        tipo: String?,
        estado: String?
    ): Flow<Resource<List<Compra>>> = flow {
        emit(Resource.Loading())
        try {
            val params = mutableMapOf<String, Any?>(
                "offset" to offset,
                "limit" to limit
            )
            tipo?.let { params["tipo"] = it }
            estado?.let { params["estado"] = it }

            val request = JsonRpcRequest(params = params)
            val response = comprasApiService.listarCompras(request)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result != null) {
                    val compras = result.compras?.map { it.toDomain() } ?: emptyList()
                    emit(Resource.Success(compras))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error del servidor"))
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

    override fun crearCompra(productoId: Int): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val params = mapOf("producto_id" to productoId)
            val request = JsonRpcRequest(params = params)
            val response = comprasApiService.crearCompra(request)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result?.compraId != null) {
                    emit(Resource.Success(result.compraId))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error al crear compra"))
                } else {
                    emit(Resource.Error("Error al crear compra"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun cancelarCompra(compraId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            // TODO: Implementar cancelación con endpoint DELETE
            emit(Resource.Error("Funcionalidad no implementada"))
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun confirmarCompra(compraId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            // TODO: Implementar confirmación con endpoint POST /confirmar
            emit(Resource.Error("Funcionalidad no implementada"))
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }
}
