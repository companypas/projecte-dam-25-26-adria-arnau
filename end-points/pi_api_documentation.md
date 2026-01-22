# Documentación API REST - Marketplace

## Base URL
```
http://localhost:8069/api
```

## Autenticación JWT

Todos los endpoints protegidos requieren un token JWT en el header:
```
Authorization: Bearer <token>
```

---

## 1. AUTENTICACIÓN

### 1.1 Registro de Usuario
**POST** `/auth/registro`

```json
{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123",
  "telefono": "+34612345678",
  "ubicacion": "Valencia, Spain"
}
```

**Respuesta (201):**
```json
{
  "mensaje": "Usuario registrado exitosamente",
  "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "usuario": {
    "id": 1,
    "id_usuario": "USR-001",
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "valoracion_promedio": 0,
    "total_valoraciones": 0
  }
}
```

### 1.2 Login
**POST** `/auth/login`

```json
{
  "email": "juan@example.com",
  "password": "password123"
}
```

**Respuesta (200):**
```json
{
  "mensaje": "Login exitoso",
  "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "usuario": { ... }
}
```

### 1.3 Refrescar Token
**POST** `/auth/refresh`

```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGc..."
}
```

---

## 2. PRODUCTOS

### 2.1 Listar Productos
**GET** `/productos`

**Parámetros Query:**
- `categoria_id` (int) - Filtrar por categoría
- `etiqueta_id` (int) - Filtrar por etiqueta
- `nombre` (string) - Buscar por nombre
- `precio_min` (float) - Precio mínimo
- `precio_max` (float) - Precio máximo
- `ubicacion` (string) - Ubicación
- `offset` (int, default: 0) - Paginación
- `limit` (int, default: 20) - Límite de resultados

**Ejemplo:**
```
GET /productos?categoria_id=1&precio_max=100&limit=10
```

**Respuesta (200):**
```json
{
  "total": 50,
  "offset": 0,
  "limit": 10,
  "productos": [
    {
      "id": 1,
      "id_producto": "PRD-001",
      "nombre": "iPhone 12",
      "descripcion": "iPhone 12 en buen estado",
      "precio": 500,
      "estado": "segunda_mano",
      "ubicacion": "Valencia",
      "estado_venta": "disponible",
      "categoria": {
        "id": 1,
        "nombre": "Electrónica"
      },
      "propietario": {
        "id": 1,
        "nombre": "Juan Pérez",
        "valoracion": 4.5
      },
      "etiquetas": [
        {"id": 1, "nombre": "Apple"}
      ],
      "total_comentarios": 2,
      "fecha_publicacion": "2024-01-15T10:30:00"
    }
  ]
}
```

### 2.2 Obtener Producto
**GET** `/productos/<producto_id>`

**Respuesta (200):** Devuelve un objeto producto

### 2.3 Crear Producto
**POST** `/productos`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "nombre": "iPhone 12",
  "descripcion": "iPhone 12 en buen estado",
  "precio": 500,
  "categoria_id": 1,
  "estado": "segunda_mano",
  "antiguedad": 12,
  "ubicacion": "Valencia",
  "etiquetas_ids": [1, 2]
}
```

**Respuesta (201):**
```json
{
  "mensaje": "Producto creado exitosamente",
  "producto": { ... }
}
```

### 2.4 Actualizar Producto
**PUT** `/productos/<producto_id>`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "nombre": "iPhone 12 Pro",
  "precio": 550
}
```

### 2.5 Eliminar Producto
**DELETE** `/productos/<producto_id>`

**Headers:** `Authorization: Bearer <token>`

---

## 3. COMENTARIOS

### 3.1 Obtener Comentarios de Producto
**GET** `/productos/<producto_id>/comentarios`

**Parámetros:** `offset`, `limit`

**Respuesta (200):**
```json
{
  "total": 5,
  "offset": 0,
  "limit": 20,
  "comentarios": [
    {
      "id": 1,
      "id_comentario": "COM-001",
      "texto": "Producto en muy buen estado",
      "fecha": "2024-01-15T14:20:00",
      "editado": false,
      "usuario": {
        "id": 2,
        "nombre": "María García"
      },
      "total_reportes": 0
    }
  ]
}
```

### 3.2 Crear Comentario
**POST** `/productos/<producto_id>/comentarios`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "texto": "Producto en muy buen estado"
}
```

### 3.3 Actualizar Comentario
**PUT** `/comentarios/<comentario_id>`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "texto": "Texto actualizado"
}
```

### 3.4 Eliminar Comentario
**DELETE** `/comentarios/<comentario_id>`

**Headers:** `Authorization: Bearer <token>`

---

## 4. CONVERSACIONES Y MENSAJES

### 4.1 Listar Conversaciones
**GET** `/conversaciones`

**Headers:** `Authorization: Bearer <token>`

**Parámetros:** `offset`, `limit`

**Respuesta (200):**
```json
{
  "total": 3,
  "conversaciones": [
    {
      "id": 1,
      "asunto": "Chat sobre: iPhone 12",
      "otro_usuario": {
        "id": 2,
        "nombre": "María García"
      },
      "estado": "abierta",
      "total_mensajes": 5,
      "ultimo_mensaje": "¿Cuál es tu mejor precio?",
      "fecha_ultimo_mensaje": "2024-01-15T15:30:00",
      "producto_id": 1
    }
  ]
}
```

### 4.2 Obtener Mensajes de Conversación
**GET** `/conversaciones/<conversacion_id>/mensajes`

**Headers:** `Authorization: Bearer <token>`

**Parámetros:** `offset`, `limit`

**Respuesta (200):**
```json
{
  "conversacion_id": 1,
  "total_mensajes": 5,
  "mensajes": [
    {
      "id": 1,
      "contenido": "¿Está disponible?",
      "fecha_envio": "2024-01-15T14:00:00",
      "leido": true,
      "remitente": {
        "id": 1,
        "nombre": "Juan Pérez"
      },
      "es_de_comprador": true,
      "es_de_vendedor": false
    }
  ]
}
```

### 4.3 Enviar Mensaje
**POST** `/conversaciones/<conversacion_id>/mensajes`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "contenido": "¿Cuál es tu mejor precio?"
}
```

**Respuesta (201):**
```json
{
  "mensaje": "Mensaje enviado exitosamente",
  "data": { ... }
}
```

### 4.4 Iniciar Chat sobre Producto
**POST** `/productos/<producto_id>/iniciar-chat`

**Headers:** `Authorization: Bearer <token>`

**Respuesta (201):**
```json
{
  "conversacion_id": 1,
  "asunto": "Chat sobre: iPhone 12",
  "mensaje": "Chat iniciado exitosamente"
}
```

---

## 5. COMPRAS

### 5.1 Crear Compra
**POST** `/compras`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "producto_id": 1
}
```

**Respuesta (201):**
```json
{
  "mensaje": "Compra creada exitosamente",
  "compra_id": 1,
  "estado": "pendiente"
}
```

### 5.2 Obtener Compra
**GET** `/compras/<compra_id>`

**Headers:** `Authorization: Bearer <token>`

**Respuesta (200):**
```json
{
  "id": 1,
  "id_compra": "CMP-001",
  "estado": "confirmada",
  "monto": 500,
  "fecha": "2024-01-15T10:00:00",
  "comprador": { ... },
  "vendedor": { ... },
  "producto": { ... }
}
```

### 5.3 Confirmar Compra
**POST** `/compras/<compra_id>/confirmar`

**Headers:** `Authorization: Bearer <token>`

---

## 6. VALORACIONES

### 6.1 Crear Valoración
**POST** `/valoraciones`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "usuario_valorado_id": 2,
  "compra_id": 1,
  "valoracion": "5",
  "comentario": "Excelente vendedor",
  "tipo_valoracion": "vendedor"
}
```

**Parámetros:**
- `usuario_valorado_id` (int) - ID del usuario a valorar
- `compra_id` (int) - ID de la compra relacionada
- `valoracion` (string) - Valor de 1 a 5
- `comentario` (string) - Comentario opcional
- `tipo_valoracion` (string) - "vendedor" o "comprador"

**Respuesta (201):**
```json
{
  "mensaje": "Valoración creada exitosamente",
  "valoracion_id": 1
}
```

---

## 7. REPORTES

### 7.1 Crear Reporte de Producto
**POST** `/reportes`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "tipo_reporte": "producto",
  "producto_id": 1,
  "motivo": "Producto de baja calidad"
}
```

### 7.2 Crear Reporte de Usuario
**POST** `/reportes`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "tipo_reporte": "usuario",
  "usuario_id": 2,
  "motivo": "Comportamiento fraudulento"
}
```

### 7.3 Crear Reporte de Comentario
**POST** `/reportes`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "tipo_reporte": "comentario",
  "comentario_id": 1,
  "motivo": "Comentario ofensivo"
}
```

**Respuesta (201):**
```json
{
  "mensaje": "Reporte creado exitosamente",
  "reporte_id": 1
}
```

---

## 8. USUARIOS

### 8.1 Obtener Perfil Público
**GET** `/usuarios/<usuario_id>`

**Respuesta (200):**
```json
{
  "id": 1,
  "id_usuario": "USR-001",
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "valoracion_promedio": 4.7,
  "total_valoraciones": 10,
  "total_productos_venta": 5,
  "total_productos_vendidos": 8
}
```

### 8.2 Obtener Perfil Actual
**GET** `/usuarios/perfil`

**Headers:** `Authorization: Bearer <token>`

### 8.3 Actualizar Perfil
**PUT** `/usuarios/perfil`

**Headers:** `Authorization: Bearer <token>`

```json
{
  "nombre": "Juan Pérez López",
  "telefono": "+34612345679",
  "ubicacion": "Madrid, Spain"
}
```

---

## 9. CATEGORÍAS

### 9.1 Listar Categorías
**GET** `/categorias`

**Respuesta (200):**
```json
{
  "total": 5,
  "categorias": [
    {
      "id": 1,
      "id_categoria": "CAT-001",
      "nombre": "Electrónica",
      "descripcion": "Productos electrónicos varios",
      "total_productos": 25,
      "imagen": "base64_encoded_image"
    }
  ]
}
```

---

## Códigos de Estado HTTP

| Código | Descripción |
|--------|-------------|
| 200 | OK - Solicitud exitosa |
| 201 | Created - Recurso creado |
| 400 | Bad Request - Parámetros inválidos |
| 401 | Unauthorized - Token ausente o inválido |
| 403 | Forbidden - Sin permisos |
| 404 | Not Found - Recurso no encontrado |
| 500 | Internal Server Error - Error del servidor |

---

## Manejo de Errores

Todas las respuestas de error tienen este formato:

```json
{
  "error": "Descripción del error"
}
```

---

## Notas de Implementación

1. **JWT Secret**: Cambiar `SECRET_KEY` en producción
2. **CORS**: Configurar según necesidades
3. **Rate Limiting**: Implementar para prevenir abuso
4. **Validación**: Todos los inputs se validan server-side
5. **Base64**: Las imágenes se codifican en base64