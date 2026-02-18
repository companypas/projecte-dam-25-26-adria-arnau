package com.example.pi_androidapp.ui.components

import android.util.Base64
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Componente reutilizable que decodifica un string Base64 y lo renderiza
 * usando Coil para aprovechar caché, downsampling y gestión eficiente de memoria.
 *
 * La decodificación de Base64 a ByteArray se realiza una sola vez (memoizada con `remember`).
 * Coil recibe el ByteArray directamente, que es un formato nativo compatible.
 *
 * @param base64String String en Base64 de la imagen (sin prefijo data:image)
 * @param contentDescription Descripción de accesibilidad
 * @param modifier Modificador de layout
 * @param contentScale Escala del contenido (por defecto Crop)
 * @param placeholderIconSize Tamaño del icono placeholder en dp
 */
@Composable
fun Base64Image(
    base64String: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderIconSize: Int = 48
) {
    if (base64String.isNullOrBlank()) {
        // Sin imagen: mostrar placeholder
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                modifier = Modifier.size(placeholderIconSize.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    // Decodificar Base64 a ByteArray una sola vez
    val imageBytes = remember(base64String) {
        try {
            // Limpiar posible prefijo data URI (data:image/png;base64,...)
            val cleanBase64 = if (base64String.contains(",")) {
                base64String.substringAfter(",")
            } else {
                base64String
            }
            Base64.decode(cleanBase64, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    if (imageBytes != null) {
        val context = LocalContext.current
        // Coil acepta ByteArray como modelo nativo para ImageRequest
        val imageRequest = remember(imageBytes) {
            ImageRequest.Builder(context)
                .data(imageBytes)
                .crossfade(true)
                .memoryCacheKey(base64String.hashCode().toString())
                .build()
        }

        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        // Error al decodificar: mostrar icono de error
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.BrokenImage,
                contentDescription = "Error al cargar imagen",
                modifier = Modifier.size(placeholderIconSize.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
