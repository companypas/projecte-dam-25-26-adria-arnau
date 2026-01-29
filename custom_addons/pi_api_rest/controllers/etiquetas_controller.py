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
