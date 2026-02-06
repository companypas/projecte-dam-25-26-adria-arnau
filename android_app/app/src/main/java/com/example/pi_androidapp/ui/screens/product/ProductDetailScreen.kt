package com.example.pi_androidapp.ui.screens.product

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator
import com.example.pi_androidapp.ui.components.SmallLoadingIndicator

/** Pantalla de detalle de producto. Muestra información completa y permite comprar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
        viewModel: ProductDetailViewModel,
        onBackClick: () -> Unit,
        onPurchaseSuccess: () -> Unit,
        onSellerClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.purchaseSuccess) { if (uiState.purchaseSuccess) onPurchaseSuccess() }

    LaunchedEffect(uiState.purchaseError) {
        uiState.purchaseError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPurchaseError()
        }
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                        title = { Text("Detalle del Producto") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver"
                                )
                            }
                        }
                )
            }
    ) { paddingValues ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null ->
                    ErrorMessage(message = uiState.error ?: "Error", onRetry = null)
            uiState.producto != null -> {
                val producto = uiState.producto!!

                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .padding(paddingValues)
                                        .verticalScroll(rememberScrollState())
                ) {
                    // Imagen del producto
                    Box(
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .aspectRatio(1f)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                    ) {
                        if (producto.imagenPrincipal != null) {
                            val bitmap =
                                    remember(producto.imagenPrincipal) {
                                        try {
                                            val imageBytes =
                                                    Base64.decode(
                                                            producto.imagenPrincipal,
                                                            Base64.DEFAULT
                                                    )
                                            BitmapFactory.decodeByteArray(
                                                    imageBytes,
                                                    0,
                                                    imageBytes.size
                                            )
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                            if (bitmap != null) {
                                Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = producto.nombre,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Información del producto
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                                text = producto.nombre,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                                text = String.format("%.2f €", producto.precio),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Ubicación y antigüedad
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(producto.ubicacion, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                    text = "Antigüedad: ${producto.antiguedadMeses} meses",
                                    style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Vendedor
                        if (producto.propietarioNombre.isNotEmpty() && producto.propietarioId > 0) {
                            Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onSellerClick(producto.propietarioId) },
                                    colors =
                                            CardDefaults.cardColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.surfaceVariant
                                            )
                            ) {
                                Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, null, Modifier.size(40.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                                "Vendedor",
                                                style = MaterialTheme.typography.labelSmall
                                        )
                                        Text(
                                                producto.propietarioNombre,
                                                fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Icon(
                                            Icons.Default.ChevronRight,
                                            contentDescription = "Ver perfil",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Descripción
                        Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                                text = producto.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de compra
                        if (producto.estadoVenta == "disponible") {
                            Button(
                                    onClick = viewModel::buyProduct,
                                    enabled = !uiState.isPurchasing,
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isPurchasing) {
                                    SmallLoadingIndicator()
                                } else {
                                    Text(
                                            "Comprar ahora",
                                            style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        } else {
                            Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors =
                                            CardDefaults.cardColors(
                                                    containerColor =
                                                            MaterialTheme.colorScheme.errorContainer
                                            )
                            ) {
                                Text(
                                        text = "Este producto ya ha sido vendido",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
