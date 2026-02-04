package com.example.pi_androidapp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator
import com.example.pi_androidapp.ui.components.ProductCard

/**
 * Pantalla principal con lista de productos. Incluye búsqueda, pull-to-refresh y navegación a otras
 * secciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        viewModel: HomeViewModel,
        onProductClick: (Int) -> Unit,
        onCreateProductClick: () -> Unit,
        onProfileClick: () -> Unit,
        onMyPurchasesClick: () -> Unit,
        onMySalesClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(text = "PI Marketplace", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = onMySalesClick) {
                                Icon(Icons.Default.Sell, contentDescription = "Mis Ventas")
                            }
                            IconButton(onClick = onMyPurchasesClick) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Mis Compras")
                            }
                            IconButton(onClick = onProfileClick) {
                                Icon(Icons.Default.Person, contentDescription = "Perfil")
                            }
                        }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                        onClick = onCreateProductClick,
                        containerColor = MaterialTheme.colorScheme.primary
                ) { Icon(Icons.Default.Add, contentDescription = "Publicar producto") }
            }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Barra de búsqueda
            OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    placeholder = { Text("Buscar productos...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Contenido principal
            when {
                uiState.isLoading && uiState.productos.isEmpty() -> {
                    LoadingIndicator()
                }
                uiState.error != null && uiState.productos.isEmpty() -> {
                    ErrorMessage(
                            message = uiState.error ?: "Error desconocido",
                            onRetry = viewModel::loadProductos
                    )
                }
                else -> {
                    PullToRefreshBox(
                            isRefreshing = uiState.isRefreshing,
                            onRefresh = viewModel::refresh,
                            modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.productos.isEmpty()) {
                            Box(
                                    modifier = Modifier.fillMaxSize().padding(32.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                        text = "No hay productos disponibles",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    contentPadding = PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items = uiState.productos, key = { it.id }) { producto ->
                                    ProductCard(
                                            producto = producto,
                                            onClick = { onProductClick(producto.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
