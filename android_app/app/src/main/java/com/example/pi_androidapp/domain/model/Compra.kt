package com.example.pi_androidapp.domain.model

/**
 * Modelo de dominio que representa una compra en el marketplace.
 *
 * @property id ID interno de la compra
 * @property idCompra ID único de la compra
 * @property estado Estado de la compra
 * @property monto Monto total de la compra
 * @property fechaCreacion Fecha de creación de la compra
 * @property fechaConfirmacion Fecha de confirmación de la compra
 * @property comprador Información del comprador
 * @property vendedor Información del vendedor
 * @property producto Información del producto comprado
 */
data class Compra(
        val id: Int,
        val idCompra: String,
        val estado: EstadoCompra,
        val monto: Double = 0.0,
        val fechaCreacion: String? = null,
        val fechaConfirmacion: String? = null,
        val comprador: CompradorVendedorInfo? = null,
        val vendedor: CompradorVendedorInfo? = null,
        val producto: ProductoCompraInfo? = null
)

/** Información básica del comprador o vendedor. */
data class CompradorVendedorInfo(val id: Int, val nombre: String)

/** Información básica del producto en una compra. */
data class ProductoCompraInfo(
        val id: Int,
        val nombre: String,
        val precio: Double,
        val imagen: String? = null
)

/** Estados posibles de una compra. */
enum class EstadoCompra(val valor: String) {
    PENDIENTE("pendiente"),
    PROCESANDO("procesando"),
    CONFIRMADA("confirmada"),
    CANCELADA("cancelada"),
    RECHAZADA("rechazada"),
    COMPLETADA("completada");

    companion object {
        fun fromString(value: String): EstadoCompra {
            return entries.find { it.valor == value } ?: PENDIENTE
        }
    }
}
