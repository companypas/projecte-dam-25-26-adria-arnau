package com.example.pi_androidapp.ui.screens.product

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Categoria
import com.example.pi_androidapp.domain.repository.CategoriasRepository
import com.example.pi_androidapp.domain.repository.ProductosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de creación de producto. Gestiona el formulario y la subida del
 * producto.
 */
@HiltViewModel
class CreateProductViewModel
@Inject
constructor(
        private val productosRepository: ProductosRepository,
        private val categoriasRepository: CategoriasRepository,
        @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProductUiState())
    val uiState: StateFlow<CreateProductUiState> = _uiState

    init {
        loadCategorias()
    }

    private fun loadCategorias() {
        categoriasRepository
                .listarCategorias()
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(categorias = result.data ?: emptyList())
                        }
                        is Resource.Error -> {}
                        is Resource.Loading -> {}
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



    fun onEstadoChange(estado: String) {
        _uiState.value = _uiState.value.copy(estado = estado)
    }

    fun onCategoriaSelect(categoria: Categoria) {
        _uiState.value = _uiState.value.copy(selectedCategoria = categoria, categoriaError = null)
    }

    fun addImage(uri: Uri) {
        val currentImages = _uiState.value.imageUris.toMutableList()
        if (currentImages.size < 10) {
            currentImages.add(uri)
            _uiState.value = _uiState.value.copy(imageUris = currentImages)
        }
    }

    fun removeImage(uri: Uri) {
        val currentImages = _uiState.value.imageUris.toMutableList()
        currentImages.remove(uri)
        _uiState.value = _uiState.value.copy(imageUris = currentImages)
    }

    /** Convierte una URI de imagen a string base64. */
    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("CreateProductVM", "Error converting URI to base64: ${e.message}")
            null
        }
    }

    /** Crea el producto con los datos del formulario. */
    fun createProduct() {
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
        if (_uiState.value.selectedCategoria == null) {
            _uiState.value = _uiState.value.copy(categoriaError = "Selecciona una categoría")
            return
        }



        // Convertir imágenes a base64
        val imagenesBase64 = _uiState.value.imageUris.mapNotNull { uri -> uriToBase64(uri) }

        productosRepository
                .crearProducto(
                        nombre = _uiState.value.nombre,
                        descripcion = _uiState.value.descripcion,
                        precio = precio,
                        estado = _uiState.value.estado,
                        ubicacion = _uiState.value.ubicacion,

                        categoriaId = _uiState.value.selectedCategoria!!.id,
                        imagenes = imagenesBase64
                )
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, isSuccess = true)
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoading = false, error = result.message)
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/** Estado de la UI de creación de producto. */
data class CreateProductUiState(
        val nombre: String = "",
        val descripcion: String = "",
        val precio: String = "",
        val ubicacion: String = "",

        val estado: String = "nuevo",
        val selectedCategoria: Categoria? = null,
        val categorias: List<Categoria> = emptyList(),
        val imageUris: List<Uri> = emptyList(),
        val nombreError: String? = null,
        val descripcionError: String? = null,
        val precioError: String? = null,
        val ubicacionError: String? = null,
        val categoriaError: String? = null,
        val isLoading: Boolean = false,
        val isSuccess: Boolean = false,
        val error: String? = null
)
