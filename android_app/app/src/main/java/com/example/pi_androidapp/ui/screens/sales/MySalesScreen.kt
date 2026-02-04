package com.example.pi_androidapp.ui.screens.sales

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.domain.model.Compra
import com.example.pi_androidapp.domain.model.EstadoCompra
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator

/** Pantalla de ventas del usuario. Lista todas las ventas y permite confirmarlas. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySalesScreen(viewModel: MySalesViewModel, onBackClick: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar errores de confirmación
    LaunchedEffect(uiState.confirmError) {
        uiState.confirmError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearConfirmError()
        }
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                        title = { Text("Mis Ventas") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        when {
            uiState.isLoading && uiState.ventas.isEmpty() -> {
                LoadingIndicator()
            }
            uiState.error != null && uiState.ventas.isEmpty() -> {
                ErrorMessage(message = uiState.error ?: "Error", onRetry = viewModel::loadVentas)
            }
            else -> {
                PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = viewModel::refresh,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                ) {
                    if (uiState.ventas.isEmpty()) {
                        Box(
                                modifier = Modifier.fillMaxSize().padding(32.dp),
                                contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                        Icons.Default.Sell,
                                        null,
                                        Modifier.padding(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                        "No tienes ventas todavía",
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
                            items(uiState.ventas, key = { it.id }) { venta ->
                                SaleCard(
                                    venta = venta,
                                    isConfirming = uiState.isConfirming,
                                    onConfirmClick = { viewModel.confirmarVenta(venta.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Tarjeta de venta individual con botón de confirmar. */
@Composable
private fun SaleCard(
    venta: Compra,
    isConfirming: Boolean,
    onConfirmClick: () -> Unit
) {
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
                        text = venta.producto?.nombre ?: "Producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                EstadoChip(estado = venta.estado)
            }

            Text(
                    text = String.format("%.2f €", venta.monto),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
            )

            venta.comprador?.let { comprador ->
                Text(
                        text = "Comprador: ${comprador.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            venta.fechaCreacion?.let { fecha ->
                Text(
                        text = "Fecha: $fecha",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón de confirmar solo para ventas pendientes
            if (venta.estado == EstadoCompra.PENDIENTE) {
                Spacer(Modifier.padding(top = 12.dp))
                Button(
                    onClick = onConfirmClick,
                    enabled = !isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, null, Modifier.padding(end = 8.dp))
                    Text(if (isConfirming) "Confirmando..." else "Confirmar Venta")
                }
            }
        }
    }
}

/** Chip de estado de la venta. */
@Composable
private fun EstadoChip(estado: EstadoCompra) {
    val (color, text) =
            when (estado) {
                EstadoCompra.PENDIENTE ->
                        MaterialTheme.colorScheme.secondaryContainer to "Pendiente"
                EstadoCompra.CONFIRMADA ->
                        MaterialTheme.colorScheme.primaryContainer to "Confirmada"
                EstadoCompra.CANCELADA -> MaterialTheme.colorScheme.errorContainer to "Cancelada"
                EstadoCompra.COMPLETADA -> MaterialTheme.colorScheme.tertiaryContainer to "Completada"
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
