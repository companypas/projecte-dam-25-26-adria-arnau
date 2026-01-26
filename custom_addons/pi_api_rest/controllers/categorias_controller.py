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
    
    @http.route('/api/categorias', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_categoria(self, **kwargs):
        """Crea una nueva categoría"""
        try:
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            nombre = data.get('nombre')
            descripcion = data.get('descripcion', '')
            imagen_b64 = data.get('imagen')
            
            if not nombre:
                return APIUtils.error_response('El nombre es requerido', 400)
            
            vals = {
                'nombre': nombre,
                'descripcion': descripcion,
            }
            
            # Procesar imagen si se proporciona
            if imagen_b64:
                try:
                    vals['imagen'] = base64.b64decode(imagen_b64)
                except Exception as e:
                    return APIUtils.error_response(f'Error al decodificar la imagen: {str(e)}', 400)
            
            categoria = request.env['pi.categoria'].sudo().create(vals)
            
            imagen_response = None
            if categoria.imagen:
                imagen_response = base64.b64encode(categoria.imagen).decode()
            
            return APIUtils.json_response({
                'mensaje': 'Categoría creada exitosamente',
                'categoria': {
                    'id': categoria.id,
                    'id_categoria': categoria.id_categoria,
                    'nombre': categoria.nombre,
                    'descripcion': categoria.descripcion,
                    'total_productos': categoria.total_productos,
                    'imagen': imagen_response,
                }
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/categorias/<int:categoria_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_categoria(self, categoria_id, **kwargs):
        """Actualiza una categoría existente"""
        try:
            categoria = request.env['pi.categoria'].sudo().browse(categoria_id)
            
            if not categoria.exists():
                return APIUtils.error_response('Categoría no encontrada', 404)
            
            vals = {}
            if 'nombre' in kwargs:
                vals['nombre'] = kwargs['nombre']
            if 'descripcion' in kwargs:
                vals['descripcion'] = kwargs['descripcion']
            if 'imagen' in kwargs:
                imagen_b64 = kwargs['imagen']
                if imagen_b64:
                    try:
                        vals['imagen'] = base64.b64decode(imagen_b64)
                    except Exception as e:
                        return APIUtils.error_response(f'Error al decodificar la imagen: {str(e)}', 400)
            
            categoria.sudo().write(vals)
            
            imagen_response = None
            if categoria.imagen:
                imagen_response = base64.b64encode(categoria.imagen).decode()
            
            return APIUtils.json_response({
                'mensaje': 'Categoría actualizada exitosamente',
                'categoria': {
                    'id': categoria.id,
                    'id_categoria': categoria.id_categoria,
                    'nombre': categoria.nombre,
                    'descripcion': categoria.descripcion,
                    'total_productos': categoria.total_productos,
                    'imagen': imagen_response,
                }
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/categorias/<int:categoria_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_categoria(self, categoria_id, **kwargs):
        """Elimina una categoría"""
        try:
            categoria = request.env['pi.categoria'].sudo().browse(categoria_id)
            
            if not categoria.exists():
                return APIUtils.error_response('Categoría no encontrada', 404)
            
            # Verificar si tiene productos asociados
            if categoria.total_productos > 0:
                return APIUtils.error_response('No se puede eliminar una categoría con productos asociados', 400)
            
            categoria_nombre = categoria.nombre
            categoria.sudo().unlink()
            
            return APIUtils.json_response({
                'mensaje': f'Categoría "{categoria_nombre}" eliminada exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
