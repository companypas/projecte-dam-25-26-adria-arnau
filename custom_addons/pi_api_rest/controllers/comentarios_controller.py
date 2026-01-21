from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils

class ComentariosController(http.Controller):
    
    @http.route('/api/productos/<int:producto_id>/comentarios', type='json', auth='public', methods=['GET'])
    def obtener_comentarios(self, producto_id, **kwargs):
        """Obtiene comentarios de un producto"""
        try:
            producto = request.env['pi.producto'].browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            offset = kwargs.get('offset', 0, type=int)
            limit = kwargs.get('limit', 20, type=int)
            
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
    
    @http.route('/api/productos/<int:producto_id>/comentarios', type='json', auth='public', methods=['POST'])
    @jwt_required
    def crear_comentario(self, producto_id, **kwargs):
        """Crea un comentario en un producto"""
        try:
            usuario = request.usuario_actual
            producto = request.env['pi.producto'].browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            texto = kwargs.get('texto')
            if not texto:
                return APIUtils.error_response('El texto del comentario es requerido', 400)
            
            comentario = request.env['pi.comentario'].create({
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
    
    @http.route('/api/comentarios/<int:comentario_id>', type='json', auth='public', methods=['PUT'])
    @jwt_required
    def actualizar_comentario(self, comentario_id, **kwargs):
        """Actualiza un comentario"""
        try:
            usuario = request.usuario_actual
            comentario = request.env['pi.comentario'].browse(comentario_id)
            
            if not comentario.exists():
                return APIUtils.error_response('Comentario no encontrado', 404)
            
            if comentario.usuario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para actualizar este comentario', 403)
            
            texto = kwargs.get('texto')
            if not texto:
                return APIUtils.error_response('El texto del comentario es requerido', 400)
            
            comentario.write({'texto': texto})
            
            return APIUtils.json_response({
                'mensaje': 'Comentario actualizado exitosamente',
                'comentario': APIUtils.comentario_to_dict(comentario)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/comentarios/<int:comentario_id>', type='json', auth='public', methods=['DELETE'])
    @jwt_required
    def eliminar_comentario(self, comentario_id, **kwargs):
        """Elimina un comentario"""
        try:
            usuario = request.usuario_actual
            comentario = request.env['pi.comentario'].browse(comentario_id)
            
            if not comentario.exists():
                return APIUtils.error_response('Comentario no encontrado', 404)
            
            if comentario.usuario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para eliminar este comentario', 403)
            
            comentario.activo = False
            
            return APIUtils.json_response({
                'mensaje': 'Comentario eliminado exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

