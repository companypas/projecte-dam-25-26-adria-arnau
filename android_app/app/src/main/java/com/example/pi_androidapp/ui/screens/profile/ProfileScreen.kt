package com.example.pi_androidapp.ui.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.ui.components.LoadingIndicator

/** Pantalla de perfil del usuario. Muestra información y permite cerrar sesión. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onBackClick: () -> Unit, onLogout: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) { if (uiState.isLoggedOut) onLogout() }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Mi Perfil") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                        text = uiState.usuario?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Información del usuario
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                ) {
                    Column {
                        ListItem(
                                headlineContent = { Text(uiState.usuario?.nombre ?: "-") },
                                supportingContent = { Text("Nombre") },
                                leadingContent = { Icon(Icons.Default.Person, null) }
                        )
                        ListItem(
                                headlineContent = { Text(uiState.usuario?.email ?: "-") },
                                supportingContent = { Text("Email") },
                                leadingContent = { Icon(Icons.Default.Email, null) }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón de cerrar sesión
                OutlinedButton(onClick = viewModel::logout, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Cerrar Sesión")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
