from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ComentariosController(http.Controller):
    
    @http.route('/api/v1/comentarios', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_comentarios_usuario(self, **kwargs):
        """Lista todos los comentarios del usuario autenticado"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            comentarios = request.env['pi.comentario'].sudo().search(
                [('usuario_id', '=', usuario.id), ('activo', '=', True)],
                offset=offset,
                limit=limit,
                order='fecha desc'
            )
            total = request.env['pi.comentario'].sudo().search_count(
                [('usuario_id', '=', usuario.id), ('activo', '=', True)]
            )
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'comentarios': [APIUtils.comentario_to_dict(c) for c in comentarios]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/comentarios/<int:comentario_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_comentario_individual(self, comentario_id, **kwargs):
        """Obtiene un comentario específico por ID"""
        try:
            comentario = request.env['pi.comentario'].sudo().browse(comentario_id)
            
            if not comentario.exists():
                return APIUtils.error_response('Comentario no encontrado', 404)
            
            if not comentario.activo:
                return APIUtils.error_response('Comentario no disponible', 404)
            
            return APIUtils.json_response(APIUtils.comentario_to_dict(comentario))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos/<int:producto_id>/comentarios', type='json', auth='none', methods=['GET', 'POST'])
    @jwt_required
    def obtener_comentarios(self, producto_id, **kwargs):
        """Obtiene comentarios de un producto"""
        try:
            producto = request.env['pi.producto'].browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            comentarios = producto.comentarios_ids.sorted('fecha', reverse=True)[offset:offset+limit]
            total = len(producto.comentarios_ids)
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'comentarios': [APIUtils.comentario_to_dict(c) for c in comentarios]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos/<int:producto_id>/comentarios/crear', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_comentario(self, producto_id, **kwargs):
        """Crea un comentario en un producto"""
        import logging
        _logger = logging.getLogger(__name__)
        try:
            usuario_data = request.usuario_actual
            _logger.info(f"[crear_comentario] usuario_data={usuario_data}, producto_id={producto_id}, kwargs={kwargs}")

            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                _logger.warning(f"[crear_comentario] Usuario no encontrado: {usuario_data['id']}")
                return {'error': 'Usuario no encontrado', 'status': 404}

            producto = request.env['pi.producto'].sudo().browse(producto_id)
            if not producto.exists():
                return {'error': 'Producto no encontrado', 'status': 404}

            # Con type='json' en Odoo, los params del JSON-RPC llegan directamente en kwargs.
            # El texto puede venir directo en kwargs: {"texto": "..."} 
            # o anidado si el cliente envió params dentro de params: kwargs.get('params', {})
            texto = kwargs.get('texto') or (kwargs.get('params') or {}).get('texto')
            _logger.info(f"[crear_comentario] texto='{texto}'")

            if not texto or not texto.strip():
                return {'error': 'El texto del comentario es requerido', 'status': 400}

            comentario = request.env['pi.comentario'].sudo().create({
                'texto': texto.strip(),
                'producto_id': producto_id,
                'usuario_id': usuario.id,
            })
            _logger.info(f"[crear_comentario] Comentario creado id={comentario.id}")

            return {
                'mensaje': 'Comentario creado exitosamente',
                'comentario': APIUtils.comentario_to_dict(comentario)
            }

        except Exception as e:
            import traceback
            _logger.error(f"[crear_comentario] Error: {e}\n{traceback.format_exc()}")
            return {'error': str(e), 'status': 500}
