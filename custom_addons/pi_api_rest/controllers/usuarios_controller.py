from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class UsuariosController(http.Controller):
    
    @http.route('/api/v1/usuarios', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_usuarios(self, **kwargs):
        """Lista usuarios con paginación y filtros"""
        try:
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            nombre = request.httprequest.args.get('nombre')
            ubicacion = request.httprequest.args.get('ubicacion')
            
            domain = []
            if nombre:
                domain.append(('name', 'ilike', nombre))
            if ubicacion:
                domain.append(('street', 'ilike', ubicacion))
            
            usuarios = request.env['pi.usuario'].sudo().search(domain, offset=offset, limit=limit)
            total = request.env['pi.usuario'].sudo().search_count(domain)
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'usuarios': [APIUtils.usuario_to_dict(u) for u in usuarios]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/<int:usuario_id>', type='json', auth='none', methods=['GET', 'POST'])
    @jwt_required
    def obtener_usuario(self, usuario_id, **kwargs):
        """Obtiene el perfil público de un usuario"""
        try:
            usuario = request.env['pi.usuario'].sudo().browse(usuario_id)
            
            if not usuario.exists():
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            return APIUtils.json_response(APIUtils.usuario_to_dict(usuario))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/<int:usuario_id>/productos', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_productos_usuario(self, usuario_id, **kwargs):
        """Lista productos de un usuario específico"""
        try:
            usuario = request.env['pi.usuario'].sudo().browse(usuario_id)
            
            if not usuario.exists():
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            productos = request.env['pi.producto'].sudo().search(
                [('propietario_id', '=', usuario_id), ('estado_venta', '=', 'disponible')],
                offset=offset,
                limit=limit
            )
            total = request.env['pi.producto'].sudo().search_count(
                [('propietario_id', '=', usuario_id), ('estado_venta', '=', 'disponible')]
            )
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'productos': [APIUtils.producto_to_dict(p) for p in productos]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/<int:usuario_id>/valoraciones', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_valoraciones_usuario(self, usuario_id, **kwargs):
        """Lista valoraciones de un usuario específico"""
        try:
            usuario = request.env['pi.usuario'].sudo().browse(usuario_id)
            
            if not usuario.exists():
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            valoraciones = request.env['pi.valoracion'].sudo().search(
                [('usuario_valorado_id', '=', usuario_id)],
                offset=offset,
                limit=limit,
                order='create_date desc'
            )
            total = request.env['pi.valoracion'].sudo().search_count(
                [('usuario_valorado_id', '=', usuario_id)]
            )
            
            resultado = []
            for val in valoraciones:
                resultado.append({
                    'id': val.id,
                    'valoracion': val.valoracion,
                    'comentario': val.comentario,
                    'tipo_valoracion': val.tipo_valoracion,
                    'fecha': val.create_date.isoformat() if val.create_date else None,
                    'valorador': {
                        'id': val.usuario_valorador_id.id,
                        'nombre': val.usuario_valorador_id.name,
                    } if val.usuario_valorador_id else None,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'valoraciones': resultado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/perfil', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_perfil_actual(self, **kwargs):
        """Obtiene el perfil del usuario autenticado"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            return APIUtils.json_response(APIUtils.usuario_to_dict(usuario))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/perfil', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_perfil(self, **kwargs):
        """Actualiza el perfil del usuario"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            vals = {}
            if 'nombre' in kwargs:
                vals['name'] = kwargs['nombre']
            if 'email' in kwargs:
                vals['email'] = kwargs['email']
            if 'telefono' in kwargs:
                vals['phone'] = kwargs['telefono']
            if 'ubicacion' in kwargs:
                vals['street'] = kwargs['ubicacion']
            
            usuario.sudo().write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Perfil actualizado exitosamente',
                'usuario': APIUtils.usuario_to_dict(usuario)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/usuarios/perfil', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_cuenta(self, **kwargs):
        """Elimina/desactiva la cuenta del usuario autenticado"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Desactivar en lugar de eliminar completamente
            usuario.sudo().write({'active': False})
            
            return APIUtils.json_response({
                'mensaje': 'Cuenta desactivada exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

