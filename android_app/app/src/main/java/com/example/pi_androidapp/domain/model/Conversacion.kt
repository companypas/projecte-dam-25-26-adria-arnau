package com.example.pi_androidapp.domain.model

/** Modelo de dominio que representa una conversación de chat. */
data class Conversacion(
        val id: Int,
        val asunto: String,
        val otroUsuario: OtroUsuarioInfo,
        val estado: String,
        val totalMensajes: Int,
        val ultimoMensaje: String?,
        val fechaUltimoMensaje: String?,
        val productoId: Int?
)

/** Información básica del otro usuario en la conversación. */
data class OtroUsuarioInfo(val id: Int, val nombre: String)

/** Modelo de dominio que representa un mensaje de chat. */
data class Mensaje(
        val id: Int,
        val contenido: String,
        val fechaEnvio: String?,
        val leido: Boolean,
        val remitenteId: Int,
        val remitenteNombre: String,
        val esDeComprador: Boolean,
        val esDeVendedor: Boolean
)
