package com.example.pi_androidapp.ui.screens.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import com.example.pi_androidapp.domain.model.Comentario
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.repository.ComentariosRepository
import com.example.pi_androidapp.domain.repository.ComprasRepository
import com.example.pi_androidapp.domain.repository.ProductosRepository
import com.example.pi_androidapp.domain.repository.ReportesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * ViewModel para la pantalla de detalle de producto. Gestiona la carga del producto, la acción de
 * compra, los comentarios y los reportes.
 */
@HiltViewModel
class ProductDetailViewModel
@Inject
constructor(
        private val productosRepository: ProductosRepository,
        private val comprasRepository: ComprasRepository,
        private val comentariosRepository: ComentariosRepository,
        private val reportesRepository: ReportesRepository,
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

    // ─── Comentarios ──────────────────────────────────────────────────────────

    /** Carga los comentarios del producto. */
    fun cargarComentarios(productoId: Int) {
        comentariosRepository
            .obtenerComentarios(productoId)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(comentariosLoading = true, comentariosError = null)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            comentariosLoading = false,
                            comentarios = result.data ?: emptyList()
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            comentariosLoading = false,
                            comentariosError = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /** Envía un nuevo comentario en el producto. */
    fun enviarComentario(productoId: Int, texto: String) {
        if (texto.isBlank()) return
        comentariosRepository
            .crearComentario(productoId, texto)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(enviandoComentario = true)
                    }
                    is Resource.Success -> {
                        val nuevo = result.data ?: return@onEach
                        _uiState.value = _uiState.value.copy(
                            enviandoComentario = false,
                            comentarios = _uiState.value.comentarios + nuevo,
                            comentarioTexto = ""
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            enviandoComentario = false,
                            comentariosError = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onComentarioTextoChanged(texto: String) {
        _uiState.value = _uiState.value.copy(comentarioTexto = texto)
    }

    fun clearComentariosError() {
        _uiState.value = _uiState.value.copy(comentariosError = null)
    }

    // ─── Reportes ─────────────────────────────────────────────────────────────

    /** Muestra/oculta el diálogo de reporte de producto. */
    fun showReportarProductoDialog() {
        _uiState.value = _uiState.value.copy(showReportarProductoDialog = true, reporteMotivoInput = "")
    }

    fun hideReportarProductoDialog() {
        _uiState.value = _uiState.value.copy(showReportarProductoDialog = false)
    }

    /** Muestra el diálogo de reporte de un comentario específico. */
    fun showReportarComentarioDialog(comentarioId: Int) {
        _uiState.value = _uiState.value.copy(
            showReportarComentarioDialog = true,
            reporteComentarioId = comentarioId,
            reporteMotivoInput = ""
        )
    }

    fun hideReportarComentarioDialog() {
        _uiState.value = _uiState.value.copy(showReportarComentarioDialog = false, reporteComentarioId = null)
    }

    fun onReporteMotivoChanged(motivo: String) {
        _uiState.value = _uiState.value.copy(reporteMotivoInput = motivo)
    }

    /** Envía el reporte del producto. */
    fun enviarReporteProducto() {
        val productoId = _uiState.value.producto?.id ?: return
        val motivo = _uiState.value.reporteMotivoInput

        reportesRepository
            .reportarProducto(productoId, motivo)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(enviandoReporte = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            enviandoReporte = false,
                            showReportarProductoDialog = false,
                            reporteExitoMsg = "Producto reportado correctamente"
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            enviandoReporte = false,
                            reporteErrorMsg = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /** Envía el reporte del comentario. */
    fun enviarReporteComentario() {
        val comentarioId = _uiState.value.reporteComentarioId ?: return
        val motivo = _uiState.value.reporteMotivoInput

        reportesRepository
            .reportarComentario(comentarioId, motivo)
            .onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.value = _uiState.value.copy(enviandoReporte = true)
                    }
                    is Resource.Success -> {
                        _uiState.value = _uiState.value.copy(
                            enviandoReporte = false,
                            showReportarComentarioDialog = false,
                            reporteComentarioId = null,
                            reporteExitoMsg = "Comentario reportado correctamente"
                        )
                    }
                    is Resource.Error -> {
                        _uiState.value = _uiState.value.copy(
                            enviandoReporte = false,
                            reporteErrorMsg = result.message
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearReporteExitoMsg() {
        _uiState.value = _uiState.value.copy(reporteExitoMsg = null)
    }

    fun clearReporteErrorMsg() {
        _uiState.value = _uiState.value.copy(reporteErrorMsg = null)
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
        val purchaseError: String? = null,
        // Comentarios
        val comentarios: List<Comentario> = emptyList(),
        val comentariosLoading: Boolean = false,
        val comentariosError: String? = null,
        val comentarioTexto: String = "",
        val enviandoComentario: Boolean = false,
        // Reportes
        val showReportarProductoDialog: Boolean = false,
        val showReportarComentarioDialog: Boolean = false,
        val reporteComentarioId: Int? = null,
        val reporteMotivoInput: String = "",
        val enviandoReporte: Boolean = false,
        val reporteExitoMsg: String? = null,
        val reporteErrorMsg: String? = null
) {
    val isOwnProduct: Boolean
        get() = producto != null && currentUserOdooId > 0 && producto.propietarioId == currentUserOdooId
}
