package com.example.pi_androidapp.data.remote.dto.comentarios

import com.google.gson.annotations.SerializedName

/** DTO de un usuario dentro de un comentario. */
data class ComentarioUsuarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre") val nombre: String?
)

/** DTO de un comentario. Coincide con comentario_to_dict() en utils.py */
data class ComentarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("id_comentario") val idComentario: String?,
    @SerializedName("texto") val texto: String?,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("editado") val editado: Boolean?,
    @SerializedName("usuario") val usuario: ComentarioUsuarioDto?,
    @SerializedName("total_reportes") val totalReportes: Int?
)

/** Resultado de listar comentarios de un producto. */
data class ComentariosResultData(
    @SerializedName("total") val total: Int?,
    @SerializedName("offset") val offset: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("comentarios") val comentarios: List<ComentarioDto>?
)

/** Resultado de crear un comentario. */
data class CrearComentarioResultData(
    @SerializedName("mensaje") val mensaje: String?,
    @SerializedName("comentario") val comentario: ComentarioDto?,
    @SerializedName("error") val error: String?  // Mensaje de error del backend (si falla)
)
