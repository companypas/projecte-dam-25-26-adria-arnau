from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ReportesController(http.Controller):
    
    @http.route('/api/v1/reportes', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_reportes(self, **kwargs):
        """Lista todos los reportes del usuario autenticado"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            estado = request.httprequest.args.get('estado')
            tipo_reporte = request.httprequest.args.get('tipo_reporte')
            
            domain = [('reportado_por_id', '=', usuario.id)]
            
            if estado:
                domain.append(('estado', '=', estado))
            if tipo_reporte:
                domain.append(('tipo_reporte', '=', tipo_reporte))
            
            reportes = request.env['pi.reporte'].sudo().search(domain, offset=offset, limit=limit, order='create_date desc')
            total = request.env['pi.reporte'].sudo().search_count(domain)
            
            resultado = []
            for reporte in reportes:
                resultado.append({
                    'id': reporte.id,
                    'tipo_reporte': reporte.tipo_reporte,
                    'motivo': reporte.motivo,
                    'estado': reporte.estado,
                    'fecha': reporte.create_date.isoformat() if reporte.create_date else None,
                    'producto_reportado': reporte.producto_reportado_id.nombre_producto if reporte.producto_reportado_id else None,
                    'usuario_reportado': reporte.usuario_reportado_id.name if reporte.usuario_reportado_id else None,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'reportes': resultado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/reportes/<int:reporte_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_reporte(self, reporte_id, **kwargs):
        """Obtiene un reporte específico"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            reporte = request.env['pi.reporte'].sudo().browse(reporte_id)
            
            if not reporte.exists():
                return APIUtils.error_response('Reporte no encontrado', 404)
            
            # Solo el reportador puede ver sus reportes
            if reporte.reportado_por_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a este reporte', 403)
            
            return APIUtils.json_response({
                'id': reporte.id,
                'tipo_reporte': reporte.tipo_reporte,
                'motivo': reporte.motivo,
                'estado': reporte.estado,
                'fecha': reporte.create_date.isoformat() if reporte.create_date else None,
                'producto_reportado': {
                    'id': reporte.producto_reportado_id.id,
                    'nombre': reporte.producto_reportado_id.nombre_producto,
                } if reporte.producto_reportado_id else None,
                'usuario_reportado': {
                    'id': reporte.usuario_reportado_id.id,
                    'nombre': reporte.usuario_reportado_id.name,
                } if reporte.usuario_reportado_id else None,
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/reportes', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_reporte(self, **kwargs):
        """Crea un reporte de producto, usuario o comentario"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            tipo_reporte = data.get('tipo_reporte')
            motivo = data.get('motivo')
            
            if not tipo_reporte or not motivo:
                return APIUtils.error_response('Tipo de reporte y motivo son requeridos', 400)
            
            if tipo_reporte not in ['producto', 'usuario', 'comentario']:
                return APIUtils.error_response('Tipo de reporte inválido', 400)
            
            vals = {
                'tipo_reporte': tipo_reporte,
                'motivo': motivo,
                'reportado_por_id': usuario.id,
            }
            
            if tipo_reporte == 'producto':
                producto_id = data.get('producto_id')
                if not producto_id:
                    return APIUtils.error_response('ID de producto requerido', 400)
                vals['producto_reportado_id'] = producto_id
                
            elif tipo_reporte == 'usuario':
                usuario_id = data.get('usuario_id')
                if not usuario_id:
                    return APIUtils.error_response('ID de usuario requerido', 400)
                if usuario_id == usuario.id:
                    return APIUtils.error_response('No puedes reportarte a ti mismo', 400)
                vals['usuario_reportado_id'] = usuario_id
                
            elif tipo_reporte == 'comentario':
                comentario_id = data.get('comentario_id')
                if not comentario_id:
                    return APIUtils.error_response('ID de comentario requerido', 400)
                vals['comentario_reportado_id'] = comentario_id
            
            reporte = request.env['pi.reporte'].sudo().create(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Reporte creado exitosamente',
                'reporte_id': reporte.id
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
