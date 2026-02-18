package com.example.pi_androidapp.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.domain.model.Conversacion
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator

/** Pantalla de lista de conversaciones del usuario. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
        viewModel: ConversationsListViewModel,
        onBackClick: () -> Unit,
        onConversationClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Mis Chats") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.conversaciones.isEmpty() -> {
                LoadingIndicator()
            }
            uiState.error != null && uiState.conversaciones.isEmpty() -> {
                ErrorMessage(
                        message = uiState.error ?: "Error",
                        onRetry = viewModel::loadConversations
                )
            }
            else -> {
                PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    if (uiState.conversaciones.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        null,
                                        Modifier.padding(16.dp).size(48.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        "No tienes conversaciones todavía",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                                contentPadding =
                                        androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.conversaciones, key = { it.id }) { conversacion ->
                                ConversationCard(
                                        conversacion = conversacion,
                                        onClick = { onConversationClick(conversacion.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de una conversación. */
@Composable
private fun ConversationCard(conversacion: Conversacion, onClick: () -> Unit) {
    Card(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del otro usuario
            Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = conversacion.otroUsuario.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                    conversacion.fechaUltimoMensaje?.let { fecha ->
                        Text(
                                text = formatFechaCorta(fecha),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                        text = conversacion.asunto,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )

                conversacion.ultimoMensaje?.let { ultimo ->
                    Text(
                            text = ultimo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Formatea una fecha ISO a formato corto (HH:mm o dd/MM). */
private fun formatFechaCorta(fecha: String): String {
    return try {
        // Formato ISO: 2026-02-18T19:17:23
        if (fecha.length >= 16) {
            fecha.substring(11, 16) // HH:mm
        } else {
            fecha
        }
    } catch (e: Exception) {
        fecha
    }
}
