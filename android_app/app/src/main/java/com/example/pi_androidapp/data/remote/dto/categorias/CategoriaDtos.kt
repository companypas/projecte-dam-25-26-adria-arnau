package com.example.pi_androidapp.data.remote.dto.categorias

import com.google.gson.annotations.SerializedName

/**
 * Datos del resultado de listado de categorías.
 * Esta es la estructura que viene dentro de `result` en la respuesta JSON-RPC.
 */
data class CategoriasResultData(
    @SerializedName("total") val total: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("categorias") val categorias: List<CategoriaItemDto>?
)

/**
 * DTO de una categoría individual.
 * Coincide con la estructura del controlador de categorías.
 */
data class CategoriaItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_categoria") val idCategoria: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("total_productos") val totalProductos: Int?,
    @SerializedName("imagen") val imagen: String?
)
