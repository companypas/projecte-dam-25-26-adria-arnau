package com.example.pi_androidapp.ui.screens.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Compra
import com.example.pi_androidapp.domain.repository.ComprasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** ViewModel para la pantalla de ventas. Gestiona la lista de ventas del usuario y confirmación. */
@HiltViewModel
class MySalesViewModel @Inject constructor(private val comprasRepository: ComprasRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(MySalesUiState())
    val uiState: StateFlow<MySalesUiState> = _uiState

    init {
        loadVentas()
    }

    /** Carga las ventas del usuario (donde es vendedor). */
    fun loadVentas() {
        comprasRepository
                .listarCompras(tipo = "ventas")
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            ventas = result.data ?: emptyList(),
                                            isRefreshing = false
                                    )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            error = result.message,
                                            isRefreshing = false
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    /** Confirma una venta. */
    fun confirmarVenta(compraId: Int) {
        comprasRepository
                .confirmarCompra(compraId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isConfirming = true)
                        }
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(isConfirming = false)
                            loadVentas() // Recargar lista tras confirmar
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isConfirming = false,
                                confirmError = result.message
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadVentas()
    }

    /** Rechaza una venta. */
    fun rechazarVenta(compraId: Int) {
        android.util.Log.d("MySalesVM", "rechazarVenta called for compraId: $compraId")
        comprasRepository
                .rechazarCompra(compraId)
                .onEach { result ->
                    android.util.Log.d("MySalesVM", "rechazarCompra result: $result")
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isRejecting = true)
                        }
                        is Resource.Success -> {
                            android.util.Log.d("MySalesVM", "Rechazar SUCCESS, reloading ventas")
                            _uiState.value = _uiState.value.copy(isRejecting = false)
                            loadVentas() // Recargar lista tras rechazar
                        }
                        is Resource.Error -> {
                            android.util.Log.e("MySalesVM", "Rechazar ERROR: ${result.message}")
                            _uiState.value = _uiState.value.copy(
                                isRejecting = false,
                                confirmError = result.message
                            )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearConfirmError() {
        _uiState.value = _uiState.value.copy(confirmError = null)
    }
}

/** Estado de la UI de ventas. */
data class MySalesUiState(
        val ventas: List<Compra> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isConfirming: Boolean = false,
        val isRejecting: Boolean = false,
        val error: String? = null,
        val confirmError: String? = null
)

