package com.example.pi_androidapp.ui.screens.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.domain.model.Compra
import com.example.pi_androidapp.domain.model.EstadoCompra
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator

/** Pantalla de compras del usuario. Lista todas las compras realizadas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPurchasesScreen(viewModel: MyPurchasesViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Mis Compras") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.compras.isEmpty() -> {
                LoadingIndicator()
            }
            uiState.error != null && uiState.compras.isEmpty() -> {
                ErrorMessage(message = uiState.error ?: "Error", onRetry = viewModel::loadCompras)
            }
            else -> {
                PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    if (uiState.compras.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                        Icons.Default.ShoppingBag,
                                        null,
                                        Modifier.padding(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        "No tienes compras todavía",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                                contentPadding =
                                        androidx.compose.foundation.layout.PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.compras, key = { it.id }) { compra ->
                                PurchaseCard(compra = compra)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de compra individual. */
@Composable
private fun PurchaseCard(compra: Compra) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        text = compra.producto?.nombre ?: "Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                EstadoChip(estado = compra.estado)
            }

            Text(
                    text = String.format("%.2f €", compra.monto),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
            )

            compra.vendedor?.let { vendedor ->
                Text(
                        text = "Vendedor: ${vendedor.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            compra.fechaCreacion?.let { fecha ->
                Text(
                        text = "Fecha: $fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Chip de estado de la compra. */
@Composable
private fun EstadoChip(estado: EstadoCompra) {
    val (color, text) =
            when (estado) {
                EstadoCompra.PENDIENTE ->
                        MaterialTheme.colorScheme.secondaryContainer to "Pendiente"
                EstadoCompra.PROCESANDO ->
                        MaterialTheme.colorScheme.tertiaryContainer to "Procesando"
                EstadoCompra.CONFIRMADA ->
                        MaterialTheme.colorScheme.primaryContainer to "Confirmada"
                EstadoCompra.CANCELADA -> MaterialTheme.colorScheme.errorContainer to "Cancelada"
                EstadoCompra.RECHAZADA -> MaterialTheme.colorScheme.errorContainer to "Rechazada"
                EstadoCompra.COMPLETADA ->
                        MaterialTheme.colorScheme.tertiaryContainer to "Completada"
            }

    Card(
            colors = CardDefaults.cardColors(containerColor = color),
            shape = RoundedCornerShape(16.dp)
    ) {
        Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall
        )
    }
}
