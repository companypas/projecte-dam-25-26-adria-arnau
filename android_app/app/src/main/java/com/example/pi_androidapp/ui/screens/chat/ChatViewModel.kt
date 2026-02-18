package com.example.pi_androidapp.ui.screens.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.security.EncryptedPrefsManager
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Mensaje
import com.example.pi_androidapp.domain.repository.ConversacionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** ViewModel para la pantalla de chat individual. */
@HiltViewModel
class ChatViewModel
@Inject
constructor(
        private val conversacionesRepository: ConversacionesRepository,
        private val encryptedPrefsManager: EncryptedPrefsManager,
        savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(
        currentUserOdooId = encryptedPrefsManager.getOdooId()
    ))
    val uiState: StateFlow<ChatUiState> = _uiState

    init {
        // Si viene con conversacionId, lo carga directamente
        savedStateHandle.get<Int>("conversacionId")?.let { id ->
            if (id > 0) {
                _uiState.value = _uiState.value.copy(conversacionId = id)
                loadMessages(id)
            }
        }

        // Si viene con productoId (iniciar chat desde producto), primero lo inicia
        savedStateHandle.get<Int>("productoId")?.let { id ->
            if (id > 0) {
                startChat(id)
            }
        }
    }

    /** Inicia un chat sobre un producto. */
    fun startChat(productoId: Int) {
        conversacionesRepository
                .iniciarChat(productoId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            val conversacionId = result.data ?: 0
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            conversacionId = conversacionId
                                    )
                            if (conversacionId > 0) {
                                loadMessages(conversacionId)
                            }
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            error = result.message
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    /** Carga los mensajes de la conversación. */
    fun loadMessages(conversacionId: Int) {
        conversacionesRepository
                .obtenerMensajes(conversacionId)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value =
                                    _uiState.value.copy(isLoadingMessages = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoadingMessages = false,
                                            mensajes = result.data ?: emptyList()
                                    )
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoadingMessages = false,
                                            error = result.message
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    /** Envía un mensaje en la conversación actual. */
    fun sendMessage(contenido: String) {
        val conversacionId = _uiState.value.conversacionId ?: return
        if (contenido.isBlank()) return

        conversacionesRepository
                .enviarMensaje(conversacionId, contenido)
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isSending = true)
                        }
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(isSending = false)
                            // Recargar mensajes después de enviar
                            loadMessages(conversacionId)
                        }
                        is Resource.Error -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isSending = false,
                                            sendError = result.message
                                    )
                        }
                    }
                }
                .launchIn(viewModelScope)
    }

    /** Limpia el error de envío. */
    fun clearSendError() {
        _uiState.value = _uiState.value.copy(sendError = null)
    }
}

/** Estado de la UI de chat. */
data class ChatUiState(
        val conversacionId: Int? = null,
        val mensajes: List<Mensaje> = emptyList(),
        val currentUserOdooId: Int = -1,
        val isLoading: Boolean = false,
        val isLoadingMessages: Boolean = false,
        val isSending: Boolean = false,
        val error: String? = null,
        val sendError: String? = null
)
