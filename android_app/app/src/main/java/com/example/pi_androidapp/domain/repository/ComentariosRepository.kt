package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Comentario
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de comentarios. */
interface ComentariosRepository {

    /**
     * Obtiene los comentarios de un producto.
     * @param productoId ID del producto
     * @param offset Offset para paginación
     * @param limit Límite de resultados
     * @return Flow con la lista de comentarios
     */
    fun obtenerComentarios(
        productoId: Int,
        offset: Int = 0,
        limit: Int = 20
    ): Flow<Resource<List<Comentario>>>

    /**
     * Crea un comentario en un producto.
     * @param productoId ID del producto
     * @param texto Contenido del comentario
     * @return Flow con el comentario creado
     */
    fun crearComentario(
        productoId: Int,
        texto: String
    ): Flow<Resource<Comentario>>
}
