package com.example.pi_androidapp.domain.model

/**
 * Modelo de dominio para un comentario de producto.
 *
 * @property id ID interno de Odoo
 * @property idComentario ID legible (ej: COM-00001)
 * @property texto Contenido del comentario
 * @property fecha Fecha de publicación ISO-8601
 * @property editado Si el comentario fue editado
 * @property usuarioId ID del autor
 * @property usuarioNombre Nombre del autor
 * @property totalReportes Número de reportes activos sobre este comentario
 */
data class Comentario(
    val id: Int,
    val idComentario: String = "",
    val texto: String,
    val fecha: String? = null,
    val editado: Boolean = false,
    val usuarioId: Int = 0,
    val usuarioNombre: String = "",
    val totalReportes: Int = 0
)
