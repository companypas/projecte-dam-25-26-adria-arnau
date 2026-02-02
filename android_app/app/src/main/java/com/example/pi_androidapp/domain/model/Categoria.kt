package com.example.pi_androidapp.domain.model

/**
 * Modelo de dominio que representa una categoría de productos.
 *
 * @property id ID interno de la categoría
 * @property idCategoria ID único de la categoría
 * @property nombre Nombre de la categoría
 * @property descripcion Descripción de la categoría
 * @property totalProductos Número de productos en esta categoría
 * @property imagen Imagen de la categoría (Base64)
 */
data class Categoria(
    val id: Int,
    val idCategoria: String,
    val nombre: String,
    val descripcion: String? = null,
    val totalProductos: Int = 0,
    val imagen: String? = null
)
