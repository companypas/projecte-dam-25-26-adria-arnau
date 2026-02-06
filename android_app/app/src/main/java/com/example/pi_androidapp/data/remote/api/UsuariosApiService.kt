package com.example.pi_androidapp.data.remote.api

import com.example.pi_androidapp.data.remote.dto.JsonRpcRequest
import com.example.pi_androidapp.data.remote.dto.JsonRpcResponse
import com.example.pi_androidapp.data.remote.dto.auth.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Servicio de API para usuarios usando JSON-RPC de Odoo. Permite obtener perfiles públicos de
 * usuarios.
 */
interface UsuariosApiService {

    /**
     * Obtener perfil público de un usuario.
     * @param request Petición JSON-RPC
     * @param usuarioId ID del usuario a consultar
     * @return Respuesta con los datos del usuario
     */
    @POST("api/v1/usuarios/{usuario_id}")
    suspend fun obtenerUsuario(
            @Body request: JsonRpcRequest,
            @Path("usuario_id") usuarioId: Int
    ): Response<JsonRpcResponse<UsuarioDto>>
}
