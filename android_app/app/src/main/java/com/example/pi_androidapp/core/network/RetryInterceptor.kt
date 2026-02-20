package com.example.pi_androidapp.core.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interceptor que implementa retry automático con backoff exponencial para errores de red temporales.
 * Solo reintenta en casos de errores de conexión recuperables.
 */
@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
        private const val MAX_RETRIES = 3
        private const val INITIAL_RETRY_DELAY_MS = 500L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        var lastResponse: Response? = null

        // Intentar hasta MAX_RETRIES veces (total de intentos = MAX_RETRIES + 1)
        for (attempt in 0..MAX_RETRIES) {
            try {
                // Si no es el primer intento, esperar antes de reintentar
                if (attempt > 0) {
                    val delay = INITIAL_RETRY_DELAY_MS * (1 shl (attempt - 1)) // Backoff exponencial
                    Log.d(TAG, "Reintentando petición (intento ${attempt + 1}/${MAX_RETRIES + 1}) después de ${delay}ms")
                    Thread.sleep(delay)
                }

                val response = chain.proceed(request)
                lastResponse = response

                // Si la respuesta es exitosa, retornar
                if (response.isSuccessful) {
                    return response
                }

                // Si es un error que no debe reintentarse, retornar la respuesta
                if (!shouldRetry(response.code)) {
                    return response
                }

                // Si llegamos aquí, es un error que puede reintentarse
                // Si es el último intento, retornar la respuesta de error
                if (attempt == MAX_RETRIES) {
                    Log.w(TAG, "Error ${response.code} después de ${MAX_RETRIES + 1} intentos")
                    return response
                }

                // Cerrar la respuesta antes de reintentar
                response.close()
                Log.d(TAG, "Respuesta con código ${response.code}, reintentando...")

            } catch (e: SocketTimeoutException) {
                lastException = e
                Log.w(TAG, "Timeout en intento ${attempt + 1}/${MAX_RETRIES + 1}: ${e.message}")
                if (attempt == MAX_RETRIES) {
                    throw IOException("Error de conexión: timeout después de ${MAX_RETRIES + 1} intentos", e)
                }
            } catch (e: ConnectException) {
                lastException = e
                Log.w(TAG, "Error de conexión en intento ${attempt + 1}/${MAX_RETRIES + 1}: ${e.message}")
                if (attempt == MAX_RETRIES) {
                    throw IOException("Error de conexión: no se pudo conectar después de ${MAX_RETRIES + 1} intentos", e)
                }
            } catch (e: UnknownHostException) {
                // Error de DNS, no tiene sentido reintentar
                Log.e(TAG, "Error de DNS: ${e.message}")
                throw IOException("Error de conexión: host desconocido", e)
            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Error de IO en intento ${attempt + 1}/${MAX_RETRIES + 1}: ${e.message}")
                if (attempt == MAX_RETRIES) {
                    throw IOException("Error de conexión después de ${MAX_RETRIES + 1} intentos: ${e.message}", e)
                }
            }
        }

        // Si llegamos aquí con una respuesta previa, retornarla
        lastResponse?.let { return it }

        // Si llegamos aquí, todos los intentos fallaron con excepciones
        throw IOException("Error de conexión después de ${MAX_RETRIES + 1} intentos", lastException)
    }

    /**
     * Determina si un código de estado HTTP debe reintentarse.
     * Solo se reintentan errores temporales del servidor (5xx) y algunos errores específicos.
     */
    private fun shouldRetry(code: Int): Boolean {
        return when (code) {
            // Errores temporales del servidor
            in 500..599 -> true
            // Gateway timeout
            504 -> true
            // Service unavailable
            503 -> true
            // Request timeout (aunque es 408, puede ser temporal)
            408 -> true
            // Otros errores no deben reintentarse
            else -> false
        }
    }
}
