package com.example.pi_androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.pi_androidapp.ui.navigation.NavGraph
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.pi_androidapp.ui.theme.PI_AndroidAppTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad principal de la aplicación Vendoo. Configurada con Hilt para inyección de
 * dependencias. Usa Jetpack Compose para la interfaz de usuario.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var preferenceManager: com.example.pi_androidapp.core.util.PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by preferenceManager.isDarkTheme.collectAsState()
            
            PI_AndroidAppTheme(darkTheme = isDarkTheme) {
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
