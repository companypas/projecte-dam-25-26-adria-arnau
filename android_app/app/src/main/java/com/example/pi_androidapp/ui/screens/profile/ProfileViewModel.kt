package com.example.pi_androidapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** ViewModel para la pantalla de perfil. Gestiona la información del usuario y el logout. */
@HiltViewModel
class ProfileViewModel @Inject constructor(private val authRepository: AuthRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(isLoading = false, usuario = user)
        }
    }

    /** Cierra la sesión del usuario. */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = _uiState.value.copy(isLoggedOut = true)
        }
    }
}

/** Estado de la UI de perfil. */
data class ProfileUiState(
        val usuario: Usuario? = null,
        val isLoading: Boolean = false,
        val isLoggedOut: Boolean = false
)
