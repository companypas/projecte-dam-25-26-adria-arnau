.. _api-auth:

=================================
API REST – Autenticación (Auth)
=================================

.. contents:: Contenido
   :local:
   :depth: 2

Base URL: ``http://<servidor>:8069``

Todos los endpoints de autenticación son **públicos** (no requieren token).

----

POST /api/v1/auth/registro
============================

Registra un nuevo usuario en el marketplace.

**Request body (JSON):**

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "nombre":    "Juan García",
       "email":     "juan@ejemplo.com",
       "password":  "miPasswordSegura",
       "telefono":  "600123456",
       "ubicacion": "Barcelona"
     }
   }

**Campos requeridos:** ``nombre``, ``email``, ``password``

**Respuesta 201 – Éxito:**

.. code-block:: json

   {
     "result": {
       "mensaje": "Usuario registrado exitosamente",
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "usuario": {
         "id": "USR-00000001-1234",
         "nombre": "Juan García",
         "email": "juan@ejemplo.com",
         "telefono": "600123456",
         "ubicacion": "Barcelona",
         "fecha_registro": "2025-12-09",
         "valoracion_promedio": 0.0,
         "total_valoraciones": 0
       }
     }
   }

**Respuesta 400 – Email ya registrado:**

.. code-block:: json

   { "result": { "error": "El email ya está registrado", "status": 400 } }

----

POST /api/v1/auth/login
=========================

Autentica un usuario existente y devuelve un token JWT.

**Request body (JSON):**

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "email":    "juan@ejemplo.com",
       "password": "miPasswordSegura"
     }
   }

**Respuesta 200 – Éxito:**

.. code-block:: json

   {
     "result": {
       "mensaje": "Login exitoso",
       "token":   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
       "usuario": { ... }
     }
   }

**Respuesta 401 – Credenciales inválidas:**

.. code-block:: json

   { "result": { "error": "Credenciales inválidas", "status": 401 } }

----

POST /api/v1/auth/refresh
==========================

Refresca un token JWT próximo a expirar.

**Request body (JSON):**

.. code-block:: json

   {
     "jsonrpc": "2.0",
     "method": "call",
     "params": {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     }
   }

**Respuesta 200 – Éxito:**

.. code-block:: json

   {
     "result": {
       "mensaje": "Token refrescado",
       "token":   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     }
   }

**Respuesta 401 – Token inválido o expirado:**

.. code-block:: json

   { "result": { "error": "Token inválido o expirado", "status": 401 } }

----

.. note::

   El token tiene una validez de **24 horas**. Pasado ese tiempo, el cliente
   debe llamar a ``/refresh`` con el token antiguo o volver a hacer ``/login``.

.. warning::

   Nunca incluyas credenciales reales (contraseñas, tokens) en el control de
   versiones ni en los logs de producción.
