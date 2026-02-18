package com.example.pi_androidapp.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.repository.ComprasRepository
import com.example.pi_androidapp.domain.repository.ProductosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de detalle de producto. Gestiona la carga del producto y la acción de
 * compra.
 */
@HiltViewModel
class ProductDetailViewModel
@Inject
constructor(
        private val productosRepository: ProductosRepository,
        private val comprasRepository: ComprasRepository,
        private val encryptedPrefsManager: EncryptedPrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState(
        currentUserOdooId = encryptedPrefsManager.getOdooId()
    ))
    val uiState: StateFlow<ProductDetailUiState> = _uiState

    /** Carga los detalles de un producto. */
    fun loadProduct(productId: Int) {
        productosRepository
                .obtenerProducto(productId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, producto = result.data)
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    /** Inicia el proceso de compra del producto. */
    fun buyProduct() {
        val producto = _uiState.value.producto ?: return

        comprasRepository
                .crearCompra(producto.id)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value =
                                    _uiState.value.copy(isPurchasing = true, purchaseError = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isPurchasing = false,
                                            purchaseSuccess = true
                                    )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isPurchasing = false,
                                            purchaseError = result.message
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearPurchaseError() {
        _uiState.value = _uiState.value.copy(purchaseError = null)
    }
}

/** Estado de la UI de detalle de producto. */
data class ProductDetailUiState(
        val producto: Producto? = null,
        val currentUserOdooId: Int = -1,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isPurchasing: Boolean = false,
        val purchaseSuccess: Boolean = false,
        val purchaseError: String? = null
) {
    val isOwnProduct: Boolean
        get() = producto != null && currentUserOdooId > 0 && producto.propietarioId == currentUserOdooId
}
