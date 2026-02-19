package com.example.pi_androidapp.domain.repository

import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Producto
import kotlinx.coroutines.flow.Flow

/** Interfaz del repositorio de productos. Define las operaciones CRUD de productos. */
interface ProductosRepository {

    /**
     * Listar productos con filtros opcionales.
     * @param offset Offset para paginación
     * @param limit Límite de resultados
     * @param categoriaId Filtrar por categoría
     * @param estado Filtrar por estado de venta
     * @param busqueda Término de búsqueda
     * @return Flow con lista de productos
     */
    fun listarProductos(
            offset: Int = 0,
            limit: Int = 20,
            categoriaId: Int? = null,
            estado: String? = null,
            busqueda: String? = null
    ): Flow<Resource<List<Producto>>>

    /**
     * Obtener detalle de un producto.
     * @param productoId ID del producto
     * @return Flow con el producto
     */
    fun obtenerProducto(productoId: Int): Flow<Resource<Producto>>

    /**
     * Crear un nuevo producto.
     * @param nombre Nombre del producto
     * @param descripcion Descripción
     * @param precio Precio
     * @param estado Estado del producto (nuevo, segunda_mano)
     * @param ubicacion Ubicación
     * @param categoriaId ID de la categoría
     * @param imagenes Lista de imágenes en base64
     * @return Flow con el ID del producto creado
     */
    fun crearProducto(
            nombre: String,
            descripcion: String,
            precio: Double,
            estado: String,
            ubicacion: String,
            categoriaId: Int,
            imagenes: List<String>
    ): Flow<Resource<Int>>

    /**
     * Actualizar un producto existente.
     * @param productoId ID del producto a actualizar
     * @param nombre Nombre del producto
     * @param descripcion Descripción
     * @param precio Precio
     * @param ubicacion Ubicación
     * @return Flow con el resultado
     */
    fun actualizarProducto(
            productoId: Int,
            nombre: String,
            descripcion: String,
            precio: Double,
            ubicacion: String
    ): Flow<Resource<Unit>>

    /**
     * Eliminar un producto.
     * @param productoId ID del producto
     * @return Flow con el resultado
     */
    fun eliminarProducto(productoId: Int): Flow<Resource<Unit>>
}
