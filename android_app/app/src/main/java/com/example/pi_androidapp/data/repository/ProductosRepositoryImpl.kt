package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.ProductosApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.repository.ProductosRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de productos. Usa JSON-RPC para comunicarse con la API de Odoo.
 */
@Singleton
class ProductosRepositoryImpl
@Inject
constructor(private val productosApiService: ProductosApiService) : ProductosRepository {

    override fun listarProductos(
            offset: Int,
            limit: Int,
            categoriaId: Int?,
            estado: String?,
            busqueda: String?
    ): Flow<Resource<List<Producto>>> = flow {
        emit(Resource.Loading())
        try {
            val params = mutableMapOf<String, Any?>("offset" to offset, "limit" to limit)
            categoriaId?.let { params["categoria_id"] = it }
            estado?.let { params["estado"] = it }
            busqueda?.let { params["nombre"] = it }

            val request = JsonRpcRequest(params = params)
            android.util.Log.d("ProductosRepo", "Request params: $params")
            val response = productosApiService.listarProductos(request)

            android.util.Log.d("ProductosRepo", "Response isSuccessful: ${response.isSuccessful}")
            android.util.Log.d("ProductosRepo", "Response code: ${response.code()}")
            android.util.Log.d("ProductosRepo", "Response body: ${response.body()}")

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                android.util.Log.d("ProductosRepo", "JsonRpcResponse: $jsonRpcResponse")
                android.util.Log.d("ProductosRepo", "Result: ${jsonRpcResponse?.result}")
                android.util.Log.d("ProductosRepo", "Error: ${jsonRpcResponse?.error}")

                val result = jsonRpcResponse?.result

                if (result != null) {
                    android.util.Log.d("ProductosRepo", "Products count: ${result.productos?.size}")
                    val productos = result.productos?.map { it.toDomain() } ?: emptyList()
                    emit(Resource.Success(productos))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error del servidor"))
                } else {
                    android.util.Log.d("ProductosRepo", "Result is null, returning empty list")
                    emit(Resource.Success(emptyList()))
                }
            } else {
                android.util.Log.e(
                        "ProductosRepo",
                        "Response not successful: ${response.errorBody()?.string()}"
                )
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductosRepo", "Exception: ${e.message}", e)
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun obtenerProducto(productoId: Int): Flow<Resource<Producto>> = flow {
        emit(Resource.Loading())
        try {
            android.util.Log.d("ProductosRepo", "Obteniendo producto con ID: $productoId")
            val request = JsonRpcRequest()
            val response = productosApiService.obtenerProducto(request, productoId)

            android.util.Log.d("ProductosRepo", "Response code: ${response.code()}")
            android.util.Log.d("ProductosRepo", "Response body: ${response.body()}")
            android.util.Log.d("ProductosRepo", "Response error body: ${response.errorBody()?.string()}")

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result != null) {
                    emit(Resource.Success(result.toDomain()))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error del servidor"))
                } else {
                    emit(Resource.Error("Producto no encontrado"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductosRepo", "Exception: ${e.message}", e)
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun crearProducto(
            nombre: String,
            descripcion: String,
            precio: Double,
            estado: String,
            ubicacion: String,
            categoriaId: Int,
            imagenes: List<String>
    ): Flow<Resource<Int>> = flow {
        emit(Resource.Loading())
        try {
            val params =
                    mapOf<String, Any?>(
                            "nombre" to nombre,
                            "descripcion" to descripcion,
                            "precio" to precio,
                            "estado" to estado,
                            "ubicacion" to ubicacion,

                            "categoria_id" to categoriaId,
                            "imagenes" to imagenes
                    )

            val request = JsonRpcRequest(params = params)
            val response = productosApiService.crearProducto(request)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                val result = jsonRpcResponse?.result

                if (result?.producto != null) {
                    emit(Resource.Success(result.producto.id))
                } else if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error al crear producto"))
                } else {
                    emit(Resource.Error("Error al crear producto"))
                }
            } else {
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun actualizarProducto(
            productoId: Int,
            nombre: String,
            descripcion: String,
            precio: Double,
            ubicacion: String
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val params =
                    mapOf<String, Any?>(
                            "nombre" to nombre,
                            "descripcion" to descripcion,
                            "precio" to precio,
                            "ubicacion" to ubicacion
                    )

            val request = JsonRpcRequest(params = params)
            val response = productosApiService.actualizarProducto(request, productoId)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error al actualizar"))
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

    override fun eliminarProducto(productoId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val request = JsonRpcRequest()
            val response = productosApiService.eliminarProducto(request, productoId)

            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                if (jsonRpcResponse?.error != null) {
                    emit(Resource.Error(jsonRpcResponse.error.message ?: "Error al eliminar"))
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
