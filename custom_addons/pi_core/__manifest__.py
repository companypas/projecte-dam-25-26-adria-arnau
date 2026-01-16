# -*- coding: utf-8 -*-
{
    'name': "pi_core",

    'summary': "Short (1 phrase/line) summary of the module's purpose",

    'description': """
Long description of module's purpose
    """,

    'author': "My Company",
    'website': "https://www.yourcompany.com",

    'category': 'Uncategorized',
    'version': '0.1',

    'depends': ['base','mail','web'],

    'data': [
        'data/ir_sequence.xml',
        'security/ir.model.access.csv',
        'reports/pi_reportes.xml',
        'views/pi_usuario_views.xml',
        'views/pi_categoria_views.xml',
        'views/pi_etiqueta_views.xml',
        'views/pi_producto_views.xml',
        'views/pi_comentario_views.xml',
        'views/pi_valoracion_views.xml',
        'views/pi_compra_views.xml',
        'views/pi_reporte_views.xml',
    ],
    'demo': [
        'demo/demo.xml',
    ],
}

