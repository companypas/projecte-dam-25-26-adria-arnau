# -*- coding: utf-8 -*-
{
    'name': "pi_core",

    'summary': "Short (1 phrase/line) summary of the module's purpose",

    'description': """
Long description of module's purpose
    """,

    'author': "My Company",
    'website': "https://www.yourcompany.com",

    # Categories can be used to filter modules in modules listing
    # Check https://github.com/odoo/odoo/blob/15.0/odoo/addons/base/data/ir_module_category_data.xml
    # for the full list
    'category': 'Uncategorized',
    'version': '0.1',

    # any module necessary for this one to work correctly
    'depends': ['base','mail'],

    # always loaded
    'data': [
        'security/ir.model.access.csv',
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
    # only loaded in demonstration mode
    'demo': [
        'demo/demo.xml',
    ],
}

