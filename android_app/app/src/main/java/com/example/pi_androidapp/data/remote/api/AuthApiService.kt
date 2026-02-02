package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.auth.AuthResponse
import com.example.pi_androidapp.data.remote.dto.auth.LoginRequest
import com.example.pi_androidapp.data.remote.dto.auth.RefreshTokenRequest
import com.example.pi_androidapp.data.remote.dto.auth.RegistroRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Servicio de API para autenticación. Define los endpoints de login, registro y refresh token. */
interface AuthApiService {

    /**
     * Iniciar sesión con email y contraseña.
     * @param request Datos de login (email, password)
     * @return Respuesta con token JWT y datos del usuario
     */
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    /**
     * Registrar un nuevo usuario.
     * @param request Datos de registro
     * @return Respuesta con token JWT y datos del usuario creado
     */
    @POST("api/v1/auth/registro")
    suspend fun registro(@Body request: RegistroRequest): Response<AuthResponse>

    /**
     * Refrescar el token JWT.
     * @param request Token actual
     * @return Respuesta con el nuevo token
     */
    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
}
