package com.example.pi_androidapp.data.remote.dto.productos

import com.google.gson.annotations.SerializedName

/**
 * Datos del resultado de listado de productos.
 * Esta es la estructura que viene dentro de `result` en la respuesta JSON-RPC.
 */
data class ProductosResultData(
    @SerializedName("total") val total: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("productos") val productos: List<ProductoDto>?
)

/**
 * DTO de un producto.
 * Coincide con la estructura de producto_to_dict() en utils.py
 */
data class ProductoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_producto") val idProducto: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("antiguedad_meses") val antiguedadMeses: Int?,
    @SerializedName("ubicacion") val ubicacion: String?,
    @SerializedName("estado_venta") val estadoVenta: String?,
    @SerializedName("categoria") val categoria: CategoriaDto?,
    @SerializedName("propietario") val propietario: PropietarioDto?,
    @SerializedName("etiquetas") val etiquetas: List<EtiquetaDto>?,
    @SerializedName("total_comentarios") val totalComentarios: Int?,
    @SerializedName("total_imagenes") val totalImagenes: Int?,
    @SerializedName("imagen_principal") val imagenPrincipal: String?,
    @SerializedName("imagenes") val imagenes: List<String>?,
    @SerializedName("fecha_publicacion") val fechaPublicacion: String?
)

/** DTO del propietario de un producto. */
data class PropietarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("valoracion") val valoracion: Double?
)

/** DTO de la categoría de un producto. */
data class CategoriaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** DTO de una etiqueta. */
data class EtiquetaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** DTO para crear un producto. */
data class CreateProductoRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("estado") val estado: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria_id") val categoriaId: Int,
    @SerializedName("imagenes") val imagenes: List<String>? = null
)

/** Datos del resultado de creación/actualización de producto. */
data class CreateProductoResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("producto") val producto: ProductoDto?
)
