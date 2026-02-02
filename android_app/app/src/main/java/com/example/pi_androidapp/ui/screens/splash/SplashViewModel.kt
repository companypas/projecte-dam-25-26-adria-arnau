package com.example.pi_androidapp.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Splash. Verifica si hay una sesión activa para decidir la
 * navegación.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(private val authRepository: AuthRepository) :
        ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    /** Verifica si el usuario tiene una sesión activa. */
    private fun checkLoginStatus() {
        viewModelScope.launch { _isLoggedIn.value = authRepository.isLoggedIn() }
    }
}
