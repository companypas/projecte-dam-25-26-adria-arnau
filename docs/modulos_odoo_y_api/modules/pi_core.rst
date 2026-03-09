.. _modulo-pi-core:

====================================
Módulo ``pi_core`` – Núcleo Vendoo
====================================

.. contents:: Contenido
   :local:
   :depth: 2

----

Descripción funcional
======================

``pi_core`` es el módulo central de la plataforma Vendoo. Implementa todos los
**modelos de negocio**, las **vistas del backend de Odoo**, la **seguridad** de
acceso y la **lógica de dominio** del marketplace (productos, usuarios,
compras, comentarios, valoraciones, reportes y mensajería).

**Qué resuelve:**

- Gestión completa del ciclo de vida de un producto (publicación → venta).
- Sistema de usuarios del marketplace con herencia de ``res.partner``.
- Compras entre usuarios con estados y valoraciones cruzadas.
- Sistema de reportes con workflow de resolución (wizard incluido).
- Conversaciones y mensajería interna entre comprador y vendedor.
- Informes PDF de productos y compras.

----

Instalación y dependencias
============================

.. code-block:: python

   # __manifest__.py
   {
       'name': 'pi_core',
       'version': '0.1',
       'category': 'Uncategorized',
       'depends': ['base', 'mail'],
       'data': [
           'security/ir.model.access.csv',
           'data/ir_sequence.xml',
           'reports/report_producto.xml',
           'reports/report_compra.xml',
           'views/pi_usuario_views.xml',
           'views/pi_categoria_views.xml',
           'views/pi_etiqueta_views.xml',
           'views/pi_producto_views.xml',
           'views/pi_comentario_views.xml',
           'views/pi_valoracion_views.xml',
           'views/pi_compra_views.xml',
           'views/pi_reporte_views.xml',
           'views/pi_conversacion_views.xml',
           'views/pi_mensaje_views.xml',
       ],
   }

**Dependencias Odoo:** ``base``, ``mail``

**Dependencias Python externas:** ninguna (solo módulos de la librería estándar
como ``hashlib``, ``random``, ``time`` y ``dateutil`` incluida con Odoo).

----

Modelos principales
====================

pi.usuario
-----------

Representa un usuario registrado en el marketplace. Hereda de ``res.partner``
mediante **herencia por delegación** (``_inherits``), lo que permite reutilizar
campos como ``name``, ``email`` y ``phone`` directamente desde el partner de Odoo.

.. list-table:: Campos principales de ``pi.usuario``
   :header-rows: 1
   :widths: 20 15 65

   * - Campo
     - Tipo
     - Descripción
   * - ``id_usuario``
     - ``Char``
     - Identificador único (ej. ``USR-00012345-4321``). Generado por secuencia.
   * - ``partner_id``
     - ``Many2one(res.partner)``
     - Partner delegado (requerido).
   * - ``password``
     - ``Char``
     - Contraseña almacenada como hash SHA-256.
   * - ``fecha_registro``
     - ``Date``
     - Fecha de alta en el marketplace.
   * - ``antiguedad``
     - ``Integer``
     - Días desde el registro (campo computado).
   * - ``valoracion_promedio``
     - ``Float``
     - Media de valoraciones recibidas (computado).
   * - ``total_valoraciones``
     - ``Integer``
     - Número total de valoraciones recibidas (computado).
   * - ``total_productos_venta``
     - ``Integer``
     - Productos actualmente en venta (computado).
   * - ``total_productos_vendidos``
     - ``Integer``
     - Número de ventas realizadas (computado).
   * - ``total_productos_comprados``
     - ``Integer``
     - Número de compras realizadas (computado).

**Restricciones:**

- ``id_usuario`` debe ser único (restricción SQL).
- La contraseña siempre se hashea con SHA-256 en ``create`` y ``write``.

----

pi.producto
-----------

Artículo publicado en el marketplace. Soporta chatter de Odoo (``mail.thread``).

.. list-table:: Campos principales de ``pi.producto``
   :header-rows: 1
   :widths: 20 15 65

   * - Campo
     - Tipo
     - Descripción
   * - ``id_producto``
     - ``Char``
     - Identificador único (ej. ``PRD-00012345-4321``).
   * - ``nombre_producto``
     - ``Char``
     - Nombre del artículo (requerido, con tracking).
   * - ``descripcion``
     - ``Text``
     - Descripción detallada.
   * - ``precio``
     - ``Float``
     - Precio en euros (> 0, requerido, con tracking).
   * - ``estado``
     - ``Selection``
     - Condición física: ``nuevo`` | ``segunda_mano``.
   * - ``estado_venta``
     - ``Selection``
     - Estado de venta: ``disponible`` | ``vendido``.
   * - ``ubicacion``
     - ``Char``
     - Localización del artículo.
   * - ``fecha_publicacion``
     - ``Datetime``
     - Fecha/hora de creación (solo lectura).
   * - ``antiguedad_producto``
     - ``Integer``
     - Meses desde la publicación (computado).
   * - ``propietario_id``
     - ``Many2one(pi.usuario)``
     - Usuario vendedor (requerido).
   * - ``categoria_id``
     - ``Many2one(pi.categoria)``
     - Categoría del producto (requerido).
   * - ``etiquetas_ids``
     - ``Many2many(pi.etiqueta)``
     - Etiquetas (máximo 5).
   * - ``imagenes_ids``
     - ``One2many(pi.producto.imagen)``
     - Imágenes del producto (máximo 10).
   * - ``imagen_principal``
     - ``Binary``
     - Primera imagen ordenada por ``sequence`` (computado).
   * - ``comentarios_ids``
     - ``One2many(pi.comentario)``
     - Comentarios del producto.

**Validaciones:**

- Etiquetas: máximo 5 por producto.
- Imágenes: máximo 10 por producto.
- Precio: debe ser mayor que 0.

**Acciones disponibles:**

- ``action_agregar_comentario`` – Abre formulario de nuevo comentario.
- ``action_reportar_producto`` – Abre wizard de reporte.
- ``action_iniciar_chat`` – Crea o abre canal de chat con el vendedor.
- ``action_marcar_vendido`` / ``action_marcar_disponible`` – Cambia estado de venta.

----

pi.compra
----------

Registra la transacción de compra-venta entre dos usuarios.

.. list-table:: Campos principales de ``pi.compra``
   :header-rows: 1
   :widths: 20 15 65

   * - Campo
     - Tipo
     - Descripción
   * - ``id_compra``
     - ``Char``
     - Identificador único (ej. ``CMP-20251209143022-5678``).
   * - ``fecha``
     - ``Datetime``
     - Fecha/hora de la compra (solo lectura).
   * - ``monto``
     - ``Monetary``
     - Importe de la transacción.
   * - ``comprador_id``
     - ``Many2one(pi.usuario)``
     - Usuario que compra.
   * - ``vendedor_id``
     - ``Many2one(pi.usuario)``
     - Usuario que vende (auto-asignado del producto).
   * - ``producto_id``
     - ``Many2one(pi.producto)``
     - Producto adquirido.
   * - ``estado``
     - ``Selection``
     - ``pendiente`` → ``procesando`` → ``confirmada`` → ``valorada_*``.
   * - ``valoracion_comprador_id``
     - ``Many2one(pi.valoracion)``
     - Valoración emitida por el comprador.
   * - ``valoracion_vendedor_id``
     - ``Many2one(pi.valoracion)``
     - Valoración emitida por el vendedor.

**Flujo de estados:**

.. code-block:: text

   pendiente → procesando → confirmada → valorada_comprador
                                       → valorada_vendedor
                                       → valorada_ambos
                         → cancelada / rechazada

----

pi.comentario
--------------

Comentario que un usuario deja en un producto.

.. list-table:: Campos de ``pi.comentario``
   :header-rows: 1
   :widths: 20 15 65

   * - Campo
     - Tipo
     - Descripción
   * - ``id_comentario``
     - ``Char``
     - Identificador único (secuencia o UUID).
   * - ``texto``
     - ``Text``
     - Contenido del comentario.
   * - ``fecha``
     - ``Datetime``
     - Fecha de publicación.
   * - ``editado``
     - ``Boolean``
     - Indica si fue modificado tras la publicación.
   * - ``fecha_edicion``
     - ``Datetime``
     - Fecha de la última edición.
   * - ``activo``
     - ``Boolean``
     - Soft-delete (falso = oculto).
   * - ``producto_id``
     - ``Many2one(pi.producto)``
     - Producto comentado.
   * - ``usuario_id``
     - ``Many2one(pi.usuario)``
     - Autor del comentario.
   * - ``total_reportes``
     - ``Integer``
     - Número de reportes activos (computado).

----

pi.valoracion
--------------

Puntuación que un usuario otorga a otro al finalizar una compra.

Los campos clave son ``valoracion_numerica`` (1-5) y ``tipo_valoracion``
(``comprador`` | ``vendedor``). Cada valoración actualiza el campo computado
``valoracion_promedio`` del usuario valorado.

----

pi.reporte
-----------

Sistema de moderación de contenido.

.. list-table:: Campos de ``pi.reporte``
   :header-rows: 1
   :widths: 20 15 65

   * - Campo
     - Tipo
     - Descripción
   * - ``motivo``
     - ``Text``
     - Descripción del motivo del reporte.
   * - ``tipo_reporte``
     - ``Selection``
     - ``producto`` | ``usuario`` | ``comentario``.
   * - ``estado``
     - ``Selection``
     - ``pendiente`` → ``en_revision`` → ``resuelto`` | ``rechazado``.
   * - ``accion_tomada``
     - ``Selection``
     - Acción del moderador: ninguna, advertencia, eliminación, suspensión.
   * - ``reportado_por_id``
     - ``Many2one(pi.usuario)``
     - Usuario que realiza el reporte.

**Wizard asociado:** ``pi.reporte.resolver.wizard`` – Formulario modal para
que el administrador elija la acción y añada notas de resolución.

----

pi.conversacion y pi.mensaje
-------------------------------

Modelo de mensajería directa entre comprador y vendedor vinculada a un
producto. ``pi.conversacion`` agrupa los mensajes; ``pi.mensaje`` almacena
cada mensaje individual con su remitente y timestamp.

----

Otros modelos auxiliares
--------------------------

+------------------------+---------------------------------------------+
| Modelo                 | Propósito                                   |
+========================+=============================================+
| ``pi.categoria``       | Categorías de productos                     |
+------------------------+---------------------------------------------+
| ``pi.etiqueta``        | Etiquetas para clasificación adicional      |
+------------------------+---------------------------------------------+
| ``pi.producto.imagen`` | Imágenes adjuntas a productos               |
+------------------------+---------------------------------------------+

----

Seguridad / Roles
==================

El módulo utiliza únicamente el grupo estándar de Odoo ``base.group_user``
(usuario interno). Todos los modelos tienen permisos completos (CRUD) para
este grupo:

.. code-block:: text

   # security/ir.model.access.csv
   id,name,model_id:id,group_id:id,perm_read,perm_write,perm_create,perm_unlink

   access_pi_usuario,     pi.usuario,     model_pi_usuario,     base.group_user, 1,1,1,1
   access_pi_producto,    pi.producto,    model_pi_producto,    base.group_user, 1,1,1,1
   access_pi_compra,      pi.compra,      model_pi_compra,      base.group_user, 1,1,1,1
   access_pi_comentario,  pi.comentario,  model_pi_comentario,  base.group_user, 1,1,1,1
   access_pi_valoracion,  pi.valoracion,  model_pi_valoracion,  base.group_user, 1,1,1,1
   access_pi_reporte,     pi.reporte,     model_pi_reporte,     base.group_user, 1,1,1,1
   access_pi_conversacion,pi.conversacion,model_pi_conversacion,base.group_user, 1,1,1,1
   access_pi_mensaje,     pi.mensaje,     model_pi_mensaje,     base.group_user, 1,1,1,1

.. note::

   No se han definido grupos de seguridad personalizados ni reglas de dominio
   (``ir.rule``). El control de acceso fino se realiza a nivel de API REST
   mediante validación JWT.

----

Vistas / Menús
===============

El módulo registra vistas para cada modelo en formato lista, formulario y
kanban (según el modelo). Los menús principales son:

- **Vendoo / Usuarios** → lista y formulario de ``pi.usuario``
- **Vendoo / Productos** → lista, kanban y formulario de ``pi.producto``
- **Vendoo / Compras** → lista y formulario de ``pi.compra``
- **Vendoo / Reportes** → lista y formulario de ``pi.reporte``
- **Vendoo / Conversaciones** → lista y formulario de ``pi.conversacion``

**Informes PDF:**

- ``report_producto.xml`` – Ficha de producto imprimible.
- ``report_compra.xml``   – Recibo de compra imprimible.

----

Secuencias automáticas
========================

El fichero ``data/ir_sequence.xml`` define secuencias para generar IDs
únicos con prefijos fijos:

+---------------------+-----------------+
| Secuencia           | Formato         |
+=====================+=================+
| ``pi.usuario``      | ``USR-XXXXXXXX``|
+---------------------+-----------------+
| ``pi.producto``     | ``PRD-XXXXXXXX``|
+---------------------+-----------------+
| ``pi.compra``       | ``CMP-XXXXXXXX``|
+---------------------+-----------------+
| ``pi.comentario``   | ``COM-XXXXXXXX``|
+---------------------+-----------------+

Si la secuencia no está disponible, se genera un ID de emergencia con
``timestamp + random`` garantizando unicidad por un máximo de 10 intentos.
