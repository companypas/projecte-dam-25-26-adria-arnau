.. _api-productos:

=================================
API REST – Productos
=================================

.. contents:: Contenido
   :local:
   :depth: 2

Todos los endpoints de esta sección requieren cabecera:

.. code-block:: http

   Authorization: Bearer <token_jwt>

----

GET/POST /api/v1/productos/listar
===================================

Lista los productos disponibles con soporte de filtros y paginación.

.. note::

   Se usa ``POST`` en JSON-RPC; los filtros van en ``params``.

**Parámetros opcionales (en** ``params`` **o query string):**

+----------------+----------+---------------------------------------------+
| Parámetro      | Tipo     | Descripción                                 |
+================+==========+=============================================+
| ``categoria_id`` | int    | Filtrar por categoría                       |
+----------------+----------+---------------------------------------------+
| ``etiqueta_id``  | int    | Filtrar por etiqueta                        |
+----------------+----------+---------------------------------------------+
| ``nombre``       | string | Búsqueda parcial por nombre (ilike)         |
+----------------+----------+---------------------------------------------+
| ``precio_min``   | float  | Precio mínimo                               |
+----------------+----------+---------------------------------------------+
| ``precio_max``   | float  | Precio máximo                               |
+----------------+----------+---------------------------------------------+
| ``ubicacion``    | string | Búsqueda parcial de ubicación               |
+----------------+----------+---------------------------------------------+
| ``estado_venta`` | string | ``disponible`` (por defecto) o ``vendido``  |
+----------------+----------+---------------------------------------------+
| ``offset``       | int    | Desplazamiento para paginación (def. 0)     |
+----------------+----------+---------------------------------------------+
| ``limit``        | int    | Resultados por página (def. 20)             |
+----------------+----------+---------------------------------------------+

**Respuesta 200:**

.. code-block:: json

   {
     "result": {
       "total": 142,
       "offset": 0,
       "limit": 20,
       "productos": [
         {
           "id": 7,
           "id_producto": "PRD-00000007-5678",
           "nombre": "Bicicleta de montaña",
           "descripcion": "Muy buen estado, cambios Shimano.",
           "precio": 350.0,
           "estado": "segunda_mano",
           "estado_venta": "disponible",
           "ubicacion": "Girona",
           "categoria": { "id": 3, "nombre": "Deporte" },
           "propietario": { "id_usuario": "USR-00000002-1111", "nombre": "Ana López" },
           "imagen_principal": "<base64>"
         }
       ]
     }
   }

----

GET/POST /api/v1/productos/<id>
================================

Devuelve el detalle completo de un producto, incluyendo **todas sus imágenes**.

**URL:** ``/api/v1/productos/7``

**Respuesta 200:** objeto producto con campo ``imagenes`` (lista base64).

**Respuesta 404:**

.. code-block:: json

   { "result": { "error": "Producto no encontrado", "status": 404 } }

----

POST /api/v1/productos
========================

Crea un nuevo producto para el usuario autenticado.

**Request body:**

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "nombre":      "Silla gaming",
       "descripcion": "Barely used, RGB lights.",
       "precio":      120.0,
       "categoria_id": 5,
       "estado":      "segunda_mano",
       "ubicacion":   "Madrid",
       "etiquetas_ids": [1, 3],
       "imagenes":    ["<base64_img_1>", "<base64_img_2>"]
     }
   }

**Campos requeridos:** ``nombre``, ``descripcion``, ``precio``,
``categoria_id``, ``ubicacion``

**Respuesta 201:**

.. code-block:: json

   {
     "result": {
       "mensaje": "Producto creado exitosamente",
       "producto": { ... }
     }
   }

----

PUT /api/v1/productos/<id>
===========================

Actualiza un producto existente. Solo el propietario puede modificarlo.

**Campos actualizables:** ``nombre``, ``descripcion``, ``precio``,
``ubicacion``, ``etiquetas_ids``

**Respuesta 403 – Sin permisos:**

.. code-block:: json

   { "result": { "error": "No tienes permisos para actualizar este producto", "status": 403 } }

----

DELETE /api/v1/productos/<id>
==============================

Elimina un producto. Solo el propietario puede borrarlo.

**Respuesta 200:**

.. code-block:: json

   {
     "result": {
       "mensaje": "Producto \"Silla gaming\" eliminado exitosamente"
     }
   }
