package com.example.pi_androidapp.core.network

import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de OkHttp que añade automáticamente el token JWT a todas las peticiones que lo
 * requieran.
 *
 * @property encryptedPrefsManager Gestor de preferencias encriptadas
 */
@Singleton
class AuthInterceptor
@Inject
constructor(private val encryptedPrefsManager: EncryptedPrefsManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Si no hay token, continuar sin modificar la petición
        val token = encryptedPrefsManager.getAuthToken()
        if (token.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        // Añadir header de autorización con el token
        val authenticatedRequest =
                originalRequest
                        .newBuilder()
                        .header(
                                ApiConstants.HEADER_AUTHORIZATION,
                                "${ApiConstants.BEARER_PREFIX}$token"
                        )
                        .header("Content-Type", ApiConstants.CONTENT_TYPE_JSON)
                        .build()

        return chain.proceed(authenticatedRequest)
    }
}
