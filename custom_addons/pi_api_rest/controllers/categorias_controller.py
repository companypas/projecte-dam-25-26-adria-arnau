from odoo import http
from odoo.http import request
import base64
from .utils import APIUtils
from .auth import jwt_required
import json

class CategoriasController(http.Controller):

    @http.route('/api/categorias', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_categorias(self, **kwargs):
        """Lista todas las categorías con paginación"""
        try:
            # Obtener parámetros de paginación
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            nombre = request.httprequest.args.get('nombre')
            
            domain = []
            if nombre:
                domain.append(('nombre', 'ilike', nombre))
            
            categorias = request.env['pi.categoria'].sudo().search(domain, offset=offset, limit=limit)
            total = request.env['pi.categoria'].sudo().search_count(domain)
            
            resultado = []
            for cat in categorias:
                imagen_b64 = None
                if cat.imagen:
                    imagen_b64 = base64.b64encode(cat.imagen).decode()
                
                resultado.append({
                    'id': cat.id,
                    'id_categoria': cat.id_categoria,
                    'nombre': cat.nombre,
                    'descripcion': cat.descripcion,
                    'total_productos': cat.total_productos,
                    'imagen': imagen_b64,
                })
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'categorias': resultado
            })
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/categorias/<int:categoria_id>', type='json', auth='public', methods=['GET'])
    def obtener_categoria(self, categoria_id, **kwargs):
        """Obtiene una categoría por ID (sin autenticación requerida)"""
        try:
            categoria = request.env['pi.categoria'].sudo().browse(categoria_id)
            
            if not categoria.exists():
                return APIUtils.error_response('Categoría no encontrada', 404)
            
            imagen_b64 = None
            if categoria.imagen:
                imagen_b64 = base64.b64encode(categoria.imagen).decode()
            
            return APIUtils.json_response({
                'id': categoria.id,
                'id_categoria': categoria.id_categoria,
                'nombre': categoria.nombre,
                'descripcion': categoria.descripcion,
                'total_productos': categoria.total_productos,
                'imagen': imagen_b64,
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
