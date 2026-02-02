package com.example.pi_androidapp.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de Registro. Gestiona el formulario de registro y la creación de
 * cuenta.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(private val authRepository: AuthRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre, nombreError = null)
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value =
                _uiState.value.copy(confirmPassword = confirmPassword, confirmPasswordError = null)
    }

    fun onTelefonoChange(telefono: String) {
        _uiState.value = _uiState.value.copy(telefono = telefono)
    }

    fun onUbicacionChange(ubicacion: String) {
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion)
    }

    /** Registra un nuevo usuario con los datos del formulario. */
    fun register() {
        val nombre = _uiState.value.nombre.trim()
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        val telefono = _uiState.value.telefono.trim().ifBlank { null }
        val ubicacion = _uiState.value.ubicacion.trim().ifBlank { null }

        // Validaciones
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(nombreError = "El nombre es requerido")
            return
        }
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "El email es requerido")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Email inválido")
            return
        }
        if (password.length < 6) {
            _uiState.value =
                    _uiState.value.copy(
                            passwordError = "La contraseña debe tener al menos 6 caracteres"
                    )
            return
        }
        if (password != confirmPassword) {
            _uiState.value =
                    _uiState.value.copy(confirmPasswordError = "Las contraseñas no coinciden")
            return
        }

        authRepository
                .registro(nombre, email, password, telefono, ubicacion)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            isSuccess = true,
                                            usuario = result.data
                                    )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/** Estado de la UI de Registro. */
data class RegisterUiState(
        val nombre: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val telefono: String = "",
        val ubicacion: String = "",
        val nombreError: String? = null,
        val emailError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
        val usuario: Usuario? = null
)
