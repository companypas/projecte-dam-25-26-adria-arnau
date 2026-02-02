package com.example.pi_androidapp.data.repository

import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.data.mapper.toDomain
import com.example.pi_androidapp.data.remote.api.AuthApiService
import com.example.pi_androidapp.data.remote.dto.auth.LoginRequest
import com.example.pi_androidapp.data.remote.dto.auth.RegistroRequest
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementación del repositorio de autenticación. Gestiona el login, registro y persistencia de
 * sesión.
 *
 * @property authApiService Servicio de API para autenticación
 * @property encryptedPrefsManager Gestor de preferencias encriptadas
 */
@Singleton
class AuthRepositoryImpl
@Inject
constructor(
        private val authApiService: AuthApiService,
        private val encryptedPrefsManager: EncryptedPrefsManager
) : AuthRepository {

    /**
     * Iniciar sesión con email y contraseña. Guarda el token y datos del usuario de forma
     * encriptada.
     */
    override fun login(email: String, password: String): Flow<Resource<Usuario>> = flow {
        emit(Resource.Loading())
        try {
            val response = authApiService.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                val authResponse = response.body()
                val result = authResponse?.result

                if (result?.token != null && result.usuario != null) {
                    // Guardar token y datos del usuario
                    encryptedPrefsManager.saveAuthToken(result.token)
                    encryptedPrefsManager.saveUserData(
                            odooId = result.usuario.id,
                            userId = result.usuario.idUsuario,
                            email = result.usuario.email,
                            name = result.usuario.nombre
                    )

                    emit(Resource.Success(result.usuario.toDomain()))
                } else {
                    emit(Resource.Error("Respuesta inválida del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                emit(Resource.Error("Error de autenticación: $errorBody"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    /** Registrar un nuevo usuario. Tras el registro exitoso, guarda el token automáticamente. */
    override fun registro(
            nombre: String,
            email: String,
            password: String,
            telefono: String?,
            ubicacion: String?
    ): Flow<Resource<Usuario>> = flow {
        emit(Resource.Loading())
        try {
            val request =
                    RegistroRequest(
                            nombre = nombre,
                            email = email,
                            password = password,
                            telefono = telefono,
                            ubicacion = ubicacion
                    )

            val response = authApiService.registro(request)

            if (response.isSuccessful) {
                val authResponse = response.body()
                val result = authResponse?.result

                if (result?.token != null && result.usuario != null) {
                    // Guardar token y datos del usuario
                    encryptedPrefsManager.saveAuthToken(result.token)
                    encryptedPrefsManager.saveUserData(
                            odooId = result.usuario.id,
                            userId = result.usuario.idUsuario,
                            email = result.usuario.email,
                            name = result.usuario.nombre
                    )

                    emit(Resource.Success(result.usuario.toDomain()))
                } else {
                    emit(Resource.Error("Respuesta inválida del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                emit(Resource.Error("Error de registro: $errorBody"))
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error de conexión: ${e.localizedMessage}"))
        }
    }

    /** Obtener el usuario actual desde el almacenamiento local. */
    override suspend fun getCurrentUser(): Usuario? {
        if (!encryptedPrefsManager.isSessionActive()) return null

        return Usuario(
                id = encryptedPrefsManager.getOdooId(),
                idUsuario = encryptedPrefsManager.getUserId() ?: "",
                nombre = encryptedPrefsManager.getUserName() ?: "",
                email = encryptedPrefsManager.getUserEmail() ?: "",
                activo = true
        )
    }

    /** Verificar si hay una sesión activa. */
    override suspend fun isLoggedIn(): Boolean {
        return encryptedPrefsManager.isSessionActive()
    }

    /** Cerrar la sesión actual. */
    override suspend fun logout() {
        encryptedPrefsManager.clearSession()
    }
}
