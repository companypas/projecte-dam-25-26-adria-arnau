package com.example.pi_androidapp.ui.screens.product

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.pi_androidapp.ui.components.SmallLoadingIndicator

/**
 * Pantalla para crear un nuevo producto. Incluye formulario con imágenes y selección de categoría.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProductScreen(
        viewModel: CreateProductViewModel,
        onBackClick: () -> Unit,
        onProductCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var categoriaExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri
                ->
                uri?.let { viewModel.addImage(it) }
            }

    LaunchedEffect(uiState.isSuccess) { if (uiState.isSuccess) onProductCreated() }

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
                        title = { Text("Publicar Producto") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                            }
                        }
                )
            }
    ) { paddingValues ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(paddingValues)
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Imágenes
            Text("Imágenes", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.imageUris.forEach { uri ->
                    Box(modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                        AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                        )
                        IconButton(
                                onClick = { viewModel.removeImage(uri) },
                                modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                    Icons.Default.Close,
                                    "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (uiState.imageUris.size < 10) {
                    Box(
                            modifier =
                                    Modifier.size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.Add, "Añadir imagen") }
                }
            }

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

            Spacer(modifier = Modifier.height(12.dp))

            // Estado del producto
            Text("Estado del producto", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                        selected = uiState.estado == "nuevo",
                        onClick = { viewModel.onEstadoChange("nuevo") },
                        label = { Text("Nuevo") }
                )
                FilterChip(
                        selected = uiState.estado == "segunda_mano",
                        onClick = { viewModel.onEstadoChange("segunda_mano") },
                        label = { Text("Segunda mano") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Categoría
            ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = it }
            ) {
                OutlinedTextField(
                        value = uiState.selectedCategoria?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                        },
                        isError = uiState.categoriaError != null,
                        supportingText = uiState.categoriaError?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                ) {
                    uiState.categorias.forEach { categoria ->
                        DropdownMenuItem(
                                text = { Text(categoria.nombre) },
                                onClick = {
                                    viewModel.onCategoriaSelect(categoria)
                                    categoriaExpanded = false
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón crear
            Button(
                    onClick = viewModel::createProduct,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (uiState.isLoading) {
                    SmallLoadingIndicator()
                } else {
                    Text("Publicar Producto", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
