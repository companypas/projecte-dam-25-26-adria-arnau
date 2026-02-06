package com.example.pi_androidapp.ui.screens.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.model.Usuario
import com.example.pi_androidapp.domain.repository.ProductosRepository
import com.example.pi_androidapp.domain.repository.UsuariosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de perfil del vendedor. Gestiona la carga del perfil público y los
 * productos del vendedor.
 */
@HiltViewModel
class SellerProfileViewModel
@Inject
constructor(
        private val usuariosRepository: UsuariosRepository,
        private val productosRepository: ProductosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SellerProfileUiState())
    val uiState: StateFlow<SellerProfileUiState> = _uiState

    private var sellerId: Int = 0

    /** Carga el perfil público de un vendedor y sus productos. */
    fun loadSeller(sellerId: Int) {
        this.sellerId = sellerId
        loadSellerProfile()
        loadSellerProducts()
    }

    private fun loadSellerProfile() {
        usuariosRepository
                .obtenerUsuario(sellerId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, usuario = result.data)
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    private fun loadSellerProducts() {
        // Aquí no podemos filtrar por propietario directamente,
        // pero usamos la API con un filtro si está disponible
        productosRepository
                .listarProductos(offset = 0, limit = 20)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoadingProducts = true)
                        }
                        is Resource.Success -> {
                            // Filtrar productos del vendedor actual
                            val sellerProducts =
                                    result.data?.filter { it.propietarioId == sellerId }
                                            ?: emptyList()
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoadingProducts = false,
                                            productos = sellerProducts
                                    )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoadingProducts = false,
                                            productos = emptyList()
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }
}

/** Estado de la UI del perfil del vendedor. */
data class SellerProfileUiState(
        val usuario: Usuario? = null,
        val productos: List<Producto> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingProducts: Boolean = false,
        val error: String? = null
)
