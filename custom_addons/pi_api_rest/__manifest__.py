{
    'name': 'PI API REST',
    'version': '1.0.0',
    'category': 'API',
    'summary': 'API REST para marketplace de productos de segunda mano',
    'description': '''
        Módulo de API REST para la aplicación Wallapop-like.
        Proporciona endpoints para:
        - Autenticación JWT
        - Gestión de productos
        - Sistema de comentarios
        - Conversaciones y mensajes
        - Reportes
        - Compras y valoraciones
        - Gestión de usuarios
        - Categorías
    ''',
    'depends': ['base', 'web', 'mail', 'pi_core'],
    'data': [],
    'installable': True,
    'application': False,
    'author': 'Tu Nombre',
    'license': 'LGPL-3',
    'external_dependencies': {
        'python': ['jwt'],
    },
}