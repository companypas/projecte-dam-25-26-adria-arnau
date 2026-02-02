package com.example.pi_androidapp.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de preferencias encriptadas para almacenamiento seguro. Utiliza EncryptedSharedPreferences
 * de AndroidX Security para almacenar el token JWT y otros datos sensibles de forma segura.
 *
 * @property context Contexto de la aplicación
 */
@Singleton
class EncryptedPrefsManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val PREFS_FILE_NAME = "pi_secure_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_ODOO_ID = "odoo_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
    }

    /**
     * SharedPreferences encriptadas usando AES256. El MasterKey se almacena en el Android Keystore.
     */
    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey =
                MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()

        EncryptedSharedPreferences.create(
                context,
                PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Guarda el token de autenticación de forma encriptada.
     * @param token Token JWT a almacenar
     */
    fun saveAuthToken(token: String) {
        encryptedPrefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    /**
     * Obtiene el token de autenticación almacenado.
     * @return Token JWT o null si no existe
     */
    fun getAuthToken(): String? {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Verifica si hay un token de autenticación almacenado.
     * @return true si existe un token válido
     */
    fun hasAuthToken(): Boolean {
        return !getAuthToken().isNullOrBlank()
    }

    /**
     * Guarda los datos básicos del usuario.
     * @param odooId ID interno de Odoo (Int)
     * @param userId ID del usuario (formato USR-XXXXX)
     * @param email Email del usuario
     * @param name Nombre del usuario
     */
    fun saveUserData(odooId: Int, userId: String, email: String, name: String) {
        encryptedPrefs
                .edit()
                .putInt(KEY_ODOO_ID, odooId)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .apply()
    }

    /**
     * Obtiene el ID interno de Odoo del usuario actual.
     * @return ID de Odoo o -1 si no existe
     */
    fun getOdooId(): Int {
        return encryptedPrefs.getInt(KEY_ODOO_ID, -1)
    }

    /**
     * Obtiene el ID del usuario actual.
     * @return ID del usuario (formato USR-XXXXX) o null si no existe
     */
    fun getUserId(): String? {
        return encryptedPrefs.getString(KEY_USER_ID, null)
    }

    /**
     * Obtiene el email del usuario actual.
     * @return Email del usuario o null
     */
    fun getUserEmail(): String? {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Obtiene el nombre del usuario actual.
     * @return Nombre del usuario o null
     */
    fun getUserName(): String? {
        return encryptedPrefs.getString(KEY_USER_NAME, null)
    }

    /** Limpia todos los datos de sesión (logout). Elimina token y datos del usuario. */
    fun clearSession() {
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Verifica si hay una sesión activa válida.
     * @return true si hay token y usuario guardados
     */
    fun isSessionActive(): Boolean {
        return hasAuthToken() && getOdooId() != -1
    }
}
