package com.example.pi_androidapp.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.repository.ProductosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** ViewModel para la pantalla Home. Gestiona la lista de productos y la búsqueda. */
@HiltViewModel
class HomeViewModel @Inject constructor(private val productosRepository: ProductosRepository) :
        ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    private var searchJob: Job? = null

    init {
        loadProductos()
    }

    /** Carga la lista de productos desde la API. */
    fun loadProductos() {
        productosRepository
                .listarProductos(
                        offset = 0,
                        limit = 50,
                        busqueda = _uiState.value.searchQuery.ifBlank { null }
                )
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            productos = result.data ?: emptyList(),
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

    /** Actualiza el término de búsqueda con debounce automático. */
    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        // Cancelar búsqueda anterior si existe
        searchJob?.cancel()
        
        // Iniciar nueva búsqueda con debounce de 500ms
        searchJob = viewModelScope.launch {
            delay(500L)
            loadProductos()
        }
    }

    /** Ejecuta la búsqueda actual. */
    fun search() {
        searchJob?.cancel()
        loadProductos()
    }

    /** Refresca la lista de productos. */
    fun refresh() {
        searchJob?.cancel()
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadProductos()
    }
}

/** Estado de la UI de Home. */
data class HomeUiState(
        val productos: List<Producto> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null,
        val searchQuery: String = ""
)

