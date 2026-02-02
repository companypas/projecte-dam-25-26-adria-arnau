package com.example.pi_androidapp.ui.screens.purchases

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

/** ViewModel para la pantalla de compras. Gestiona la lista de compras del usuario. */
@HiltViewModel
class MyPurchasesViewModel @Inject constructor(private val comprasRepository: ComprasRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(MyPurchasesUiState())
    val uiState: StateFlow<MyPurchasesUiState> = _uiState

    init {
        loadCompras()
    }

    /** Carga las compras del usuario. */
    fun loadCompras() {
        comprasRepository
                .listarCompras()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            compras = result.data ?: emptyList(),
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

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadCompras()
    }
}

/** Estado de la UI de compras. */
data class MyPurchasesUiState(
        val compras: List<Compra> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null
)
