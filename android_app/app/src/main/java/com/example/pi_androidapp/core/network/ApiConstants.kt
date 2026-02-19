package com.example.pi_androidapp.core.network

import com.example.pi_androidapp.BuildConfig

/** Constantes de configuración de la API. Centraliza URLs y configuraciones de red. */
object ApiConstants {

    /** URL base de la API REST de Odoo */
    const val BASE_URL = BuildConfig.API_BASE_URL

    /** Timeout de conexión en segundos */
    const val CONNECT_TIMEOUT = 30L

    /** Timeout de lectura en segundos */
    const val READ_TIMEOUT = 60L

    /** Timeout de escritura en segundos */
    const val WRITE_TIMEOUT = 60L

    /** Header de autorización */
    const val HEADER_AUTHORIZATION = "Authorization"

    /** Prefijo Bearer para el token */
    const val BEARER_PREFIX = "Bearer "

    /** Content-Type JSON */
    const val CONTENT_TYPE_JSON = "application/json"
}
