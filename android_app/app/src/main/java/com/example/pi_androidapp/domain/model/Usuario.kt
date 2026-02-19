package com.example.pi_androidapp.domain.model

/**
 * Modelo de dominio que representa un usuario del marketplace.
 *
 * @property id ID interno del usuario
 * @property idUsuario ID único del usuario en el sistema (formato: USR-XXXXX)
 * @property nombre Nombre completo del usuario
 * @property email Correo electrónico
 * @property telefono Número de teléfono (opcional)
 * @property ubicacion Ubicación/dirección del usuario
 * @property fechaRegistro Fecha de registro en el sistema
 * @property activo Estado de la cuenta
 * @property antiguedad Meses desde el registro
 * @property valoracionPromedio Valoración promedio del usuario (0-5)
 * @property totalValoraciones Número total de valoraciones recibidas
 * @property totalProductosVenta Productos actualmente en venta
 * @property totalProductosVendidos Productos vendidos históricamente
 * @property totalProductosComprados Productos comprados históricamente
 * @property imagen Foto de perfil del usuario codificada en Base64 (opcional)
 */
data class Usuario(
    val id: Int,
    val idUsuario: String,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val ubicacion: String? = null,
    val fechaRegistro: String? = null,
    val activo: Boolean = true,
    val antiguedad: Int = 0,
    val valoracionPromedio: Double = 0.0,
    val totalValoraciones: Int = 0,
    val totalProductosVenta: Int = 0,
    val totalProductosVendidos: Int = 0,
    val totalProductosComprados: Int = 0,
    val imagen: String? = null
)
