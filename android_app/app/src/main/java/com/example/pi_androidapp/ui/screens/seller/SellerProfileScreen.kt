package com.example.pi_androidapp.ui.screens.seller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.ui.components.Base64Image
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator

/**
 * Pantalla de perfil público del vendedor. Muestra información, estadísticas y productos del
 * vendedor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
        viewModel: SellerProfileViewModel,
        onBackClick: () -> Unit,
        onProductClick: (Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("Perfil del Vendedor") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null ->
                    ErrorMessage(message = uiState.error ?: "Error", onRetry = null)
            uiState.usuario != null -> {
                val usuario = uiState.usuario!!

                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                                        .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    if (!usuario.imagen.isNullOrBlank()) {
                        Base64Image(
                                base64String = usuario.imagen,
                                contentDescription = "Foto de ${usuario.nombre}",
                                modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape),
                                contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                            text = usuario.nombre,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                    )

                    // Valoración con estrellas
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                                text = String.format("%.1f", usuario.valoracionPromedio),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                        )
                        Text(
                                text = " (${usuario.totalValoraciones} valoraciones)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Estadísticas del vendedor
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                                value = usuario.totalProductosVendidos.toString(),
                                label = "Vendidos",
                                icon = Icons.Default.ShoppingCart
                        )
                        StatCard(
                                value = usuario.totalProductosVenta.toString(),
                                label = "En venta",
                                icon = Icons.Default.ShoppingCart
                        )
                        StatCard(
                                value = "${usuario.antiguedad} meses",
                                label = "Miembro",
                                icon = Icons.Default.Person
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Información de contacto
                    Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    CardDefaults.cardColors(
                                            containerColor =
                                                    MaterialTheme.colorScheme.surfaceVariant
                                    )
                    ) {
                        Column {
                            if (!usuario.email.isNullOrBlank()) {
                                ListItem(
                                        headlineContent = { Text(usuario.email) },
                                        supportingContent = { Text("Email") },
                                        leadingContent = { Icon(Icons.Default.Email, null) }
                                )
                            }

                            if (!usuario.telefono.isNullOrBlank()) {
                                ListItem(
                                        headlineContent = { Text(usuario.telefono) },
                                        supportingContent = { Text("Teléfono") },
                                        leadingContent = { Icon(Icons.Default.Phone, null) }
                                )
                            }

                            if (!usuario.ubicacion.isNullOrBlank()) {
                                ListItem(
                                        headlineContent = { Text(usuario.ubicacion) },
                                        supportingContent = { Text("Ubicación") },
                                        leadingContent = { Icon(Icons.Default.LocationOn, null) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Productos del vendedor
                    Text(
                            text = "Productos en venta",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    if (uiState.isLoadingProducts) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else if (uiState.productos.isEmpty()) {
                        Text(
                                text = "Este vendedor no tiene productos en venta",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            items(uiState.productos) { producto ->
                                SellerProductCard(
                                        producto = producto,
                                        onClick = { onProductClick(producto.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun StatCard(
        value: String,
        label: String,
        icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
            modifier = Modifier.width(100.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
    ) {
        Column(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SellerProductCard(producto: Producto, onClick: () -> Unit) {
    Card(
            modifier = Modifier.width(140.dp).clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Imagen del producto con Coil
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .aspectRatio(1f)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
            ) {
                Base64Image(
                        base64String = producto.imagenPrincipal,
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        placeholderIconSize = 40
                )
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
                Text(
                        text = "${String.format("%.2f", producto.precio)}€",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
