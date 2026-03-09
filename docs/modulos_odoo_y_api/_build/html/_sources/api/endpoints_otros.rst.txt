.. _api-otros:

=======================================================
API REST – Comentarios, Compras, Valoraciones y más
=======================================================

.. contents:: Contenido
   :local:
   :depth: 2

Todos los endpoints requieren cabecera ``Authorization: Bearer <token_jwt>``.

----

Comentarios
============

POST /api/v1/comentarios
--------------------------

Crea un comentario en un producto.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "producto_id": 7,
       "texto": "Excelente estado, muy buen vendedor."
     }
   }

GET/POST /api/v1/comentarios/<producto_id>
-------------------------------------------

Lista todos los comentarios de un producto.

PUT /api/v1/comentarios/<id>
-----------------------------

Edita un comentario (solo el autor). Actualiza ``editado=True`` y ``fecha_edicion``.

DELETE /api/v1/comentarios/<id>
--------------------------------

Soft-delete de un comentario (``activo=False``).

----

Compras
========

POST /api/v1/compras
---------------------

Inicia una compra sobre un producto disponible.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "producto_id": 7
     }
   }

El servidor asigna automáticamente ``monto`` y ``vendedor_id`` desde el producto.

**Flujo de estados:**

.. code-block:: text

   POST /compras              → crea con estado pendiente
   PUT  /compras/<id>/confirmar → estado confirmada + producto vendido
   PUT  /compras/<id>/cancelar  → estado cancelada

GET/POST /api/v1/compras/mis-compras
--------------------------------------

Lista las compras realizadas por el usuario autenticado.

GET/POST /api/v1/compras/mis-ventas
------------------------------------

Lista las ventas realizadas por el usuario autenticado como vendedor.

----

Valoraciones
=============

POST /api/v1/valoraciones
--------------------------

Valora al otro participante de una compra confirmada.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "compra_id":    12,
       "valoracion":   5,
       "comentario":   "Trato excelente, muy rápido.",
       "tipo_valoracion": "vendedor"
     }
   }

``tipo_valoracion``: ``vendedor`` | ``comprador``

----

Reportes
=========

POST /api/v1/reportes
----------------------

Crea un reporte de contenido.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "tipo_reporte":        "producto",
       "producto_reportado_id": 7,
       "motivo":              "Producto fraudulento o mal descrito."
     }
   }

``tipo_reporte``: ``producto`` | ``usuario`` | ``comentario``

----

Conversaciones y Mensajes
==========================

POST /api/v1/conversaciones
-----------------------------

Crea o reanuda una conversación sobre un producto.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "producto_id": 7
     }
   }

GET/POST /api/v1/conversaciones/mis-conversaciones
---------------------------------------------------

Lista todas las conversaciones del usuario autenticado.

POST /api/v1/mensajes
----------------------

Envía un mensaje dentro de una conversación.

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "conversacion_id": 3,
       "contenido":       "¡Hola! ¿Sigue disponible?"
     }
   }

GET/POST /api/v1/mensajes/<conversacion_id>
--------------------------------------------

Lista los mensajes de una conversación (el usuario debe ser participante).

----

Categorías y Etiquetas
========================

GET/POST /api/v1/categorias
-----------------------------

Lista todas las categorías disponibles.

**Respuesta 200:**

.. code-block:: json

   {
     "result": {
       "categorias": [
         { "id": 1, "nombre": "Electrónica" },
         { "id": 2, "nombre": "Ropa" },
         { "id": 3, "nombre": "Deporte" }
       ]
     }
   }

GET/POST /api/v1/etiquetas
----------------------------

Lista todas las etiquetas disponibles.

.. code-block:: json

   {
     "result": {
       "etiquetas": [
         { "id": 1, "nombre": "Urgente" },
         { "id": 2, "nombre": "Negociable" }
       ]
     }
   }

----

Códigos de error comunes
=========================

+--------+---------------------------------------------+
| Código | Descripción                                 |
+========+=============================================+
| 400    | Parámetros requeridos faltantes o inválidos |
+--------+---------------------------------------------+
| 401    | Token ausente, inválido o expirado          |
+--------+---------------------------------------------+
| 403    | Sin permisos sobre el recurso               |
+--------+---------------------------------------------+
| 404    | Recurso no encontrado                       |
+--------+---------------------------------------------+
| 500    | Error interno del servidor                  |
+--------+---------------------------------------------+
