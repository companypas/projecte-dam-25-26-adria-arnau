from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ValoracionesController(http.Controller):
    
    @http.route('/api/valoraciones', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_valoraciones(self, **kwargs):
        """Lista todas las valoraciones del usuario autenticado (dadas y recibidas)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            tipo = request.httprequest.args.get('tipo')  # 'recibidas' o 'dadas'
            
            domain = []
            if tipo == 'recibidas':
                domain = [('usuario_valorado_id', '=', usuario.id)]
            elif tipo == 'dadas':
                domain = [('usuario_valorador_id', '=', usuario.id)]
            else:
                # Por defecto, mostrar ambas
                domain = ['|', ('usuario_valorado_id', '=', usuario.id), ('usuario_valorador_id', '=', usuario.id)]
            
            valoraciones = request.env['pi.valoracion'].sudo().search(domain, offset=offset, limit=limit, order='create_date desc')
            total = request.env['pi.valoracion'].sudo().search_count(domain)
            
            resultado = []
            for val in valoraciones:
                resultado.append({
                    'id': val.id,
                    'valoracion': val.valoracion,
                    'comentario': val.comentario,
                    'tipo_valoracion': val.tipo_valoracion,
                    'fecha': val.create_date.isoformat() if val.create_date else None,
                    'valorado': {
                        'id': val.usuario_valorado_id.id,
                        'nombre': val.usuario_valorado_id.name,
                    } if val.usuario_valorado_id else None,
                    'valorador': {
                        'id': val.usuario_valorador_id.id,
                        'nombre': val.usuario_valorador_id.name,
                    } if val.usuario_valorador_id else None,
                    'compra_id': val.compra_id.id if val.compra_id else None,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'valoraciones': resultado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/valoraciones/<int:valoracion_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_valoracion(self, valoracion_id, **kwargs):
        """Obtiene una valoración específica"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            valoracion = request.env['pi.valoracion'].sudo().browse(valoracion_id)
            
            if not valoracion.exists():
                return APIUtils.error_response('Valoración no encontrada', 404)
            
            # Solo el valorador o el valorado pueden ver la valoración
            if valoracion.usuario_valorado_id.id != usuario.id and valoracion.usuario_valorador_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta valoración', 403)
            
            return APIUtils.json_response({
                'id': valoracion.id,
                'valoracion': valoracion.valoracion,
                'comentario': valoracion.comentario,
                'tipo_valoracion': valoracion.tipo_valoracion,
                'fecha': valoracion.create_date.isoformat() if valoracion.create_date else None,
                'valorado': {
                    'id': valoracion.usuario_valorado_id.id,
                    'nombre': valoracion.usuario_valorado_id.name,
                } if valoracion.usuario_valorado_id else None,
                'valorador': {
                    'id': valoracion.usuario_valorador_id.id,
                    'nombre': valoracion.usuario_valorador_id.name,
                } if valoracion.usuario_valorador_id else None,
                'compra_id': valoracion.compra_id.id if valoracion.compra_id else None,
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/valoraciones', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_valoracion(self, **kwargs):
        """Crea una valoración de usuario"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            usuario_valorado_id = data.get('usuario_valorado_id')
            compra_id = data.get('compra_id')
            valoracion = data.get('valoracion')
            comentario = data.get('comentario', '')
            tipo_valoracion = data.get('tipo_valoracion')
            
            if not all([usuario_valorado_id, compra_id, valoracion, tipo_valoracion]):
                return APIUtils.error_response('Parámetros requeridos faltantes', 400)
            
            if valoracion not in ['1', '2', '3', '4', '5']:
                return APIUtils.error_response('Valoración debe ser entre 1 y 5', 400)
            
            compra = request.env['pi.compra'].sudo().browse(compra_id)
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.estado != 'confirmada':
                return APIUtils.error_response('La compra debe estar confirmada para valorar', 400)
            
            val = request.env['pi.valoracion'].sudo().create({
                'usuario_valorado_id': usuario_valorado_id,
                'usuario_valorador_id': usuario.id,
                'compra_id': compra_id,
                'valoracion': valoracion,
                'comentario': comentario,
                'tipo_valoracion': tipo_valoracion,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Valoración creada exitosamente',
                'valoracion': {
                    'id': val.id,
                    'valoracion': val.valoracion,
                    'comentario': val.comentario,
                    'tipo_valoracion': val.tipo_valoracion,
                }
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/valoraciones/<int:valoracion_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_valoracion(self, valoracion_id, **kwargs):
        """Actualiza una valoración propia"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            valoracion = request.env['pi.valoracion'].sudo().browse(valoracion_id)
            
            if not valoracion.exists():
                return APIUtils.error_response('Valoración no encontrada', 404)
            
            # Solo el valorador puede actualizar su valoración
            if valoracion.usuario_valorador_id.id != usuario.id:
                return APIUtils.error_response('Solo puedes actualizar tus propias valoraciones', 403)
            
            vals = {}
            if 'valoracion' in kwargs:
                val_num = kwargs['valoracion']
                if val_num not in ['1', '2', '3', '4', '5']:
                    return APIUtils.error_response('Valoración debe ser entre 1 y 5', 400)
                vals['valoracion'] = val_num
            if 'comentario' in kwargs:
                vals['comentario'] = kwargs['comentario']
            
            if vals:
                valoracion.sudo().write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Valoración actualizada exitosamente',
                'valoracion': {
                    'id': valoracion.id,
                    'valoracion': valoracion.valoracion,
                    'comentario': valoracion.comentario,
                    'tipo_valoracion': valoracion.tipo_valoracion,
                }
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/valoraciones/<int:valoracion_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_valoracion(self, valoracion_id, **kwargs):
        """Elimina una valoración propia"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            valoracion = request.env['pi.valoracion'].sudo().browse(valoracion_id)
            
            if not valoracion.exists():
                return APIUtils.error_response('Valoración no encontrada', 404)
            
            # Solo el valorador puede eliminar su valoración
            if valoracion.usuario_valorador_id.id != usuario.id:
                return APIUtils.error_response('Solo puedes eliminar tus propias valoraciones', 403)
            
            valoracion.sudo().unlink()
            
            return APIUtils.json_response({
                'mensaje': 'Valoración eliminada exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

