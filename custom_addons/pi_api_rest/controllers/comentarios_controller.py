from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ComentariosController(http.Controller):
    
    @http.route('/api/comentarios', type='json', auth='none', methods=['GET'])
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
    
    @http.route('/api/comentarios/<int:comentario_id>', type='json', auth='public', methods=['GET'])
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
    
    @http.route('/api/productos/<int:producto_id>/comentarios', type='json', auth='public', methods=['GET'])
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
    
    @http.route('/api/productos/<int:producto_id>/comentarios', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_comentario(self, producto_id, **kwargs):
        """Crea un comentario en un producto"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            texto = data.get('texto')
            if not texto:
                return APIUtils.error_response('El texto del comentario es requerido', 400)
            
            comentario = request.env['pi.comentario'].sudo().create({
                'texto': texto,
                'producto_id': producto_id,
                'usuario_id': usuario.id,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Comentario creado exitosamente',
                'comentario': APIUtils.comentario_to_dict(comentario)
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/comentarios/<int:comentario_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_comentario(self, comentario_id, **kwargs):
        """Actualiza un comentario"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            comentario = request.env['pi.comentario'].sudo().browse(comentario_id)
            
            if not comentario.exists():
                return APIUtils.error_response('Comentario no encontrado', 404)
            
            if comentario.usuario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para actualizar este comentario', 403)
            
            texto = kwargs.get('texto')
            if not texto:
                return APIUtils.error_response('El texto del comentario es requerido', 400)
            
            comentario.sudo().write({'texto': texto, 'editado': True})
            
            return APIUtils.json_response({
                'mensaje': 'Comentario actualizado exitosamente',
                'comentario': APIUtils.comentario_to_dict(comentario)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/comentarios/<int:comentario_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_comentario(self, comentario_id, **kwargs):
        """Elimina un comentario (soft delete)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            comentario = request.env['pi.comentario'].sudo().browse(comentario_id)
            
            if not comentario.exists():
                return APIUtils.error_response('Comentario no encontrado', 404)
            
            if comentario.usuario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para eliminar este comentario', 403)
            
            comentario.sudo().write({'activo': False})
            
            return APIUtils.json_response({
                'mensaje': 'Comentario eliminado exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

