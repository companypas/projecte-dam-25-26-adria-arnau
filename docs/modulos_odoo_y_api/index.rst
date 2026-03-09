.. Vendoo – Índice principal de documentación técnica

==================================================
Vendoo – Documentación Técnica de Módulos Odoo
==================================================

.. image:: https://img.shields.io/badge/Odoo-16.0-blue
   :alt: Odoo 16.0

.. image:: https://img.shields.io/badge/Python-3.10+-green
   :alt: Python 3.10+

.. image:: https://img.shields.io/badge/Licencia-LGPL--3-lightgrey
   :alt: LGPL-3

Bienvenido a la documentación técnica de **Vendoo**, un *marketplace* de
productos de segunda mano construido sobre Odoo 16.

.. note::

   Esta documentación se ha generado con **Sphinx** a partir del código fuente
   de los módulos ``custom_addons/``. Los docstrings del código Python se
   incluyen automáticamente gracias a la extensión ``autodoc``.

----

Descripción general del proyecto
==================================

Vendoo es un marketplace que permite a los usuarios publicar, comprar y vender
productos de segunda mano. El backend está implementado como módulos
personalizados de Odoo y expone una **API REST con JWT** para la aplicación
móvil Android.

El sistema se compone de dos módulos principales:

+-------------------+---------------------------------------------------------+
| Módulo            | Descripción                                             |
+===================+=========================================================+
| ``pi_core``       | Modelos, vistas, seguridad y lógica de negocio          |
+-------------------+---------------------------------------------------------+
| ``pi_api_rest``   | API REST JSON-RPC con autenticación JWT                 |
+-------------------+---------------------------------------------------------+

----

Contenido de la documentación
================================

.. toctree::
   :maxdepth: 2
   :caption: Módulos Odoo

   modules/pi_core
   modules/pi_api_rest

.. toctree::
   :maxdepth: 2
   :caption: Referencia de la API REST

   api/endpoints_auth
   api/endpoints_productos
   api/endpoints_usuarios
   api/endpoints_otros

.. toctree::
   :maxdepth: 1
   :caption: Referencia de código (autodoc)

   api/autoapi_core
   api/autoapi_rest

----

Instalación rápida
===================

.. code-block:: bash

   # 1. Clonar el repositorio
   git clone <url-del-repo>
   cd projecte-dam-25-26-adria-arnau

   # 2. Levantar el entorno Docker
   docker-compose up -d

   # 3. Instalar módulos desde el backend de Odoo:
   #    Configuración → Activar modo developer → Aplicaciones
   #    → Actualizar lista → Instalar: pi_core, pi_api_rest

Generar esta documentación
============================

.. code-block:: bash

   # 1. Instalar dependencias de documentación
   pip install sphinx sphinx-rtd-theme myst-parser sphinx-autodoc-typehints

   # 2. Generar stubs de API automáticamente
   sphinx-apidoc -o docs/api/autoapi custom_addons

   # 3. Compilar el sitio HTML
   sphinx-build -b html docs docs/_build/html
   # o usando el Makefile:
   make -C docs html

   # 4. Abrir el resultado
   start docs/_build/html/index.html   # Windows
   open  docs/_build/html/index.html   # macOS

----

Índice y búsqueda
==================

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`
