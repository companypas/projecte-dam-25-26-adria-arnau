package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.local.AppDatabase
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.ProductosApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.repository.ProductosRepository
import com.example.piandroidapp.data.local.ProductoEntity
import com.example.piandroidapp.data.local.ProductoQueries
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de productos con caché local (SQLDelight).
 *
 * Estrategia: **network-first con fallback a caché** (TTL de 5 minutos).
 * - Siempre intenta la petición de red primero.
 * - Si la red falla ("unexpected end of stream", timeout, etc.) → devuelve caché.
 * - Tras cada petición exitosa → actualiza la caché.
 * - Al crear/editar/eliminar → invalida la caché del listado.
 */
@Singleton
class ProductosRepositoryImpl
@Inject
constructor(
    private val productosApiService: ProductosApiService,
    private val db: AppDatabase
) : ProductosRepository {

    companion object {
        /** TTL de 5 minutos para el listado de productos. */
        private const val CACHE_TTL_MS = 5 * 60 * 1000L
        private val gson = Gson()
        private val stringListType = object : TypeToken<List<String>>() {}.type
    }

    override fun listarProductos(
        offset: Int,
        limit: Int,
        categoriaId: Int?,
        estado: String?,
        busqueda: String?
    ): Flow<Resource<List<Producto>>> = flow {
        emit(Resource.Loading())

        // Solo usar caché para el listado sin filtros (offset=0, sin búsqueda activa)
        val useCache = offset == 0 && busqueda.isNullOrBlank()

        if (useCache) {
            val cachedList = if (categoriaId != null) {
                db.productoQueries.getProductosByCategoria(categoriaId.toLong()).executeAsList()
            } else {
                db.productoQueries.getAllProductos().executeAsList()
            }
            val oldestCachedAt = db.productoQueries.getOldestCachedAt().executeAsOneOrNull()?.MIN ?: 0L
            val isCacheValid = cachedList.isNotEmpty() &&
                    (System.currentTimeMillis() - oldestCachedAt) < CACHE_TTL_MS

            if (isCacheValid) {
                emit(Resource.Success(cachedList.map { it.toDomainFromCache() }))
                return@flow
            }
        }

        // Petición de red
        try {
            val params = mutableMapOf<String, Any?>("offset" to offset, "limit" to limit)
            categoriaId?.let { params["categoria_id"] = it }
            estado?.let { params["estado"] = it }
            busqueda?.let { params["nombre"] = it }

            val request = JsonRpcRequest(params = params)
            android.util.Log.d("ProductosRepo", "Request params: $params")
            val response = productosApiService.listarProductos(request)
            android.util.Log.d("ProductosRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()?.result
                if (result != null) {
                    val productos = result.productos?.map { it.toDomain() } ?: emptyList()

                    // Actualizar caché (solo para listado sin búsqueda)
                    if (useCache) {
                        val now = System.currentTimeMillis()
                        if (categoriaId == null) db.productoQueries.deleteAll()
                        productos.forEach { p -> db.productoQueries.upsertProducto(p, now) }
                    }
                    emit(Resource.Success(productos))
                } else if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error del servidor"))
                } else {
                    emit(Resource.Success(emptyList()))
                }
            } else {
                android.util.Log.e("ProductosRepo", "Response not successful: ${response.code()}")
                emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductosRepo", "Exception: ${e.message}", e)
            // Red caída → intentar devolver caché aunque haya expirado
            if (useCache) {
                val cachedList = if (categoriaId != null) {
                    db.productoQueries.getProductosByCategoria(categoriaId.toLong()).executeAsList()
                } else {
                    db.productoQueries.getAllProductos().executeAsList()
                }
                if (cachedList.isNotEmpty()) {
                    emit(Resource.Success(cachedList.map { it.toDomainFromCache() }))
                    return@flow
                }
            }
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    override fun obtenerProducto(productoId: Int): Flow<Resource<Producto>> = flow {
        emit(Resource.Loading())

        // Comprobar caché primero para detalle de producto
        val cached = db.productoQueries.getProducto(productoId.toLong()).executeAsOneOrNull()
        val isCacheValid = cached != null &&
                (System.currentTimeMillis() - cached.cachedAt) < CACHE_TTL_MS
        if (isCacheValid) {
            emit(Resource.Success(cached!!.toDomainFromCache()))
            return@flow
        }

        try {
            android.util.Log.d("ProductosRepo", "Obteniendo producto con ID: $productoId")
            val request = JsonRpcRequest()
            val response = productosApiService.obtenerProducto(request, productoId)
            android.util.Log.d("ProductosRepo", "Response code: ${response.code()}")

            if (response.isSuccessful) {
                val result = response.body()?.result
                if (result != null) {
                    val producto = result.toDomain()
                    // Guardar en caché
                    db.productoQueries.upsertProducto(producto, System.currentTimeMillis())
                    emit(Resource.Success(producto))
                } else if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error del servidor"))
                } else {
                    emit(Resource.Error("Producto no encontrado"))
                }
            } else {
                // Si falla la red pero tenemos caché (expirada), no rompemos la UX
                if (cached != null) emit(Resource.Success(cached.toDomainFromCache()))
                else emit(Resource.Error("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ProductosRepo", "Exception: ${e.message}", e)
            if (cached != null) emit(Resource.Success(cached.toDomainFromCache()))
            else emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
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
            val params = mapOf<String, Any?>(
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
                val result = response.body()?.result
                if (result?.producto != null) {
                    // Invalidar caché al crear un producto nuevo
                    db.productoQueries.deleteAll()
                    emit(Resource.Success(result.producto.id))
                } else if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al crear producto"))
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
            val params = mapOf<String, Any?>(
                "nombre" to nombre,
                "descripcion" to descripcion,
                "precio" to precio,
                "ubicacion" to ubicacion
            )
            val request = JsonRpcRequest(params = params)
            val response = productosApiService.actualizarProducto(request, productoId)

            if (response.isSuccessful) {
                if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al actualizar"))
                } else {
                    // Invalidar caché del producto editado
                    db.productoQueries.deleteProducto(productoId.toLong())
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
                if (response.body()?.error != null) {
                    emit(Resource.Error(response.body()?.error?.message ?: "Error al eliminar"))
                } else {
                    // Invalidar caché al eliminar
                    db.productoQueries.deleteProducto(productoId.toLong())
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

// ---------------------------------------------------------------------------
// Helpers de conversión caché → dominio
// ---------------------------------------------------------------------------

/** Convierte ProductoEntity (SQLDelight) al modelo de dominio. */
private fun ProductoEntity.toDomainFromCache(): Producto {
    val etiquetasList: List<String> = try {
        val type = object : TypeToken<List<String>>() {}.type
        Gson().fromJson(etiquetas, type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    return Producto(
        id = id.toInt(),
        idProducto = idProducto,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        estado = estado,
        estadoVenta = estadoVenta,
        ubicacion = ubicacion,
        antiguedadMeses = antiguedadMeses.toInt(),
        fechaPublicacion = fechaPublicacion,
        categoriaId = categoriaId.toInt(),
        categoriaNombre = categoriaNombre,
        propietarioId = propietarioId.toInt(),
        propietarioNombre = propietarioNombre,
        propietarioValoracion = propietarioValoracion,
        etiquetas = etiquetasList,
        totalComentarios = totalComentarios.toInt(),
        totalImagenes = totalImagenes.toInt(),
        imagenPrincipal = imagenPrincipal,
        imagenes = emptyList() // Las imágenes completas no se cachean (muy pesadas)
    )
}

/** Extensión para insertar un Producto en la caché SQLDelight. */
private fun ProductoQueries.upsertProducto(
    p: Producto,
    cachedAt: Long
) {
    upsertProducto(
        id = p.id.toLong(),
        idProducto = p.idProducto,
        nombre = p.nombre,
        descripcion = p.descripcion,
        precio = p.precio,
        estado = p.estado,
        estadoVenta = p.estadoVenta,
        ubicacion = p.ubicacion,
        antiguedadMeses = p.antiguedadMeses.toLong(),
        fechaPublicacion = p.fechaPublicacion,
        categoriaId = p.categoriaId.toLong(),
        categoriaNombre = p.categoriaNombre,
        propietarioId = p.propietarioId.toLong(),
        propietarioNombre = p.propietarioNombre,
        propietarioValoracion = p.propietarioValoracion,
        etiquetas = Gson().toJson(p.etiquetas),
        totalComentarios = p.totalComentarios.toLong(),
        totalImagenes = p.totalImagenes.toLong(),
        imagenPrincipal = p.imagenPrincipal,
        cachedAt = cachedAt
    )
}
