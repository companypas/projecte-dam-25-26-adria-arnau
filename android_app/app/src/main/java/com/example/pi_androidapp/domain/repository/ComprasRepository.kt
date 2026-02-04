package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Compra
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de compras. Define las operaciones de gestión de compras. */
interface ComprasRepository {

    /**
     * Listar compras del usuario autenticado.
     * @param offset Offset para paginación
     * @param limit Límite de resultados
     * @param tipo Filtrar por tipo (compras, ventas)
     * @param estado Filtrar por estado
     * @return Flow con lista de compras
     */
    fun listarCompras(
            offset: Int = 0,
            limit: Int = 20,
            tipo: String? = null,
            estado: String? = null
    ): Flow<Resource<List<Compra>>>

    /**
     * Crear una nueva compra.
     * @param productoId ID del producto a comprar
     * @return Flow con el ID de la compra creada
     */
    fun crearCompra(productoId: Int): Flow<Resource<Int>>

    /**
     * Cancelar una compra.
     * @param compraId ID de la compra
     * @return Flow con el resultado
     */
    fun cancelarCompra(compraId: Int): Flow<Resource<Unit>>

    /**
     * Confirmar una compra (solo vendedor).
     * @param compraId ID de la compra
     * @return Flow con el resultado
     */
    fun confirmarCompra(compraId: Int): Flow<Resource<Unit>>

    /**
     * Rechazar una compra (solo vendedor).
     * @param compraId ID de la compra
     * @return Flow con el resultado
     */
    fun rechazarCompra(compraId: Int): Flow<Resource<Unit>>
}
