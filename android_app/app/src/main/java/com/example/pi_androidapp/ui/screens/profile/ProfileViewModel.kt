package com.example.pi_androidapp.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.AuthRepository
import com.example.pi_androidapp.domain.repository.ProductosRepository
import com.example.pi_androidapp.domain.repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de perfil. Gestiona la información completa del usuario,
 * sus productos y el logout.
 */
@HiltViewModel
class ProfileViewModel
@Inject
constructor(
        private val authRepository: AuthRepository,
        private val usuariosRepository: UsuariosRepository,
        private val productosRepository: ProductosRepository,
        private val encryptedPrefsManager: EncryptedPrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val odooId = encryptedPrefsManager.getOdooId()
        if (odooId <= 0) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = "No se encontró el usuario")
            return
        }

        // Cargar perfil completo desde la API
        usuariosRepository
                .obtenerUsuario(odooId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, usuario = result.data)
                            // Una vez tenemos el perfil, cargar los productos
                            loadMyProducts(odooId)
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun loadMyProducts(odooId: Int) {
        productosRepository
                .listarProductos(offset = 0, limit = 50)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoadingProducts = true)
                        }
                        is Resource.Success -> {
                            val myProducts = result.data?.filter { it.propietarioId == odooId } ?: emptyList()
                            _uiState.value = _uiState.value.copy(
                                    isLoadingProducts = false,
                                    productos = myProducts
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                    isLoadingProducts = false,
                                    productos = emptyList()
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
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
        val productos: List<Producto> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingProducts: Boolean = false,
        val isLoggedOut: Boolean = false,
        val error: String? = null
)
