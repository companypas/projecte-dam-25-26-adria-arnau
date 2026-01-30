from odoo import http
from odoo.http import request
from .utils import APIUtils
from .auth import jwt_required
import json

class EtiquetasController(http.Controller):

    @http.route('/api/v1/etiquetas', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_etiquetas(self, **kwargs):
        """Lista todas las etiquetas con paginación"""
        try:
            # Obtener parámetros de paginación
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            nombre = request.httprequest.args.get('nombre')
            
            domain = [('activo', '=', True)]
            if nombre:
                domain.append(('nombre', 'ilike', nombre))
            
            etiquetas = request.env['pi.etiqueta'].sudo().search(domain, offset=offset, limit=limit)
            total = request.env['pi.etiqueta'].sudo().search_count(domain)
            
            resultado = []
            for etiqueta in etiquetas:
                resultado.append({
                    'id': etiqueta.id,
                    'nombre': etiqueta.nombre,
                    'descripcion': etiqueta.descripcion,
                    'color': etiqueta.color,
                    'total_productos': etiqueta.total_productos,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'etiquetas': resultado
            })
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/etiquetas/<int:etiqueta_id>', type='json', auth='none', methods=['GET'])
    @jwt_required
    def obtener_etiqueta(self, etiqueta_id, **kwargs):
        """Obtiene una etiqueta por ID (sin autenticación requerida)"""
        try:
            etiqueta = request.env['pi.etiqueta'].sudo().browse(etiqueta_id)
            
            if not etiqueta.exists():
                return APIUtils.error_response('Etiqueta no encontrada', 404)
            
            if not etiqueta.activo:
                return APIUtils.error_response('Etiqueta no disponible', 404)
            
            return APIUtils.json_response({
                'id': etiqueta.id,
                'nombre': etiqueta.nombre,
                'descripcion': etiqueta.descripcion,
                'color': etiqueta.color,
                'total_productos': etiqueta.total_productos,
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/etiquetas', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_etiqueta(self, **kwargs):
        """Crea una nueva etiqueta"""
        try:
            data = request.jsonrequest.get('params', {})
            
            # Validar campo requerido
            if not data.get('nombre'):
                return APIUtils.error_response('El campo nombre es obligatorio', 400)
            
            # Verificar si ya existe una etiqueta con ese nombre
            etiqueta_existente = request.env['pi.etiqueta'].sudo().search([
                ('nombre', '=', data.get('nombre'))
            ], limit=1)
            
            if etiqueta_existente:
                return APIUtils.error_response('Ya existe una etiqueta con ese nombre', 400)
            
            # Preparar valores para crear
            valores = {
                'nombre': data.get('nombre'),
                'descripcion': data.get('descripcion', ''),
                'color': data.get('color', '#6c757d'),
                'activo': data.get('activo', True),
            }
            
            # Crear la etiqueta
            nueva_etiqueta = request.env['pi.etiqueta'].sudo().create(valores)
            
            return APIUtils.json_response({
                'message': 'Etiqueta creada exitosamente',
                'etiqueta': {
                    'id': nueva_etiqueta.id,
                    'nombre': nueva_etiqueta.nombre,
                    'descripcion': nueva_etiqueta.descripcion,
                    'color': nueva_etiqueta.color,
                    'activo': nueva_etiqueta.activo,
                }
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
