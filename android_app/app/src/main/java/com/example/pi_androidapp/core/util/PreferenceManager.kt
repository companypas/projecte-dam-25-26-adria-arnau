package com.example.pi_androidapp.core.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestor de preferencias simples (no sensibles) de la aplicación.
 */
@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vendoo_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(fetchDarkThemePreference())
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
    }

    private fun fetchDarkThemePreference(): Boolean {
        // Por defecto, se podría usar la del sistema, pero aquí simplificamos a false (claro)
        return prefs.getBoolean(KEY_DARK_THEME, false)
    }

    fun setDarkTheme(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, enabled).apply()
        _isDarkTheme.value = enabled
    }

    fun isDarkThemeEnabled(): Boolean {
        return _isDarkTheme.value
    }
}
