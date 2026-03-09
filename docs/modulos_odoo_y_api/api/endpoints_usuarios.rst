.. _api-usuarios:

=================================
API REST – Usuarios
=================================

.. contents:: Contenido
   :local:
   :depth: 2

Todos los endpoints requieren cabecera ``Authorization: Bearer <token_jwt>``.

----

GET/POST /api/v1/usuarios/perfil
==================================

Devuelve el perfil del usuario autenticado.

**Respuesta 200:**

.. code-block:: json

   {
     "result": {
       "id_usuario": "USR-00000001-1234",
       "nombre":     "Juan García",
       "email":      "juan@ejemplo.com",
       "telefono":   "600123456",
       "ubicacion":  "Barcelona",
       "fecha_registro": "2025-12-09",
       "antiguedad": 90,
       "valoracion_promedio": 4.5,
       "total_valoraciones": 12,
       "total_productos_venta": 3,
       "total_productos_vendidos": 7,
       "total_productos_comprados": 5
     }
   }

----

GET/POST /api/v1/usuarios/<id_usuario>
========================================

Devuelve el perfil público de cualquier usuario (sin datos sensibles).

**Respuesta 404 – Usuario no encontrado:**

.. code-block:: json

   { "result": { "error": "Usuario no encontrado", "status": 404 } }

----

PUT /api/v1/usuarios/perfil
=============================

Actualiza los datos del usuario autenticado.

**Campos actualizables:** ``nombre``, ``telefono``, ``ubicacion``, ``password``

**Request body:**

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "nombre":    "Juan García Pérez",
       "telefono":  "611223344",
       "ubicacion": "Tarragona"
     }
   }

.. warning::

   Si se incluye el campo ``password``, se almacenará como hash SHA-256.
   Nunca envíes contraseñas en texto plano en logs ni capturas de pantalla.

----

GET/POST /api/v1/usuarios/<id_usuario>/productos
==================================================

Lista los productos publicados por un usuario específico.

**Respuesta 200:** lista de objetos producto (misma estructura que
``/api/v1/productos/listar``).
