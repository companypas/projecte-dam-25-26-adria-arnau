from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ConversacionesController(http.Controller):
    
    @http.route('/api/v1/conversaciones', type='json', auth='public', methods=['GET'])
    @jwt_required
    def listar_conversaciones(self, **kwargs):
        """Lista las conversaciones del usuario"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            conversaciones = request.env['pi.conversacion'].sudo().search([
                '|',
                ('comprador_id', '=', usuario.id),
                ('vendedor_id', '=', usuario.id)
            ], offset=offset, limit=limit, order='last_message_date desc')
            
            total = request.env['pi.conversacion'].sudo().search_count([
                '|',
                ('comprador_id', '=', usuario.id),
                ('vendedor_id', '=', usuario.id)
            ])
            
            resultado = []
            for conv in conversaciones:
                otro_usuario = conv.vendedor_id if conv.comprador_id == usuario else conv.comprador_id
                resultado.append({
                    'id': conv.id,
                    'asunto': conv.name,
                    'otro_usuario': {
                        'id': otro_usuario.id,
                        'nombre': otro_usuario.name,
                    },
                    'estado': conv.state,
                    'total_mensajes': conv.total_mensajes,
                    'ultimo_mensaje': conv.last_message_preview,
                    'fecha_ultimo_mensaje': conv.last_message_date.isoformat() if conv.last_message_date else None,
                    'producto_id': conv.producto_id.id if conv.producto_id else None,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'conversaciones': resultado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    
    @http.route('/api/v1/conversaciones/<int:conversacion_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_conversacion(self, conversacion_id, **kwargs):
        """Obtiene los detalles de una conversación específica"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            conversacion = request.env['pi.conversacion'].sudo().browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            otro_usuario = conversacion.vendedor_id if conversacion.comprador_id.id == usuario.id else conversacion.comprador_id
            
            return APIUtils.json_response({
                'id': conversacion.id,
                'asunto': conversacion.name,
                'otro_usuario': {
                    'id': otro_usuario.id,
                    'nombre': otro_usuario.name,
                },
                'estado': conversacion.state,
                'total_mensajes': conversacion.total_mensajes,
                'ultimo_mensaje': conversacion.last_message_preview,
                'fecha_ultimo_mensaje': conversacion.last_message_date.isoformat() if conversacion.last_message_date else None,
                'producto_id': conversacion.producto_id.id if conversacion.producto_id else None,
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/conversaciones/<int:conversacion_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_conversacion(self, conversacion_id, **kwargs):
        """Actualiza el estado de una conversación (archivar, etc.)"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            conversacion = request.env['pi.conversacion'].sudo().browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            vals = {}
            if 'estado' in kwargs:
                vals['state'] = kwargs['estado']
            
            if vals:
                conversacion.sudo().write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Conversación actualizada exitosamente',
                'conversacion_id': conversacion.id,
                'estado': conversacion.state
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    
    @http.route('/api/v1/conversaciones/<int:conversacion_id>/mensajes', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_mensajes(self, conversacion_id, **kwargs):
        """Obtiene los mensajes de una conversación"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            conversacion = request.env['pi.conversacion'].sudo().browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 50, type=int)
            
            mensajes = conversacion.mensaje_ids.sorted('fecha_envio')[offset:offset+limit]
            
            # Marcar mensajes como leídos
            for mensaje in mensajes:
                if mensaje.remitente_id.id != usuario.id and not mensaje.leido:
                    mensaje.leido = True
            
            return APIUtils.json_response({
                'conversacion_id': conversacion_id,
                'total_mensajes': conversacion.total_mensajes,
                'offset': offset,
                'limit': limit,
                'mensajes': [APIUtils.mensaje_to_dict(m) for m in mensajes]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    
    @http.route('/api/v1/conversaciones/<int:conversacion_id>/mensajes', type='json', auth='none', methods=['POST'])
    @jwt_required
    def enviar_mensaje(self, conversacion_id, **kwargs):
        """Envía un mensaje en una conversación"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            conversacion = request.env['pi.conversacion'].sudo().browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            contenido = data.get('contenido')
            if not contenido:
                return APIUtils.error_response('El contenido del mensaje es requerido', 400)
            
            mensaje = request.env['pi.mensaje'].sudo().create({
                'conversacion_id': conversacion_id,
                'remitente_id': usuario.id,
                'contenido': contenido,
                'tipo_contenido': 'texto',
            })
            
            return APIUtils.json_response({
                'mensaje': 'Mensaje enviado exitosamente',
                'data': APIUtils.mensaje_to_dict(mensaje)
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos/<int:producto_id>/iniciar-chat', type='json', auth='public', methods=['POST'])
    @jwt_required
    def iniciar_chat_producto(self, producto_id, **kwargs):
        """Inicia un chat sobre un producto"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id == usuario.id:
                return APIUtils.error_response('No puedes chatear contigo mismo', 400)
            
            # Buscar si existe conversación
            conversacion = request.env['pi.conversacion'].sudo().search([
                ('producto_id', '=', producto_id),
                ('comprador_id', '=', usuario.id),
                ('vendedor_id', '=', producto.propietario_id.id),
            ], limit=1)
            
            if not conversacion:
                conversacion = request.env['pi.conversacion'].sudo().create({
                    'name': f'Chat sobre: {producto.nombre_producto}',
                    'comprador_id': usuario.id,
                    'vendedor_id': producto.propietario_id.id,
                    'producto_id': producto_id,
                })
            
            return APIUtils.json_response({
                'conversacion_id': conversacion.id,
                'asunto': conversacion.name,
                'mensaje': 'Chat iniciado exitosamente'
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
