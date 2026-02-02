package com.example.pi_androidapp.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/** DTO para la petición de login. */
data class LoginRequest(
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String
)

/** DTO para la petición de registro. */
data class RegistroRequest(
        @SerializedName("nombre") val nombre: String,
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String,
        @SerializedName("telefono") val telefono: String? = null,
        @SerializedName("ubicacion") val ubicacion: String? = null
)

/** DTO para la respuesta de login/registro exitoso. */
data class AuthResponse(@SerializedName("result") val result: AuthResultData?)

/** Datos del resultado de autenticación. */
data class AuthResultData(
        @SerializedName("mensaje") val mensaje: String?,
        @SerializedName("token") val token: String?,
        @SerializedName("usuario") val usuario: UsuarioDto?
)

/** DTO del usuario devuelto en respuestas de autenticación. */
data class UsuarioDto(
        @SerializedName("id") val id: Int,
        @SerializedName("id_usuario") val idUsuario: String,
        @SerializedName("nombre") val nombre: String,
        @SerializedName("email") val email: String,
        @SerializedName("telefono") val telefono: String?,
        @SerializedName("ubicacion") val ubicacion: String?,
        @SerializedName("fecha_registro") val fechaRegistro: String?,
        @SerializedName("activo") val activo: Boolean?
)

/** DTO para la petición de refresh token. */
data class RefreshTokenRequest(@SerializedName("token") val token: String)
