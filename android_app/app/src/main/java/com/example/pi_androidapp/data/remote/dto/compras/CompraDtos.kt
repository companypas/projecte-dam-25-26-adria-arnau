package com.example.pi_androidapp.data.remote.dto.compras

import com.google.gson.annotations.SerializedName

/**
 * Datos del resultado de listado de compras.
 * Esta es la estructura que viene dentro de `result` en la respuesta JSON-RPC.
 */
data class ComprasResultData(
    @SerializedName("total") val total: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("compras") val compras: List<CompraDto>?
)

/**
 * DTO de una compra.
 */
data class CompraDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_compra") val idCompra: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("monto") val monto: Double?,
    @SerializedName("fecha") val fechaCreacion: String?,
    @SerializedName("fecha_confirmacion") val fechaConfirmacion: String?,
    @SerializedName("comprador") val comprador: PersonaDto?,
    @SerializedName("vendedor") val vendedor: PersonaDto?,
    @SerializedName("producto") val producto: ProductoCompraDto?
)

/** DTO de un usuario (comprador/vendedor). */
data class PersonaDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** DTO de un producto en una compra. */
data class ProductoCompraDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("precio") val precio: Double?,
    @SerializedName("imagen") val imagen: String?
)

/** Respuesta al crear una compra. */
data class CreateCompraResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("compra_id") val compraId: Int?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: Int?
)

/** Respuesta al confirmar una compra. */
data class ConfirmCompraResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: Int?
)
