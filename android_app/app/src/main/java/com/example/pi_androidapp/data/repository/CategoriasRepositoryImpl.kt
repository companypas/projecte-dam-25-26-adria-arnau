package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.CategoriasApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Categoria
import com.example.pi_androidapp.domain.repository.CategoriasRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de categorías.
 * Usa JSON-RPC para comunicarse con la API de Odoo.
 */
@Singleton
class CategoriasRepositoryImpl @Inject constructor(
    private val categoriasApiService: CategoriasApiService
) : CategoriasRepository {

    override fun listarCategorias(): Flow<Resource<List<Categoria>>> = flow {
        emit(Resource.Loading())
        try {
            val request = JsonRpcRequest(params = mapOf("offset" to 0, "limit" to 100))
            val response = categoriasApiService.listarCategorias(request)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result != null) {
                    val categorias = result.categorias?.map { it.toDomain() } ?: emptyList()
                    emit(Resource.Success(categorias))
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
}
