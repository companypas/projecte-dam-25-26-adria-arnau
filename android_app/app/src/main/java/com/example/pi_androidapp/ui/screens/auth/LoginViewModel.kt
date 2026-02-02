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
 * ViewModel para la pantalla de Login. Gestiona el estado del formulario y el proceso de
 * autenticación.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    /** Actualiza el email del formulario. */
    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null)
    }

    /** Actualiza la contraseña del formulario. */
    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null)
    }

    /** Intenta iniciar sesión con las credenciales actuales. */
    fun login() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        // Validaciones básicas
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(emailError = "El email es requerido")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = "Email inválido")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(passwordError = "La contraseña es requerida")
            return
        }

        authRepository
                .login(email, password)
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

    /** Limpia el mensaje de error. */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/** Estado de la UI de Login. */
data class LoginUiState(
        val email: String = "",
        val password: String = "",
        val emailError: String? = null,
        val passwordError: String? = null,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null,
        val usuario: Usuario? = null
)
