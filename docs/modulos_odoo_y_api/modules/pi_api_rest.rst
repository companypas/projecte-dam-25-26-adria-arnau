.. _modulo-pi-api-rest:

=============================================
Módulo ``pi_api_rest`` – API REST Vendoo
=============================================

.. contents:: Contenido
   :local:
   :depth: 2

----

Descripción funcional
======================

``pi_api_rest`` expone todos los recursos del marketplace como una **API REST
JSON-RPC** protegida con **JWT** (*JSON Web Tokens*). La aplicación móvil
Android consume esta API para realizar todas sus operaciones.

**Qué resuelve:**

- Autenticación segura sin depender de la sesión web de Odoo.
- Endpoints CRUD para productos, usuarios, comentarios, valoraciones, compras,
  reportes, conversaciones, mensajes, categorías y etiquetas.
- Validación de tokens en cada petición protegida mediante un decorador
  ``@jwt_required`` reutilizable.
- Serialización de registros Odoo a JSON estandarizado.

----

Instalación y dependencias
============================

.. code-block:: python

   # __manifest__.py
   {
       'name': 'PI API REST',
       'version': '1.0.0',
       'category': 'API',
       'depends': ['base', 'web', 'mail', 'pi_core'],
       'external_dependencies': {
           'python': ['jwt'],   # pip install PyJWT
       },
       'installable': True,
       'application': False,
       'license': 'LGPL-3',
   }

**Dependencias Odoo:** ``base``, ``web``, ``mail``, **``pi_core``**

**Dependencias Python:** ``PyJWT`` (debe instalarse en el entorno Python de Odoo)

.. code-block:: bash

   pip install PyJWT

----

Autenticación JWT
==================

El módulo implementa autenticación sin estado mediante tokens JWT firmados
con una clave secreta (``HS256``).

**Flujo de autenticación:**

.. code-block:: text

   Cliente                         Servidor (Odoo)
   ──────                         ────────────────
   POST /api/v1/auth/login  ─────► Verifica email + hash SHA-256(password)
                            ◄───── { token: "eyJ..." }

   GET  /api/v1/productos/listar
   Authorization: Bearer eyJ…  ──► Decodifica JWT, inyecta usuario_actual
                                   Ejecuta lógica de negocio
                            ◄───── { productos: [...] }

**Archivos relevantes:**

- ``controllers/auth.py`` – Clase ``JWTAuth``: genera y verifica tokens.
- ``controllers/utils.py`` – Clase ``APIUtils``: helpers de respuesta JSON.
- ``controllers/auth_controller.py`` – Endpoints ``/registro``, ``/login``, ``/refresh``.

**Duración del token:** 24 horas (configurable en ``auth.py``).

----

Controladores (Controllers)
============================

+------------------------------------+----------------------------------------+
| Fichero                            | Responsabilidad                        |
+====================================+========================================+
| ``auth_controller.py``             | Registro, login y refresco de token    |
+------------------------------------+----------------------------------------+
| ``productos_controller.py``        | CRUD completo de productos             |
+------------------------------------+----------------------------------------+
| ``usuarios_controller.py``         | Perfil, actualización y bajas          |
+------------------------------------+----------------------------------------+
| ``comentarios_controller.py``      | Comentarios por producto               |
+------------------------------------+----------------------------------------+
| ``compras_controller.py``          | Compras y cambio de estado             |
+------------------------------------+----------------------------------------+
| ``valoraciones_controller.py``     | Valoraciones entre usuarios            |
+------------------------------------+----------------------------------------+
| ``reportes_controller.py``         | Reportes de contenido                  |
+------------------------------------+----------------------------------------+
| ``conversaciones_controller.py``   | Conversaciones y mensajes              |
+------------------------------------+----------------------------------------+
| ``categorias_controller.py``       | Listado de categorías                  |
+------------------------------------+----------------------------------------+
| ``etiquetas_controller.py``        | Listado y gestión de etiquetas         |
+------------------------------------+----------------------------------------+

----

Seguridad
==========

- Todos los endpoints protegidos requieren cabecera ``Authorization: Bearer <token>``.
- El decorador ``@jwt_required`` valida el token antes de ejecutar el controlador.
- Las operaciones de modificación comprueban que el ``usuario_actual`` sea el
  propietario del recurso (comprobación a nivel de aplicación, no de base de datos).
- La contraseña nunca se devuelve en ninguna respuesta JSON.

----

Modelos propios
================

``pi_api_rest`` no define nuevos modelos de negocio; reutiliza los de ``pi_core``.
Incluye un modelo mínimo de configuración (si aplica) en ``models/``.

----

Véase también
==============

- :ref:`api-auth` – Endpoints de autenticación detallados
- :ref:`api-productos` – Endpoints de productos
- :ref:`api-usuarios` – Endpoints de usuarios
- :ref:`api-otros` – Demás endpoints (comentarios, compras, valoraciones…)
