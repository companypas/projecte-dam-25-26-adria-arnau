package com.example.pi_androidapp.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pi_androidapp.core.util.Resource
import com.example.pi_androidapp.domain.model.Conversacion
import com.example.pi_androidapp.domain.repository.ConversacionesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/** ViewModel para la pantalla de lista de conversaciones. */
@HiltViewModel
class ConversationsListViewModel
@Inject
constructor(private val conversacionesRepository: ConversacionesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsListUiState())
    val uiState: StateFlow<ConversationsListUiState> = _uiState

    init {
        loadConversations()
    }

    /** Carga las conversaciones del usuario. */
    fun loadConversations() {
        conversacionesRepository
                .listarConversaciones()
                .onEach { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                        }
                        is Resource.Success -> {
                            _uiState.value =
                                    _uiState.value.copy(
                                            isLoading = false,
                                            conversaciones = result.data ?: emptyList(),
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

    /** Refresca la lista de conversaciones. */
    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadConversations()
    }
}

/** Estado de la UI de lista de conversaciones. */
data class ConversationsListUiState(
        val conversaciones: List<Conversacion> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val error: String? = null
)
