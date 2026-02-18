package com.example.pi_androidapp.data.remote.dto.conversaciones

import com.google.gson.annotations.SerializedName

/** Resultado paginado de conversaciones. */
data class ConversacionesResultData(
    @SerializedName("total") val total: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("conversaciones") val conversaciones: List<ConversacionDto>?
)

/** DTO de una conversación. */
data class ConversacionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("asunto") val asunto: String?,
    @SerializedName("otro_usuario") val otroUsuario: OtroUsuarioDto?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("total_mensajes") val totalMensajes: Int?,
    @SerializedName("ultimo_mensaje") val ultimoMensaje: String?,
    @SerializedName("fecha_ultimo_mensaje") val fechaUltimoMensaje: String?,
    @SerializedName("producto_id") val productoId: Int?
)

/** DTO del otro usuario en la conversación. */
data class OtroUsuarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** Resultado paginado de mensajes. */
data class MensajesResultData(
    @SerializedName("conversacion_id") val conversacionId: Int?,
    @SerializedName("total_mensajes") val totalMensajes: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("mensajes") val mensajes: List<MensajeDto>?
)

/** DTO de un mensaje. */
data class MensajeDto(
    @SerializedName("id") val id: Int,
    @SerializedName("contenido") val contenido: String?,
    @SerializedName("fecha_envio") val fechaEnvio: String?,
    @SerializedName("leido") val leido: Boolean?,
    @SerializedName("remitente") val remitente: RemitenteDto?,
    @SerializedName("es_de_comprador") val esDeComprador: Boolean?,
    @SerializedName("es_de_vendedor") val esDeVendedor: Boolean?
)

/** DTO del remitente de un mensaje. */
data class RemitenteDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** Resultado al enviar un mensaje. */
data class EnviarMensajeResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("data") val data: MensajeDto?,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: Int?
)

/** Resultado al iniciar un chat. */
data class IniciarChatResultData(
    @SerializedName("conversacion_id") val conversacionId: Int?,
    @SerializedName("asunto") val asunto: String?,
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("status") val status: Int?
)
