package com.example.pi_androidapp.data.mapper

import com.example.pi_androidapp.data.remote.dto.auth.UsuarioDto
import com.example.pi_androidapp.data.remote.dto.categorias.CategoriaItemDto
import com.example.pi_androidapp.data.remote.dto.compras.CompraDto
import com.example.pi_androidapp.data.remote.dto.conversaciones.ConversacionDto
import com.example.pi_androidapp.data.remote.dto.conversaciones.MensajeDto
import com.example.pi_androidapp.data.remote.dto.productos.ProductoDto
import com.example.pi_androidapp.domain.model.Categoria
import com.example.pi_androidapp.domain.model.Compra
import com.example.pi_androidapp.domain.model.CompradorVendedorInfo
import com.example.pi_androidapp.domain.model.Conversacion
import com.example.pi_androidapp.domain.model.EstadoCompra
import com.example.pi_androidapp.domain.model.Mensaje
import com.example.pi_androidapp.domain.model.OtroUsuarioInfo
import com.example.pi_androidapp.domain.model.Producto
import com.example.pi_androidapp.domain.model.ProductoCompraInfo
import com.example.pi_androidapp.domain.model.Usuario

/** Mappers para convertir DTOs de la API a modelos de dominio. */

/** Convierte UsuarioDto a Usuario del dominio. */
fun UsuarioDto.toDomain(): Usuario {
    return Usuario(
            id = id,
            idUsuario = idUsuario,
            nombre = nombre,
            email = email,
            telefono = telefono,
            ubicacion = ubicacion,
            fechaRegistro = fechaRegistro,
            activo = activo ?: true,
            antiguedad = antiguedad ?: 0,
            valoracionPromedio = valoracionPromedio ?: 0.0,
            totalValoraciones = totalValoraciones ?: 0,
            totalProductosVenta = totalProductosVenta ?: 0,
            totalProductosVendidos = totalProductosVendidos ?: 0,
            totalProductosComprados = totalProductosComprados ?: 0
    )
}

/** Convierte ProductoDto a Producto del dominio. */
fun ProductoDto.toDomain(): Producto {
    return Producto(
            id = id,
            idProducto = idProducto ?: "",
            nombre = nombre ?: "",
            descripcion = descripcion ?: "",
            precio = precio ?: 0.0,
            estado = estado ?: "nuevo",
            antiguedadMeses = antiguedadMeses ?: 0,
            ubicacion = ubicacion ?: "",
            estadoVenta = estadoVenta ?: "disponible",
            categoriaId = categoria?.id ?: 0,
            categoriaNombre = categoria?.nombre ?: "",
            propietarioId = propietario?.id ?: 0,
            propietarioNombre = propietario?.nombre ?: "",
            propietarioValoracion = propietario?.valoracion ?: 0.0,
            etiquetas = etiquetas?.map { it.nombre ?: "" } ?: emptyList(),
            totalComentarios = totalComentarios ?: 0,
            totalImagenes = totalImagenes ?: 0,
            imagenPrincipal = imagenPrincipal,
            fechaPublicacion = fechaPublicacion
    )
}

/** Convierte CategoriaItemDto a Categoria del dominio. */
fun CategoriaItemDto.toDomain(): Categoria {
    return Categoria(
            id = id,
            idCategoria = idCategoria ?: "",
            nombre = nombre ?: "",
            descripcion = descripcion ?: "",
            totalProductos = totalProductos ?: 0,
            imagen = imagen
    )
}

/** Convierte CompraDto a Compra del dominio. */
fun CompraDto.toDomain(): Compra {
    return Compra(
            id = id,
            idCompra = idCompra ?: "",
            estado = EstadoCompra.fromString(estado ?: "pendiente"),
            monto = monto ?: 0.0,
            fechaCreacion = fechaCreacion,
            fechaConfirmacion = fechaConfirmacion,
            comprador =
                    comprador?.let { CompradorVendedorInfo(id = it.id, nombre = it.nombre ?: "") },
            vendedor =
                    vendedor?.let { CompradorVendedorInfo(id = it.id, nombre = it.nombre ?: "") },
            producto =
                    producto?.let {
                        ProductoCompraInfo(
                                id = it.id,
                                nombre = it.nombre ?: "",
                                precio = it.precio ?: 0.0,
                                imagen = it.imagen
                        )
                    }
    )
}

/** Convierte ConversacionDto a Conversacion del dominio. */
fun ConversacionDto.toDomain(): Conversacion {
    return Conversacion(
            id = id,
            asunto = asunto ?: "",
            otroUsuario =
                    OtroUsuarioInfo(
                            id = otroUsuario?.id ?: 0,
                            nombre = otroUsuario?.nombre ?: ""
                    ),
            estado = estado ?: "abierta",
            totalMensajes = totalMensajes ?: 0,
            ultimoMensaje = ultimoMensaje,
            fechaUltimoMensaje = fechaUltimoMensaje,
            productoId = productoId
    )
}

/** Convierte MensajeDto a Mensaje del dominio. */
fun MensajeDto.toDomain(): Mensaje {
    return Mensaje(
            id = id,
            contenido = contenido ?: "",
            fechaEnvio = fechaEnvio,
            leido = leido ?: false,
            remitenteId = remitente?.id ?: 0,
            remitenteNombre = remitente?.nombre ?: "",
            esDeComprador = esDeComprador ?: false,
            esDeVendedor = esDeVendedor ?: false
    )
}

