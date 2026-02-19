package com.example.pi_androidapp.ui.screens.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pi_androidapp.ui.components.LoadingIndicator
import com.example.pi_androidapp.ui.components.SmallLoadingIndicator

/**
 * Pantalla para editar un producto existente. Muestra un formulario pre-rellenado con los datos
 * actuales del producto y permite guardar los cambios.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductScreen(
        viewModel: EditProductViewModel,
        onBackClick: () -> Unit,
        onProductUpdated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onProductUpdated() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                        title = { Text("Editar Producto") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        if (uiState.isLoadingProduct) {
            LoadingIndicator()
        } else {
            Column(
                    modifier =
                            Modifier.fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(horizontal = 16.dp)
                                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Nombre
                OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = viewModel::onNombreChange,
                        label = { Text("Nombre del producto *") },
                        isError = uiState.nombreError != null,
                        supportingText = uiState.nombreError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción
                OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = viewModel::onDescripcionChange,
                        label = { Text("Descripción *") },
                        isError = uiState.descripcionError != null,
                        supportingText = uiState.descripcionError?.let { { Text(it) } },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Precio
                OutlinedTextField(
                        value = uiState.precio,
                        onValueChange = viewModel::onPrecioChange,
                        label = { Text("Precio (€) *") },
                        isError = uiState.precioError != null,
                        supportingText = uiState.precioError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Ubicación
                OutlinedTextField(
                        value = uiState.ubicacion,
                        onValueChange = viewModel::onUbicacionChange,
                        label = { Text("Ubicación *") },
                        isError = uiState.ubicacionError != null,
                        supportingText = uiState.ubicacionError?.let { { Text(it) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón guardar
                Button(
                        onClick = viewModel::saveProduct,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (uiState.isSaving) {
                        SmallLoadingIndicator()
                    } else {
                        Text("Guardar Cambios", style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
