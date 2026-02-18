package com.example.pi_androidapp.domain.model

/**
 * Modelo de dominio que representa un producto del marketplace.
 *
 * @property id ID interno del producto
 * @property idProducto ID único del producto (ej: PRD-00000001)
 * @property nombre Nombre del producto
 * @property descripcion Descripción detallada
 * @property precio Precio en euros
 * @property estado Estado del producto (nuevo, segunda_mano)
 * @property estadoVenta Estado de venta (disponible, vendido)
 * @property ubicacion Ubicación del producto
 * @property antiguedadMeses Antigüedad del producto en meses
 * @property fechaPublicacion Fecha de publicación
 * @property categoriaId ID de la categoría
 * @property categoriaNombre Nombre de la categoría
 * @property propietarioId ID del propietario
 * @property propietarioNombre Nombre del propietario
 * @property propietarioValoracion Valoración del propietario
 * @property imagenPrincipal Imagen principal codificada en Base64
 * @property totalComentarios Número total de comentarios
 */
data class Producto(
    val id: Int,
    val idProducto: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val estado: String,
    val estadoVenta: String,
    val ubicacion: String,
    val antiguedadMeses: Int = 0,
    val fechaPublicacion: String? = null,
    val categoriaId: Int = 0,
    val categoriaNombre: String = "",
    val propietarioId: Int = 0,
    val propietarioNombre: String = "",
    val propietarioValoracion: Double = 0.0,
    val etiquetas: List<String> = emptyList(),
    val totalComentarios: Int = 0,
    val totalImagenes: Int = 0,
    val imagenPrincipal: String? = null
)
