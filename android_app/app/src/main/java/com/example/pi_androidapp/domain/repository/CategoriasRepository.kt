package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Categoria
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de categorías. Define las operaciones para obtener categorías. */
interface CategoriasRepository {

    /**
     * Listar todas las categorías disponibles.
     * @return Flow con lista de categorías
     */
    fun listarCategorias(): Flow<Resource<List<Categoria>>>
}
