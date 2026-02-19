package com.example.pi_androidapp.ui.screens.product

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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.ui.components.Base64Image
import com.example.pi_androidapp.ui.components.ErrorMessage
import com.example.pi_androidapp.ui.components.LoadingIndicator
import com.example.pi_androidapp.ui.components.SmallLoadingIndicator
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * Devuelve las coordenadas (latitud, longitud) asociadas a un nombre de ubicación. La búsqueda es
 * case-insensitive y se recorta el texto. Si la ubicación no se encuentra en el mapa, se devuelven
 * coordenadas del centro de España.
 */
private fun getCoordinatesForLocation(ubicacion: String): LatLng {
        val coordsMap =
                mapOf(
                        "carlet" to LatLng(39.226358, -0.521611),
                        "valencia" to LatLng(39.4699, -0.3763),
                        "madrid" to LatLng(40.4168, -3.7038),
                        "barcelona" to LatLng(41.3851, 2.1734),
                        "sevilla" to LatLng(37.3891, -5.9845),
                        "zaragoza" to LatLng(41.6488, -0.8891),
                        "malaga" to LatLng(36.7213, -4.4214),
                        "murcia" to LatLng(37.9922, -1.1307),
                        "palma" to LatLng(39.5696, 2.6502),
                        "bilbao" to LatLng(43.2630, -2.9350),
                        "alicante" to LatLng(38.3452, -0.4810),
                        "cordoba" to LatLng(37.8882, -4.7794),
                        "valladolid" to LatLng(41.6523, -4.7245),
                        "vigo" to LatLng(42.2406, -8.7207),
                        "gijon" to LatLng(43.5322, -5.6611),
                        "granada" to LatLng(37.1773, -3.5986),
                        "alzira" to LatLng(39.1510, -0.4366),
                        "xativa" to LatLng(38.9903, -0.5189),
                        "gandia" to LatLng(38.9667, -0.1833),
                        "sueca" to LatLng(39.2026, -0.3113),
                        "albacete" to LatLng(38.9942, -1.8585),
                        "castellon" to LatLng(39.9864, -0.0513),
                        "elche" to LatLng(38.2669, -0.6986),
                        "torrent" to LatLng(39.4372, -0.4656),
                        "sagunto" to LatLng(39.6833, -0.2667),
                        "algemesi" to LatLng(39.1898, -0.4355),
                        "carcaixent" to LatLng(39.1243, -0.4528),
                        "benifaio" to LatLng(39.2847, -0.4263),
                        "alginet" to LatLng(39.2633, -0.4744),
                        "guadassuar" to LatLng(39.1892, -0.5097),
                        "l'alcudia" to LatLng(39.1937, -0.5008),
                        "alcudia" to LatLng(39.1937, -0.5008),
                )

        val key = ubicacion.trim().lowercase()
        return coordsMap[key] ?: LatLng(40.0, -3.5) // Fallback: centro de España
}

/** Pantalla de detalle de producto. Muestra información completa y permite comprar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
        viewModel: ProductDetailViewModel,
        onBackClick: () -> Unit,
        onPurchaseSuccess: () -> Unit,
        onSellerClick: (Int) -> Unit,
        onChatClick: (Int) -> Unit
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
                                        // Imagen del producto con Coil
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxWidth()
                                                                .aspectRatio(1f)
                                                                .background(
                                                                        MaterialTheme.colorScheme
                                                                                .surfaceVariant
                                                                ),
                                                contentAlignment = Alignment.Center
                                        ) {
                                                Base64Image(
                                                        base64String = producto.imagenPrincipal,
                                                        contentDescription = producto.nombre,
                                                        modifier = Modifier.fillMaxSize(),
                                                        placeholderIconSize = 64
                                                )
                                        }

                                        // Información del producto
                                        Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                        text = producto.nombre,
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineSmall,
                                                        fontWeight = FontWeight.Bold
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                        text =
                                                                String.format(
                                                                        "%.2f €",
                                                                        producto.precio
                                                                ),
                                                        style =
                                                                MaterialTheme.typography
                                                                        .headlineMedium,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                )

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Ubicación y antigüedad
                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                Icons.Default.LocationOn,
                                                                null,
                                                                Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                                producto.ubicacion,
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium
                                                        )
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        Icon(
                                                                Icons.Default.Schedule,
                                                                null,
                                                                Modifier.size(20.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                                text =
                                                                        "Publicado hace ${producto.antiguedadMeses} meses",
                                                                style =
                                                                        MaterialTheme.typography
                                                                                .bodyMedium
                                                        )
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Vendedor
                                                if (producto.propietarioNombre.isNotEmpty() &&
                                                                producto.propietarioId > 0
                                                ) {
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                onClick = {
                                                                        onSellerClick(
                                                                                producto.propietarioId
                                                                        )
                                                                },
                                                                colors =
                                                                        CardDefaults.cardColors(
                                                                                containerColor =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .surfaceVariant
                                                                        )
                                                        ) {
                                                                Row(
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                                12.dp
                                                                                        )
                                                                                        .fillMaxWidth(),
                                                                        verticalAlignment =
                                                                                Alignment
                                                                                        .CenterVertically
                                                                ) {
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .Person,
                                                                                null,
                                                                                Modifier.size(40.dp)
                                                                        )
                                                                        Spacer(
                                                                                Modifier.width(
                                                                                        12.dp
                                                                                )
                                                                        )
                                                                        Column(
                                                                                modifier =
                                                                                        Modifier.weight(
                                                                                                1f
                                                                                        )
                                                                        ) {
                                                                                Text(
                                                                                        "Vendedor",
                                                                                        style =
                                                                                                MaterialTheme
                                                                                                        .typography
                                                                                                        .labelSmall
                                                                                )
                                                                                Text(
                                                                                        producto.propietarioNombre,
                                                                                        fontWeight =
                                                                                                FontWeight
                                                                                                        .Medium
                                                                                )
                                                                        }
                                                                        Icon(
                                                                                Icons.Default
                                                                                        .ChevronRight,
                                                                                contentDescription =
                                                                                        "Ver perfil",
                                                                                tint =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .onSurfaceVariant
                                                                        )
                                                                }
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                // Descripción
                                                Text(
                                                        text = "Descripción",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                        text = producto.descripcion,
                                                        style = MaterialTheme.typography.bodyMedium
                                                )

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // ── Google Maps ──
                                                Text(
                                                        text = "Ubicación",
                                                        style =
                                                                MaterialTheme.typography
                                                                        .titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                val position =
                                                        remember(producto.ubicacion) {
                                                                getCoordinatesForLocation(
                                                                        producto.ubicacion
                                                                )
                                                        }
                                                val cameraPositionState =
                                                        rememberCameraPositionState {
                                                                this.position =
                                                                        CameraPosition
                                                                                .fromLatLngZoom(
                                                                                        position,
                                                                                        14f
                                                                                )
                                                        }

                                                Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp),
                                                        elevation =
                                                                CardDefaults.cardElevation(
                                                                        defaultElevation = 4.dp
                                                                )
                                                ) {
                                                        GoogleMap(
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(220.dp)
                                                                                .clip(
                                                                                        RoundedCornerShape(
                                                                                                12.dp
                                                                                        )
                                                                                ),
                                                                cameraPositionState =
                                                                        cameraPositionState
                                                        ) {
                                                                Marker(
                                                                        state =
                                                                                MarkerState(
                                                                                        position =
                                                                                                position
                                                                                ),
                                                                        title = producto.nombre,
                                                                        snippet = producto.ubicacion
                                                                )
                                                        }
                                                }

                                                Spacer(modifier = Modifier.height(24.dp))

                                                // Botón de compra y chat (solo si NO es producto propio)
                                                if (!uiState.isOwnProduct) {
                                                    if (producto.estadoVenta == "disponible") {
                                                        Button(
                                                                onClick = viewModel::buyProduct,
                                                                enabled = !uiState.isPurchasing,
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(56.dp),
                                                                shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                                if (uiState.isPurchasing) {
                                                                        SmallLoadingIndicator()
                                                                } else {
                                                                        Text(
                                                                                "Comprar ahora",
                                                                                style =
                                                                                        MaterialTheme
                                                                                                .typography
                                                                                                .titleMedium
                                                                        )
                                                                }
                                                        }
                                                    } else {
                                                        Card(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                colors =
                                                                        CardDefaults.cardColors(
                                                                                containerColor =
                                                                                        MaterialTheme
                                                                                                .colorScheme
                                                                                                .errorContainer
                                                                        )
                                                        ) {
                                                                Text(
                                                                        text =
                                                                                "Este producto ya ha sido vendido",
                                                                        modifier =
                                                                                Modifier.padding(
                                                                                        16.dp
                                                                                ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodyMedium,
                                                                        color =
                                                                                MaterialTheme
                                                                                        .colorScheme
                                                                                        .onErrorContainer
                                                                )
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(32.dp))

                                                    // Botón de chatear con el vendedor
                                                    if (producto.propietarioId > 0) {
                                                        androidx.compose.material3.OutlinedButton(
                                                                onClick = {
                                                                        onChatClick(producto.id)
                                                                },
                                                                modifier =
                                                                        Modifier.fillMaxWidth()
                                                                                .height(48.dp),
                                                                shape = RoundedCornerShape(12.dp)
                                                        ) {
                                                                Icon(
                                                                        Icons.AutoMirrored.Filled
                                                                                .Chat,
                                                                        contentDescription = null,
                                                                        modifier =
                                                                                Modifier.size(20.dp)
                                                                )
                                                                Spacer(Modifier.width(8.dp))
                                                                Text(
                                                                        "Chatear con el vendedor",
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .titleSmall
                                                                )
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(32.dp))
                                        }
                                }
                        }
                }
        }
}
