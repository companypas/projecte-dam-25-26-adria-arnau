# 📚 Documentación API REST - Marketplace PI

## Índice
1. [Información General](#información-general)
2. [Autenticación](#autenticación)
3. [Modelos de Datos](#modelos-de-datos)
4. [Endpoints por Controller](#endpoints-por-controller)
   - [Auth Controller](#controller-authcontroller)
   - [Usuarios Controller](#controller-usuarioscontroller)
   - [Productos Controller](#controller-productoscontroller)
   - [Categorías Controller](#controller-categoriascontroller)
   - [Etiquetas Controller](#controller-etiquetascontroller)
   - [Comentarios Controller](#controller-comentarioscontroller)
   - [Compras Controller](#controller-comprascontroller)
   - [Conversaciones Controller](#controller-conversacionescontroller)
   - [Valoraciones Controller](#controller-valoracionescontroller)
   - [Reportes Controller](#controller-reportescontroller)

---

## Información General

### Base URL
```
https://tu-servidor-odoo.com/api/v1
```

### Formato de Peticiones
- **Content-Type**: `application/json`
- **Método**: JSON-RPC (Odoo utiliza `type='json'`)

### Estructura de Petición JSON-RPC
```json
{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    // parámetros del endpoint
  }
}
```

### Estructura de Respuesta Exitosa
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    // datos de respuesta
  }
}
```

### Estructura de Respuesta de Error
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "error": "Mensaje de error",
    "status": 400
  }
}
```

### Códigos de Estado Comunes
| Código | Descripción |
|--------|-------------|
| 200 | OK - Petición exitosa |
| 201 | Created - Recurso creado exitosamente |
| 400 | Bad Request - Parámetros inválidos o faltantes |
| 401 | Unauthorized - Token no proporcionado o inválido |
| 403 | Forbidden - Sin permisos para acceder al recurso |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error del servidor |

---

## Autenticación

La API utiliza **JWT (JSON Web Tokens)** con formato **Bearer Token**.

### Header de Autenticación
```http
Authorization: Bearer <token>
```

### Características del Token
- **Algoritmo**: HS256
- **Expiración**: 24 horas
- **Renovación automática**: Cada respuesta incluye un `nuevo_token` para renovación

---

## Modelos de Datos

### Usuario
```json
{
  "id": 1,
  "id_usuario": "USR-00000001-1234",
  "nombre": "string",
  "email": "string",
  "telefono": "string",
  "ubicacion": "string",
  "fecha_registro": "2025-01-30",
  "antiguedad": 30,
  "valoracion_promedio": 4.5,
  "total_valoraciones": 10,
  "total_productos_venta": 5,
  "total_productos_vendidos": 3,
  "total_productos_comprados": 2
}
```

### Producto
```json
{
  "id": 1,
  "id_producto": "PRD-00000001-1234",
  "nombre": "string",
  "descripcion": "string",
  "precio": 99.99,
  "estado": "nuevo | segunda_mano",
  "antiguedad_meses": 0,
  "ubicacion": "string",
  "estado_venta": "disponible | vendido",
  "categoria": {
    "id": 1,
    "nombre": "string"
  },
  "propietario": {
    "id": 1,
    "nombre": "string",
    "valoracion": 4.5
  },
  "etiquetas": [
    {"id": 1, "nombre": "string"}
  ],
  "total_comentarios": 5,
  "total_imagenes": 3,
  "imagen_principal": "base64_string",
  "fecha_publicacion": "2025-01-30T10:00:00"
}
```

### Categoría
```json
{
  "id": 1,
  "id_categoria": "CAT-00001",
  "nombre": "string",
  "descripcion": "string",
  "total_productos": 10,
  "imagen": "base64_string"
}
```

### Etiqueta
```json
{
  "id": 1,
  "nombre": "string",
  "descripcion": "string",
  "color": "#6c757d",
  "total_productos": 5
}
```

### Comentario
```json
{
  "id": 1,
  "id_comentario": "COM-00001",
  "texto": "string",
  "fecha": "2025-01-30T10:00:00",
  "editado": false,
  "usuario": {
    "id": 1,
    "nombre": "string"
  },
  "total_reportes": 0
}
```

### Compra
```json
{
  "id": 1,
  "id_compra": "CMP-20250130100000-1234",
  "estado": "pendiente | procesando | confirmada | cancelada | valorada_comprador | valorada_vendedor",
  "monto": 99.99,
  "fecha": "2025-01-30T10:00:00",
  "comprador": {"id": 1, "nombre": "string"},
  "vendedor": {"id": 2, "nombre": "string"},
  "producto": {"id": 1, "nombre": "string", "precio": 99.99}
}
```

### Conversación
```json
{
  "id": 1,
  "asunto": "string",
  "otro_usuario": {"id": 1, "nombre": "string"},
  "estado": "abierta | cerrada | bloqueada",
  "total_mensajes": 5,
  "ultimo_mensaje": "string",
  "fecha_ultimo_mensaje": "2025-01-30T10:00:00",
  "producto_id": 1
}
```

### Mensaje
```json
{
  "id": 1,
  "contenido": "string",
  "fecha_envio": "2025-01-30T10:00:00",
  "leido": false,
  "remitente": {"id": 1, "nombre": "string"},
  "es_de_comprador": true,
  "es_de_vendedor": false
}
```

### Valoración
```json
{
  "id": 1,
  "valoracion": "1 | 2 | 3 | 4 | 5",
  "comentario": "string",
  "tipo_valoracion": "comprador | vendedor",
  "fecha": "2025-01-30T10:00:00",
  "valorado": {"id": 1, "nombre": "string"},
  "valorador": {"id": 2, "nombre": "string"},
  "compra_id": 1
}
```

### Reporte
```json
{
  "id": 1,
  "tipo_reporte": "producto | usuario | comentario",
  "motivo": "string",
  "estado": "pendiente | en_revision | resuelto | rechazado",
  "fecha": "2025-01-30T10:00:00",
  "producto_reportado": "string | null",
  "usuario_reportado": "string | null"
}
```

---

## Enumeraciones (Enums)

### Estado del Producto (`estado`)
| Valor | Descripción |
|-------|-------------|
| `nuevo` | Producto nuevo, sin uso previo |
| `segunda_mano` | Producto usado/de segunda mano |

### Estado de Venta (`estado_venta`)
| Valor | Descripción |
|-------|-------------|
| `disponible` | Producto disponible para compra |
| `vendido` | Producto ya vendido |

### Estado de Compra (`estado`)
| Valor | Descripción |
|-------|-------------|
| `pendiente` | Compra pendiente de procesar |
| `procesando` | Compra en proceso |
| `confirmada` | Compra confirmada por vendedor |
| `cancelada` | Compra cancelada |
| `valorada_comprador` | Valorada por el comprador |
| `valorada_vendedor` | Valorada por el vendedor |

### Estado de Conversación (`state`)
| Valor | Descripción |
|-------|-------------|
| `abierta` | Conversación activa |
| `cerrada` | Conversación cerrada |
| `bloqueada` | Conversación bloqueada |

### Tipo de Reporte (`tipo_reporte`)
| Valor | Descripción |
|-------|-------------|
| `producto` | Reporte de un producto |
| `usuario` | Reporte de un usuario |
| `comentario` | Reporte de un comentario |

### Estado de Reporte (`estado`)
| Valor | Descripción |
|-------|-------------|
| `pendiente` | Reporte pendiente de revisión |
| `en_revision` | Reporte en revisión |
| `resuelto` | Reporte resuelto |
| `rechazado` | Reporte rechazado |

### Tipo de Valoración (`tipo_valoracion`)
| Valor | Descripción |
|-------|-------------|
| `comprador` | Valoración hacia el comprador |
| `vendedor` | Valoración hacia el vendedor |

### Valoración (`valoracion`)
| Valor | Descripción |
|-------|-------------|
| `1` | 1 Estrella |
| `2` | 2 Estrellas |
| `3` | 3 Estrellas |
| `4` | 4 Estrellas |
| `5` | 5 Estrellas |

---

## Controller: AuthController

### POST /api/v1/auth/registro
**Descripción:** Registra un nuevo usuario en el sistema.

**Autenticación:** No requerida

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `nombre` | string | ✅ Sí | Nombre del usuario |
| `email` | string | ✅ Sí | Email único del usuario |
| `password` | string | ✅ Sí | Contraseña del usuario |
| `telefono` | string | ❌ No | Número de teléfono |
| `ubicacion` | string | ❌ No | Ubicación/dirección del usuario |

**Ejemplo de petición:**
```http
POST /api/v1/auth/registro
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "nombre": "Juan García",
    "email": "juan@example.com",
    "password": "miContraseñaSegura123",
    "telefono": "+34 612 345 678",
    "ubicacion": "Madrid, España"
  }
}
```

**Ejemplo de respuesta exitosa (201):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Usuario registrado exitosamente",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "id": 1,
      "id_usuario": "USR-00000001-1234",
      "nombre": "Juan García",
      "email": "juan@example.com",
      "telefono": "+34 612 345 678",
      "ubicacion": "Madrid, España",
      "fecha_registro": "2025-01-30",
      "antiguedad": 0,
      "valoracion_promedio": 0.0,
      "total_valoraciones": 0,
      "total_productos_venta": 0,
      "total_productos_vendidos": 0,
      "total_productos_comprados": 0
    }
  }
}
```

**Códigos de estado:**
- `201` - Usuario registrado exitosamente
- `400` - Parámetros faltantes o email ya registrado
- `500` - Error interno del servidor

---

### POST /api/v1/auth/login
**Descripción:** Autentica un usuario y devuelve un token JWT.

**Autenticación:** No requerida

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `email` | string | ✅ Sí | Email del usuario |
| `password` | string | ✅ Sí | Contraseña del usuario |

**Ejemplo de petición:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "email": "juan@example.com",
    "password": "miContraseñaSegura123"
  }
}
```

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Login exitoso",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "usuario": {
      "id": 1,
      "id_usuario": "USR-00000001-1234",
      "nombre": "Juan García",
      "email": "juan@example.com"
    }
  }
}
```

**Códigos de estado:**
- `200` - Login exitoso
- `400` - Email o contraseña faltantes
- `401` - Credenciales inválidas
- `500` - Error interno del servidor

---

### POST /api/v1/auth/refresh
**Descripción:** Refresca un token JWT existente.

**Autenticación:** No requerida (pero requiere token válido en body)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `token` | string | ✅ Sí | Token JWT actual |

**Ejemplo de petición:**
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Token refrescado",
    "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Códigos de estado:**
- `200` - Token refrescado exitosamente
- `400` - Token no proporcionado
- `401` - Token expirado o inválido
- `500` - Error interno del servidor

---

## Controller: UsuariosController

### GET /api/v1/usuarios
**Descripción:** Lista usuarios con paginación y filtros.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Obligatorio | Default | Descripción |
|-----------|------|-------------|---------|-------------|
| `offset` | integer | ❌ No | 0 | Número de registros a saltar |
| `limit` | integer | ❌ No | 20 | Número máximo de registros |
| `nombre` | string | ❌ No | - | Filtrar por nombre (búsqueda parcial) |
| `ubicacion` | string | ❌ No | - | Filtrar por ubicación (búsqueda parcial) |

**Ejemplo de petición:**
```http
POST /api/v1/usuarios?nombre=Juan&limit=10
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {}
}
```

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "total": 25,
    "offset": 0,
    "limit": 10,
    "usuarios": [
      {
        "id": 1,
        "id_usuario": "USR-00000001-1234",
        "nombre": "Juan García",
        "email": "juan@example.com",
        "telefono": "+34 612 345 678",
        "ubicacion": "Madrid",
        "valoracion_promedio": 4.5,
        "total_valoraciones": 10
      }
    ],
    "nuevo_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### GET /api/v1/usuarios/{usuario_id}
**Descripción:** Obtiene el perfil público de un usuario específico.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `usuario_id` | integer | ID del usuario |

**Ejemplo de petición:**
```http
POST /api/v1/usuarios/1
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {}
}
```

**Códigos de estado:**
- `200` - Usuario encontrado
- `401` - Token inválido
- `404` - Usuario no encontrado
- `500` - Error interno

---

### GET /api/v1/usuarios/{usuario_id}/productos
**Descripción:** Lista los productos en venta de un usuario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `usuario_id` | integer | ID del usuario |

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |

---

### GET /api/v1/usuarios/{usuario_id}/valoraciones
**Descripción:** Lista las valoraciones recibidas por un usuario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `usuario_id` | integer | ID del usuario |

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |

---

### GET /api/v1/usuarios/perfil
**Descripción:** Obtiene el perfil del usuario autenticado.

**Autenticación:** ✅ Requerida (Bearer Token)

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "id": 1,
    "id_usuario": "USR-00000001-1234",
    "nombre": "Juan García",
    "email": "juan@example.com",
    "telefono": "+34 612 345 678",
    "ubicacion": "Madrid, España",
    "fecha_registro": "2025-01-30",
    "antiguedad": 30,
    "valoracion_promedio": 4.5,
    "total_valoraciones": 10,
    "total_productos_venta": 5,
    "total_productos_vendidos": 3,
    "total_productos_comprados": 2,
    "nuevo_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### PUT /api/v1/usuarios/perfil
**Descripción:** Actualiza el perfil del usuario autenticado.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `nombre` | string | ❌ No | Nuevo nombre |
| `email` | string | ❌ No | Nuevo email |
| `telefono` | string | ❌ No | Nuevo teléfono |
| `ubicacion` | string | ❌ No | Nueva ubicación |

**Ejemplo de petición:**
```http
POST /api/v1/usuarios/perfil
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "nombre": "Juan García López",
    "telefono": "+34 699 888 777"
  }
}
```

---

### DELETE /api/v1/usuarios/perfil
**Descripción:** Desactiva la cuenta del usuario autenticado.

**Autenticación:** ✅ Requerida (Bearer Token)

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Cuenta desactivada exitosamente"
  }
}
```

---

## Controller: ProductosController

### GET /api/v1/productos
**Descripción:** Lista todos los productos disponibles con filtros.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `categoria_id` | integer | - | Filtrar por categoría |
| `etiqueta_id` | integer | - | Filtrar por etiqueta |
| `nombre` | string | - | Búsqueda por nombre |
| `precio_min` | float | - | Precio mínimo |
| `precio_max` | float | - | Precio máximo |
| `ubicacion` | string | - | Filtrar por ubicación |

**Ejemplo de petición:**
```http
POST /api/v1/productos?categoria_id=1&precio_max=100
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {}
}
```

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "total": 50,
    "offset": 0,
    "limit": 20,
    "productos": [
      {
        "id": 1,
        "id_producto": "PRD-00000001-1234",
        "nombre": "iPhone 13",
        "descripcion": "iPhone 13 en perfecto estado",
        "precio": 599.99,
        "estado": "segunda_mano",
        "antiguedad_meses": 12,
        "ubicacion": "Madrid",
        "estado_venta": "disponible",
        "categoria": {"id": 1, "nombre": "Electrónica"},
        "propietario": {"id": 1, "nombre": "Juan", "valoracion": 4.5},
        "etiquetas": [{"id": 1, "nombre": "Apple"}],
        "total_comentarios": 5,
        "total_imagenes": 3,
        "imagen_principal": "base64...",
        "fecha_publicacion": "2025-01-30T10:00:00"
      }
    ],
    "nuevo_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### GET /api/v1/productos/{producto_id}
**Descripción:** Obtiene los detalles de un producto específico.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

---

### POST /api/v1/productos
**Descripción:** Crea un nuevo producto.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `nombre` | string | ✅ Sí | Nombre del producto |
| `descripcion` | string | ✅ Sí | Descripción detallada |
| `precio` | float | ✅ Sí | Precio (mayor a 0) |
| `categoria_id` | integer | ✅ Sí | ID de la categoría |
| `ubicacion` | string | ✅ Sí | Ubicación del producto |
| `estado` | enum | ❌ No | `nuevo` o `segunda_mano` (default: `nuevo`) |
| `antiguedad` | integer | ❌ No | Antigüedad en meses (default: 0) |
| `etiquetas_ids` | array[int] | ❌ No | IDs de etiquetas (máx. 5) |

**Ejemplo de petición:**
```http
POST /api/v1/productos
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "nombre": "MacBook Pro 2023",
    "descripcion": "MacBook Pro M2 en perfecto estado",
    "precio": 1299.99,
    "categoria_id": 1,
    "ubicacion": "Barcelona",
    "estado": "segunda_mano",
    "antiguedad": 6,
    "etiquetas_ids": [1, 2, 3]
  }
}
```

**Ejemplo de respuesta exitosa (201):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Producto creado exitosamente",
    "producto": {
      "id": 10,
      "id_producto": "PRD-00000010-5678",
      "nombre": "MacBook Pro 2023",
      "precio": 1299.99
    },
    "nuevo_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
  }
}
```

---

### PUT /api/v1/productos/{producto_id}
**Descripción:** Actualiza un producto existente (solo el propietario).

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `nombre` | string | ❌ No | Nuevo nombre |
| `descripcion` | string | ❌ No | Nueva descripción |
| `precio` | float | ❌ No | Nuevo precio |
| `ubicacion` | string | ❌ No | Nueva ubicación |
| `etiquetas_ids` | array[int] | ❌ No | Nuevas etiquetas |

**Códigos de estado:**
- `200` - Producto actualizado
- `400` - No hay datos para actualizar
- `403` - Sin permisos (no es propietario)
- `404` - Producto no encontrado

---

### DELETE /api/v1/productos/{producto_id}
**Descripción:** Elimina un producto (solo el propietario).

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "mensaje": "Producto \"MacBook Pro 2023\" eliminado exitosamente"
  }
}
```

---

## Controller: CategoriasController

### GET /api/v1/categorias
**Descripción:** Lista todas las categorías con paginación.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `nombre` | string | - | Filtrar por nombre |

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "total": 10,
    "offset": 0,
    "limit": 20,
    "categorias": [
      {
        "id": 1,
        "id_categoria": "CAT-00001",
        "nombre": "Electrónica",
        "descripcion": "Dispositivos electrónicos",
        "total_productos": 25,
        "imagen": "base64..."
      }
    ]
  }
}
```

---

### GET /api/v1/categorias/{categoria_id}
**Descripción:** Obtiene una categoría específica por ID.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `categoria_id` | integer | ID de la categoría |

---

## Controller: EtiquetasController

### GET /api/v1/etiquetas
**Descripción:** Lista todas las etiquetas activas con paginación.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `nombre` | string | - | Filtrar por nombre |

**Ejemplo de respuesta exitosa (200):**
```json
{
  "jsonrpc": "2.0",
  "id": null,
  "result": {
    "total": 15,
    "offset": 0,
    "limit": 20,
    "etiquetas": [
      {
        "id": 1,
        "nombre": "Apple",
        "descripcion": "Productos Apple",
        "color": "#333333",
        "total_productos": 12
      }
    ]
  }
}
```

---

### GET /api/v1/etiquetas/{etiqueta_id}
**Descripción:** Obtiene una etiqueta específica por ID.

**Autenticación:** ✅ Requerida (Bearer Token)

---

## Controller: ComentariosController

### GET /api/v1/comentarios
**Descripción:** Lista todos los comentarios del usuario autenticado.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |

---

### GET /api/v1/comentarios/{comentario_id}
**Descripción:** Obtiene un comentario específico por ID.

**Autenticación:** ✅ Requerida (Bearer Token)

---

### GET /api/v1/productos/{producto_id}/comentarios
**Descripción:** Obtiene los comentarios de un producto específico.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |

---

### POST /api/v1/productos/{producto_id}/comentarios
**Descripción:** Crea un comentario en un producto.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `texto` | string | ✅ Sí | Texto del comentario |

**Ejemplo de petición:**
```http
POST /api/v1/productos/1/comentarios
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "texto": "¿Está disponible para envío?"
  }
}
```

---

## Controller: ComprasController

### GET /api/v1/compras
**Descripción:** Lista las compras del usuario (como comprador o vendedor).

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `tipo` | enum | - | `compras` o `ventas` |
| `estado` | enum | - | Filtrar por estado de compra |

---

### POST /api/v1/compras
**Descripción:** Crea una nueva compra.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `producto_id` | integer | ✅ Sí | ID del producto a comprar |

**Validaciones:**
- El producto debe estar disponible
- No puedes comprar tu propio producto

---

### GET /api/v1/compras/{compra_id}
**Descripción:** Obtiene los detalles de una compra.

**Autenticación:** ✅ Requerida (Bearer Token)

**Acceso:** Solo comprador o vendedor de la compra

---

### PUT /api/v1/compras/{compra_id}
**Descripción:** Actualiza notas de una compra.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `notas` | string | ❌ No | Notas adicionales |

---

### DELETE /api/v1/compras/{compra_id}
**Descripción:** Cancela una compra (solo si está pendiente).

**Autenticación:** ✅ Requerida (Bearer Token)

**Restricciones:**
- Solo el comprador puede cancelar
- Solo se pueden cancelar compras en estado `pendiente`

---

### POST /api/v1/compras/{compra_id}/confirmar
**Descripción:** Confirma una compra (solo vendedor).

**Autenticación:** ✅ Requerida (Bearer Token)

**Restricciones:**
- Solo el vendedor puede confirmar

---

## Controller: ConversacionesController

### GET /api/v1/conversaciones
**Descripción:** Lista las conversaciones del usuario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |

---

### GET /api/v1/conversaciones/{conversacion_id}
**Descripción:** Obtiene los detalles de una conversación.

**Autenticación:** ✅ Requerida (Bearer Token)

---

### PUT /api/v1/conversaciones/{conversacion_id}
**Descripción:** Actualiza el estado de una conversación.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `estado` | enum | ❌ No | `abierta`, `cerrada` o `bloqueada` |

---

### GET /api/v1/conversaciones/{conversacion_id}/mensajes
**Descripción:** Obtiene los mensajes de una conversación.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 50 | Máximo de registros |

**Nota:** Los mensajes se marcan automáticamente como leídos.

---

### POST /api/v1/conversaciones/{conversacion_id}/mensajes
**Descripción:** Envía un mensaje en una conversación.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `contenido` | string | ✅ Sí | Contenido del mensaje |

---

### POST /api/v1/productos/{producto_id}/iniciar-chat
**Descripción:** Inicia o recupera un chat sobre un producto.

**Autenticación:** ✅ Requerida (Bearer Token)

**Path Parameters:**
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `producto_id` | integer | ID del producto |

**Validaciones:**
- No puedes chatear contigo mismo

---

## Controller: ValoracionesController

### GET /api/v1/valoraciones
**Descripción:** Lista las valoraciones del usuario (dadas y recibidas).

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `tipo` | enum | - | `recibidas` o `dadas` |

---

### GET /api/v1/valoraciones/{valoracion_id}
**Descripción:** Obtiene una valoración específica.

**Autenticación:** ✅ Requerida (Bearer Token)

**Acceso:** Solo valorador o valorado

---

### POST /api/v1/valoraciones
**Descripción:** Crea una valoración de usuario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `usuario_valorado_id` | integer | ✅ Sí | ID del usuario a valorar |
| `compra_id` | integer | ✅ Sí | ID de la compra relacionada |
| `valoracion` | enum | ✅ Sí | `1`, `2`, `3`, `4` o `5` |
| `tipo_valoracion` | enum | ✅ Sí | `comprador` o `vendedor` |
| `comentario` | string | ❌ No | Comentario opcional |

**Validaciones:**
- La compra debe estar confirmada
- No puedes valorarte a ti mismo

---

## Controller: ReportesController

### GET /api/v1/reportes
**Descripción:** Lista los reportes realizados por el usuario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Query Parameters:**
| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `offset` | integer | 0 | Registros a saltar |
| `limit` | integer | 20 | Máximo de registros |
| `estado` | enum | - | Filtrar por estado |
| `tipo_reporte` | enum | - | Filtrar por tipo |

---

### GET /api/v1/reportes/{reporte_id}
**Descripción:** Obtiene un reporte específico.

**Autenticación:** ✅ Requerida (Bearer Token)

**Acceso:** Solo el reportador puede ver sus reportes

---

### POST /api/v1/reportes
**Descripción:** Crea un reporte de producto, usuario o comentario.

**Autenticación:** ✅ Requerida (Bearer Token)

**Body (JSON):**
| Campo | Tipo | Obligatorio | Descripción |
|-------|------|-------------|-------------|
| `tipo_reporte` | enum | ✅ Sí | `producto`, `usuario` o `comentario` |
| `motivo` | string | ✅ Sí | Motivo del reporte |
| `producto_id` | integer | Condicional | Requerido si `tipo_reporte` = `producto` |
| `usuario_id` | integer | Condicional | Requerido si `tipo_reporte` = `usuario` |
| `comentario_id` | integer | Condicional | Requerido si `tipo_reporte` = `comentario` |

**Validaciones:**
- No puedes reportarte a ti mismo

**Ejemplo de petición (reportar producto):**
```http
POST /api/v1/reportes
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "method": "call",
  "params": {
    "tipo_reporte": "producto",
    "motivo": "Producto falsificado",
    "producto_id": 5
  }
}
```

---

## Notas Adicionales

### Paginación
Todos los endpoints de listado soportan paginación mediante:
- `offset`: Número de registros a saltar
- `limit`: Número máximo de registros a devolver

La respuesta siempre incluye:
- `total`: Número total de registros disponibles
- `offset`: Offset aplicado
- `limit`: Límite aplicado

### Renovación de Token
Cada respuesta autenticada incluye un campo `nuevo_token` con un token renovado. Se recomienda usar este nuevo token para las siguientes peticiones.

### Búsquedas Parciales
Los filtros de texto (nombre, ubicación, etc.) utilizan búsqueda parcial case-insensitive (ILIKE).

---

*Documentación generada automáticamente a partir del código fuente del proyecto.*
*Última actualización: 30 de Enero de 2025*
