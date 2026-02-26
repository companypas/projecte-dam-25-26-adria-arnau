package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de reportes. */
interface ReportesRepository {

    /**
     * Crea un reporte sobre un producto.
     * @param productoId ID del producto a reportar
     * @param motivo Motivo del reporte
     * @return Flow con el resultado de la operación
     */
    fun reportarProducto(
        productoId: Int,
        motivo: String
    ): Flow<Resource<Unit>>

    /**
     * Crea un reporte sobre un comentario.
     * @param comentarioId ID del comentario a reportar
     * @param motivo Motivo del reporte
     * @return Flow con el resultado de la operación
     */
    fun reportarComentario(
        comentarioId: Int,
        motivo: String
    ): Flow<Resource<Unit>>
}
