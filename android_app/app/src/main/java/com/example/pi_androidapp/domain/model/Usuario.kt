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
 */
data class Usuario(
        val id: Int,
        val idUsuario: String,
        val nombre: String,
        val email: String,
        val telefono: String? = null,
        val ubicacion: String? = null,
        val fechaRegistro: String? = null,
        val activo: Boolean = true
)
