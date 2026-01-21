from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils

class UsuariosController(http.Controller):
    
    @http.route('/api/usuarios/<int:usuario_id>', type='json', auth='public', methods=['GET'])
    def obtener_usuario(self, usuario_id, **kwargs):
        """Obtiene el perfil público de un usuario"""
        try:
            usuario = request.env['pi.usuario'].browse(usuario_id)
            
            if not usuario.exists():
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            return APIUtils.json_response(APIUtils.usuario_to_dict(usuario))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/usuarios/perfil', type='json', auth='public', methods=['GET'])
    @jwt_required
    def obtener_perfil_actual(self, **kwargs):
        """Obtiene el perfil del usuario autenticado"""
        try:
            usuario = request.usuario_actual
            return APIUtils.json_response(APIUtils.usuario_to_dict(usuario))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/usuarios/perfil', type='json', auth='public', methods=['PUT'])
    @jwt_required
    def actualizar_perfil(self, **kwargs):
        """Actualiza el perfil del usuario"""
        try:
            usuario = request.usuario_actual
            
            vals = {}
            if 'nombre' in kwargs:
                vals['name'] = kwargs['nombre']
            if 'email' in kwargs:
                vals['email'] = kwargs['email']
            if 'telefono' in kwargs:
                vals['phone'] = kwargs['telefono']
            if 'ubicacion' in kwargs:
                vals['street'] = kwargs['ubicacion']
            
            usuario.write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Perfil actualizado exitosamente',
                'usuario': APIUtils.usuario_to_dict(usuario)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

