from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ComprasController(http.Controller):
    
    @http.route('/api/v1/compras', type='json', auth='none', methods=['GET', 'POST'])
    @jwt_required
    def listar_compras(self, **kwargs):
        """Lista todas las compras del usuario autenticado (como comprador o vendedor)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Obtener parámetros de kwargs (JSON-RPC body) o query string
            offset = kwargs.get('offset', 0) if 'offset' in kwargs else request.httprequest.args.get('offset', 0, type=int)
            limit = kwargs.get('limit', 50) if 'limit' in kwargs else request.httprequest.args.get('limit', 50, type=int)
            tipo = kwargs.get('tipo') or request.httprequest.args.get('tipo')
            estado = kwargs.get('estado') or request.httprequest.args.get('estado')
            
            domain = ['|', ('comprador_id', '=', usuario.id), ('vendedor_id', '=', usuario.id)]
            
            if tipo == 'compras':
                domain = [('comprador_id', '=', usuario.id)]
            elif tipo == 'ventas':
                domain = [('vendedor_id', '=', usuario.id)]
            
            if estado:
                domain.append(('estado', '=', estado))
            
            compras = request.env['pi.compra'].sudo().search(domain, offset=offset, limit=limit, order='fecha desc')
            total = request.env['pi.compra'].sudo().search_count(domain)
            
            resultado = []
            for compra in compras:
                resultado.append({
                    'id': compra.id,
                    'id_compra': compra.id_compra,
                    'estado': compra.estado,
                    'monto': compra.monto,
                    'fecha': compra.fecha.isoformat() if compra.fecha else None,
                    'comprador': {
                        'id': compra.comprador_id.id,
                        'nombre': compra.comprador_id.name,
                    } if compra.comprador_id else None,
                    'vendedor': {
                        'id': compra.vendedor_id.id,
                        'nombre': compra.vendedor_id.name,
                    } if compra.vendedor_id else None,
                    'producto': {
                        'id': compra.producto_id.id,
                        'nombre': compra.producto_id.nombre_producto,
                        'precio': compra.producto_id.precio,
                    } if compra.producto_id else None,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'compras': resultado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/compras/crear', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_compra(self, **kwargs):
        """Crea una nueva compra"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            producto_id = data.get('producto_id')
            if not producto_id:
                return APIUtils.error_response('ID de producto requerido', 400)
            
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.estado_venta != 'disponible':
                return APIUtils.error_response('El producto no está disponible', 400)
            
            if producto.propietario_id.id == usuario.id:
                return APIUtils.error_response('No puedes comprar tu propio producto', 400)
            
            compra = request.env['pi.compra'].sudo().create({
                'comprador_id': usuario.id,
                'producto_id': producto_id,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Compra creada exitosamente',
                'compra_id': compra.id,
                'estado': compra.estado
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/compras/<int:compra_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_compra(self, compra_id, **kwargs):
        """Obtiene los detalles de una compra"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            compra = request.env['pi.compra'].sudo().browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.comprador_id.id != usuario.id and compra.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta compra', 403)
            
            return APIUtils.json_response({
                'id': compra.id,
                'id_compra': compra.id_compra,
                'estado': compra.estado,
                'monto': compra.monto,
                'fecha': compra.fecha.isoformat() if compra.fecha else None,
                'comprador': APIUtils.usuario_to_dict(compra.comprador_id),
                'vendedor': APIUtils.usuario_to_dict(compra.vendedor_id),
                'producto': APIUtils.producto_to_dict(compra.producto_id),
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    
    @http.route('/api/v1/compras/<int:compra_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_compra(self, compra_id, **kwargs):
        """Actualiza una compra (solo ciertos campos según estado)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            compra = request.env['pi.compra'].sudo().browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.comprador_id.id != usuario.id and compra.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta compra', 403)
            
            # Solo permitir actualizar notas o información adicional, no el estado
            # El estado se cambia mediante endpoints específicos como confirmar_compra
            vals = {}
            if 'notas' in kwargs:
                vals['notas'] = kwargs['notas']
            
            if vals:
                compra.sudo().write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Compra actualizada exitosamente',
                'compra': {
                    'id': compra.id,
                    'id_compra': compra.id_compra,
                    'estado': compra.estado,
                    'monto': compra.monto,
                    'fecha': compra.fecha.isoformat() if compra.fecha else None,
                }
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/compras/<int:compra_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def cancelar_compra(self, compra_id, **kwargs):
        """Cancela una compra (solo si está en estado pendiente)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            compra = request.env['pi.compra'].sudo().browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            # Solo el comprador puede cancelar
            if compra.comprador_id.id != usuario.id:
                return APIUtils.error_response('Solo el comprador puede cancelar la compra', 403)
            
            if compra.estado != 'pendiente':
                return APIUtils.error_response('Solo se pueden cancelar compras en estado pendiente', 400)
            
            # Cambiar estado a cancelada
            compra.sudo().write({'estado': 'cancelada'})
            
            # Liberar el producto (volver a disponible si estaba reservado)
            if compra.producto_id and compra.producto_id.estado_venta == 'reservado':
                compra.producto_id.sudo().write({'estado_venta': 'disponible'})
            
            return APIUtils.json_response({
                'mensaje': 'Compra cancelada exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/compras/<int:compra_id>/confirmar', type='json', auth='none', methods=['POST'])
    @jwt_required
    def confirmar_compra(self, compra_id, **kwargs):
        """Confirma una compra"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            compra = request.env['pi.compra'].sudo().browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.vendedor_id.id != usuario.id:
                return APIUtils.error_response('Solo el vendedor puede confirmar la compra', 403)
            
            compra.action_confirmar_compra()
            
            return APIUtils.json_response({
                'mensaje': 'Compra confirmada exitosamente',
                'estado': compra.estado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

