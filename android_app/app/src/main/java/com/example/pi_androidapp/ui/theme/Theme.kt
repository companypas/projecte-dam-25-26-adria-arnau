package com.example.pi_androidapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
        darkColorScheme(
                primary = PrimaryBlue,
                onPrimary = Color.White,
                primaryContainer = PrimaryBlueDark,
                onPrimaryContainer = Color.White,
                secondary = SecondaryOrange,
                onSecondary = Color.Black,
                secondaryContainer = SecondaryOrangeDark,
                onSecondaryContainer = Color.White,
                tertiary = Pink80,
                background = BackgroundDark,
                onBackground = TextPrimaryDark,
                surface = SurfaceDark,
                onSurface = TextPrimaryDark,
                error = ErrorRed,
                onError = Color.White
        )

private val LightColorScheme =
        lightColorScheme(
                primary = PrimaryBlue,
                onPrimary = Color.White,
                primaryContainer = PrimaryBlueLight,
                onPrimaryContainer = PrimaryBlueDark,
                secondary = SecondaryOrange,
                onSecondary = Color.Black,
                secondaryContainer = SecondaryOrangeDark,
                onSecondaryContainer = Color.White,
                tertiary = Pink40,
                background = BackgroundLight,
                onBackground = TextPrimary,
                surface = SurfaceLight,
                onSurface = TextPrimary,
                error = ErrorRed,
                onError = Color.White
        )

/**
 * Tema principal de la aplicación PI_AndroidApp. Soporta tema claro, oscuro y colores dinámicos de
 * Android 12+.
 */
@Composable
fun PI_AndroidAppTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme =
            when {
                dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                }
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
