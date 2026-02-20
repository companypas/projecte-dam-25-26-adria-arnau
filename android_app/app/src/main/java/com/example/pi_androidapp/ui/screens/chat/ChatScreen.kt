package com.example.pi_androidapp.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.domain.model.Mensaje
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator

/** Pantalla de chat individual con mensajes y campo de envío. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
        viewModel: ChatViewModel,
        otroUsuarioNombre: String?,
        onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Scroll al último mensaje cuando cambian los mensajes
    LaunchedEffect(uiState.mensajes.size) {
        if (uiState.mensajes.isNotEmpty()) {
            listState.animateScrollToItem(uiState.mensajes.size - 1)
        }
    }

    // Mostrar error de envío
    LaunchedEffect(uiState.sendError) {
        uiState.sendError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSendError()
        }
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    text = otroUsuarioNombre ?: "Chat",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            },
            bottomBar = {
                // Barra de envío de mensajes
                Surface(
                        tonalElevation = 3.dp,
                        shadowElevation = 8.dp
                ) {
                    Row(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                            .windowInsetsPadding(WindowInsets.navigationBars)
                                            .padding(bottom = 8.dp)
                                            .imePadding(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Escribe un mensaje...") },
                                shape = RoundedCornerShape(24.dp),
                                colors =
                                        OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor =
                                                        MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor =
                                                        MaterialTheme.colorScheme.outline
                                        ),
                                maxLines = 4,
                                enabled = !uiState.isSending
                        )

                        Spacer(Modifier.width(8.dp))

                        // Botón de enviar
                        Surface(
                                shape = CircleShape,
                                color =
                                        if (messageText.isNotBlank() && !uiState.isSending)
                                                MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendMessage(messageText.trim())
                                            messageText = ""
                                        }
                                    },
                                    enabled = messageText.isNotBlank() && !uiState.isSending
                            ) {
                                if (uiState.isSending) {
                                    CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Icon(
                                            Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Enviar",
                                            tint =
                                                    if (messageText.isNotBlank())
                                                            MaterialTheme.colorScheme.onPrimary
                                                    else
                                                            MaterialTheme.colorScheme
                                                                    .onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null && uiState.mensajes.isEmpty() -> {
                ErrorMessage(
                        message = uiState.error ?: "Error",
                        onRetry = {
                            uiState.conversacionId?.let { viewModel.loadMessages(it) }
                        }
                )
            }
            else -> {
                val screenWidth = LocalConfiguration.current.screenWidthDp.dp

                if (uiState.mensajes.isEmpty() && !uiState.isLoadingMessages) {
                    Box(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .padding(paddingValues)
                                            .padding(32.dp),
                            contentAlignment = Alignment.Center
                    ) {
                        Text(
                                "No hay mensajes todavía. ¡Envía el primero!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.mensajes, key = { it.id }) { mensaje ->
                            MessageBubble(
                                    mensaje = mensaje,
                                    maxWidth = screenWidth * 0.75f,
                                    currentUserOdooId = uiState.currentUserOdooId
                            )
                        }

                        // Spacer al final para evitar que el último mensaje quede oculto
                        item { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }
        }
    }
}

/** Burbuja de mensaje individual. Alineada a la derecha si es del usuario actual. */
@Composable
private fun MessageBubble(mensaje: Mensaje, maxWidth: androidx.compose.ui.unit.Dp, currentUserOdooId: Int) {
    val isFromCurrentUser = mensaje.remitenteId == currentUserOdooId

    Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                    if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
                modifier = Modifier.widthIn(max = maxWidth),
                shape =
                        RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                        ),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        if (isFromCurrentUser)
                                                MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                        )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Mostrar nombre del remitente si no es el usuario actual
                if (!isFromCurrentUser) {
                    Text(
                            text = mensaje.remitenteNombre,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                }

                Text(
                        text = mensaje.contenido,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                                if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(2.dp))

                Text(
                        text = formatHora(mensaje.fechaEnvio),
                        style = MaterialTheme.typography.labelSmall,
                        color =
                                if (isFromCurrentUser)
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/** Formatea una fecha ISO a hora (HH:mm). */
private fun formatHora(fecha: String?): String {
    if (fecha == null) return ""
    return try {
        if (fecha.length >= 16) {
            fecha.substring(11, 16)
        } else {
            fecha
        }
    } catch (e: Exception) {
        ""
    }
}
