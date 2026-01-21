from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils

class ConversacionesController(http.Controller):
    
    @http.route('/api/conversaciones', type='json', auth='public', methods=['GET'])
    @jwt_required
    def listar_conversaciones(self, **kwargs):
        """Lista las conversaciones del usuario"""
        try:
            usuario = request.usuario_actual
            offset = kwargs.get('offset', 0, type=int)
            limit = kwargs.get('limit', 20, type=int)
            
            conversaciones = request.env['pi.conversacion'].search([
                '|',
                ('comprador_id', '=', usuario.id),
                ('vendedor_id', '=', usuario.id)
            ], offset=offset, limit=limit, order='last_message_date desc')
            
            total = request.env['pi.conversacion'].search_count([
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
    
    @http.route('/api/conversaciones/<int:conversacion_id>/mensajes', type='json', auth='public', methods=['GET'])
    @jwt_required
    def obtener_mensajes(self, conversacion_id, **kwargs):
        """Obtiene los mensajes de una conversación"""
        try:
            usuario = request.usuario_actual
            conversacion = request.env['pi.conversacion'].browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            offset = kwargs.get('offset', 0, type=int)
            limit = kwargs.get('limit', 50, type=int)
            
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
    
    @http.route('/api/conversaciones/<int:conversacion_id>/mensajes', type='json', auth='public', methods=['POST'])
    @jwt_required
    def enviar_mensaje(self, conversacion_id, **kwargs):
        """Envía un mensaje en una conversación"""
        try:
            usuario = request.usuario_actual
            conversacion = request.env['pi.conversacion'].browse(conversacion_id)
            
            if not conversacion.exists():
                return APIUtils.error_response('Conversación no encontrada', 404)
            
            if conversacion.comprador_id.id != usuario.id and conversacion.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta conversación', 403)
            
            contenido = kwargs.get('contenido')
            if not contenido:
                return APIUtils.error_response('El contenido del mensaje es requerido', 400)
            
            mensaje = request.env['pi.mensaje'].create({
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
    
    @http.route('/api/productos/<int:producto_id>/iniciar-chat', type='json', auth='public', methods=['POST'])
    @jwt_required
    def iniciar_chat_producto(self, producto_id, **kwargs):
        """Inicia un chat sobre un producto"""
        try:
            usuario = request.usuario_actual
            producto = request.env['pi.producto'].browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id == usuario.id:
                return APIUtils.error_response('No puedes chatear contigo mismo', 400)
            
            # Buscar si existe conversación
            conversacion = request.env['pi.conversacion'].search([
                ('producto_id', '=', producto_id),
                ('comprador_id', '=', usuario.id),
                ('vendedor_id', '=', producto.propietario_id.id),
            ], limit=1)
            
            if not conversacion:
                conversacion = request.env['pi.conversacion'].create({
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
