package com.example.pi_androidapp.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.repository.ProductosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de edición de producto. Carga los datos existentes del producto
 * y permite modificarlos y guardarlos.
 */
@HiltViewModel
class EditProductViewModel
@Inject
constructor(
        private val productosRepository: ProductosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState

    /** Carga los datos del producto existente desde la API. */
    fun loadProduct(productId: Int) {
        _uiState.value = _uiState.value.copy(productId = productId)

        productosRepository
                .obtenerProducto(productId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoadingProduct = true)
                        }
                        is Resource.Success -> {
                            val producto = result.data
                            if (producto != null) {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoadingProduct = false,
                                                nombre = producto.nombre,
                                                descripcion = producto.descripcion,
                                                precio = producto.precio.toString(),
                                                ubicacion = producto.ubicacion
                                        )
                            } else {
                                _uiState.value =
                                        _uiState.value.copy(
                                                isLoadingProduct = false,
                                                error = "Producto no encontrado"
                                        )
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoadingProduct = false,
                                            error = result.message
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre, nombreError = null)
    }

    fun onDescripcionChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion, descripcionError = null)
    }

    fun onPrecioChange(precio: String) {
        _uiState.value = _uiState.value.copy(precio = precio, precioError = null)
    }

    fun onUbicacionChange(ubicacion: String) {
        _uiState.value = _uiState.value.copy(ubicacion = ubicacion, ubicacionError = null)
    }

    /** Guarda los cambios del producto. */
    fun saveProduct() {
        // Validaciones
        if (_uiState.value.nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(nombreError = "El nombre es requerido")
            return
        }
        if (_uiState.value.descripcion.isBlank()) {
            _uiState.value = _uiState.value.copy(descripcionError = "La descripción es requerida")
            return
        }
        val precio = _uiState.value.precio.toDoubleOrNull()
        if (precio == null || precio <= 0) {
            _uiState.value = _uiState.value.copy(precioError = "El precio debe ser mayor a 0")
            return
        }
        if (_uiState.value.ubicacion.isBlank()) {
            _uiState.value = _uiState.value.copy(ubicacionError = "La ubicación es requerida")
            return
        }

        productosRepository
                .actualizarProducto(
                        productoId = _uiState.value.productId,
                        nombre = _uiState.value.nombre,
                        descripcion = _uiState.value.descripcion,
                        precio = precio,
                        ubicacion = _uiState.value.ubicacion
                )
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isSaving = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(isSaving = false, isSuccess = true)
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(isSaving = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/** Estado de la UI de edición de producto. */
data class EditProductUiState(
        val productId: Int = 0,
        val nombre: String = "",
        val descripcion: String = "",
        val precio: String = "",
        val ubicacion: String = "",
        val isLoadingProduct: Boolean = false,
        val isSaving: Boolean = false,
        val isSuccess: Boolean = false,
        val nombreError: String? = null,
        val descripcionError: String? = null,
        val precioError: String? = null,
        val ubicacionError: String? = null,
        val error: String? = null
)
