package com.example.pi_androidapp.core.util

/**
 * Wrapper genérico para manejar estados de las operaciones de red. Implementa el patrón Result para
 * gestión de errores.
 *
 * @param T Tipo de datos que contiene el recurso
 */
sealed class Resource<out T> {

    /**
     * Estado de éxito con datos.
     * @property data Los datos obtenidos
     */
    data class Success<T>(val data: T) : Resource<T>()

    /**
     * Estado de error con mensaje y datos opcionales.
     * @property message Mensaje de error
     * @property data Datos parciales (opcional)
     */
    data class Error<T>(val message: String, val data: T? = null) : Resource<T>()

    /**
     * Estado de carga.
     * @property data Datos en caché mientras se carga (opcional)
     */
    data class Loading<T>(val data: T? = null) : Resource<T>()

    /** Verifica si el recurso está en estado de éxito. */
    val isSuccess: Boolean
        get() = this is Success

    /** Verifica si el recurso está en estado de error. */
    val isError: Boolean
        get() = this is Error

    /** Verifica si el recurso está en estado de carga. */
    val isLoading: Boolean
        get() = this is Loading

    /** Obtiene los datos si existen, null en caso contrario. */
    fun getOrNull(): T? =
            when (this) {
                is Success -> data
                is Error -> data
                is Loading -> data
            }

    /** Transforma los datos usando la función proporcionada. */
    fun <R> map(transform: (T) -> R): Resource<R> =
            when (this) {
                is Success -> Success(transform(data))
                is Error -> Error(message, data?.let(transform))
                is Loading -> Loading(data?.let(transform))
            }
}
