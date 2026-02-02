package com.example.pi_androidapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase Application principal de la app. Inicializa Hilt para inyección de dependencias.
 *
 * @author PI_AndroidApp Team
 */
@HiltAndroidApp
class PIApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicialización adicional si es necesaria
    }
}
