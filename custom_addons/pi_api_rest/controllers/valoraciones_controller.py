from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils

class ValoracionesController(http.Controller):
    
    @http.route('/api/valoraciones', type='json', auth='public', methods=['POST'])
    @jwt_required
    def crear_valoracion(self, **kwargs):
        """Crea una valoración de usuario"""
        try:
            usuario = request.usuario_actual
            
            usuario_valorado_id = kwargs.get('usuario_valorado_id', type=int)
            compra_id = kwargs.get('compra_id', type=int)
            valoracion = kwargs.get('valoracion')
            comentario = kwargs.get('comentario', '')
            tipo_valoracion = kwargs.get('tipo_valoracion')
            
            if not all([usuario_valorado_id, compra_id, valoracion, tipo_valoracion]):
                return APIUtils.error_response('Parámetros requeridos faltantes', 400)
            
            if valoracion not in ['1', '2', '3', '4', '5']:
                return APIUtils.error_response('Valoración debe ser entre 1 y 5', 400)
            
            compra = request.env['pi.compra'].browse(compra_id)
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.estado != 'confirmada':
                return APIUtils.error_response('La compra debe estar confirmada para valorar', 400)
            
            val = request.env['pi.valoracion'].create({
                'usuario_valorado_id': usuario_valorado_id,
                'usuario_valorador_id': usuario.id,
                'compra_id': compra_id,
                'valoracion': valoracion,
                'comentario': comentario,
                'tipo_valoracion': tipo_valoracion,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Valoración creada exitosamente',
                'valoracion_id': val.id
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

