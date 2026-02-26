package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.local.AppDatabase
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.CategoriasApiService
import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.domain.model.Categoria
import com.example.pi_androidapp.domain.repository.CategoriasRepository
import com.example.piandroidapp.data.local.CategoriaEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de categorías con caché local (SQLDelight).
 *
 * Estrategia: **cache-first con TTL de 24 horas**.
 * - Si hay datos en caché y no han expirado → se devuelven directamente (sin red).
 * - Si la caché está vacía o ha expirado → se hace petición a la API, se guarda en BD y se emite.
 * - Si la petición falla pero hay caché (aunque expirada) → se devuelven los datos cacheados
 *   con un error informativo (modo offline).
 */
@Singleton
class CategoriasRepositoryImpl
@Inject
constructor(
    private val categoriasApiService: CategoriasApiService,
    private val db: AppDatabase
) : CategoriasRepository {

    companion object {
        /** TTL de la caché de categorías: 24 horas en ms. Las categorías cambian raramente. */
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L
    }

    override fun listarCategorias(): Flow<Resource<List<Categoria>>> = flow {
        emit(Resource.Loading())

        val cachedList = db.categoriaQueries.getAllCategorias().executeAsList()
        val oldestCachedAt = db.categoriaQueries.getOldestCachedAt().executeAsOneOrNull()?.MIN ?: 0L
        val isCacheValid = cachedList.isNotEmpty() &&
                (System.currentTimeMillis() - oldestCachedAt) < CACHE_TTL_MS

        if (isCacheValid) {
            // Caché válida → emitir directamente sin petición de red
            emit(Resource.Success(cachedList.map { it.toDomainFromCache() }))
            return@flow
        }

        // Caché vacía o expirada → pedir a la API
        try {
            val request = JsonRpcRequest(params = mapOf("offset" to 0, "limit" to 100))
            val response = categoriasApiService.listarCategorias(request)

            if (response.isSuccessful) {
                val result = response.body()?.result
                if (result != null) {
                    val categorias = result.categorias?.map { it.toDomain() } ?: emptyList()

                    // Guardar en caché
                    val now = System.currentTimeMillis()
                    db.categoriaQueries.deleteAll()
                    categorias.forEach { cat ->
                        db.categoriaQueries.upsertCategoria(
                            id = cat.id.toLong(),
                            idCategoria = cat.idCategoria,
                            nombre = cat.nombre,
                            descripcion = cat.descripcion,
                            totalProductos = cat.totalProductos.toLong(),
                            imagen = cat.imagen,
                            cachedAt = now
                        )
                    }
                    emit(Resource.Success(categorias))
                } else if (response.body()?.error != null) {
                    emitCacheOrError(cachedList, response.body()?.error?.message ?: "Error del servidor")
                } else {
                    emit(Resource.Success(emptyList()))
                }
            } else {
                emitCacheOrError(cachedList, "Error HTTP: ${response.code()}")
            }
        } catch (e: Exception) {
            // "unexpected end of stream" u otros errores de red → devolver caché si existe
            emitCacheOrError(cachedList, "Sin conexión: ${e.localizedMessage}")
        }
    }

    /**
     * Si hay datos en caché (aunque hayan expirado), los devuelve como éxito (modo offline).
     * Si no hay caché, emite un error.
     */
    private suspend fun kotlinx.coroutines.flow.FlowCollector<Resource<List<Categoria>>>.emitCacheOrError(
        cachedList: List<CategoriaEntity>,
        errorMsg: String
    ) {
        if (cachedList.isNotEmpty()) {
            emit(Resource.Success(cachedList.map { it.toDomainFromCache() }))
        } else {
            emit(Resource.Error(errorMsg))
        }
    }
}

/** Convierte una entidad de la BD local al modelo de dominio. */
private fun CategoriaEntity.toDomainFromCache(): Categoria {
    return Categoria(
        id = id.toInt(),
        idCategoria = idCategoria,
        nombre = nombre,
        descripcion = descripcion,
        totalProductos = totalProductos.toInt(),
        imagen = imagen
    )
}
