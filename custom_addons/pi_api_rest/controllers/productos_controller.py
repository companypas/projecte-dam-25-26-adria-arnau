from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils

class ProductosController(http.Controller):
    
    @http.route('/api/productos', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_productos(self, **kwargs):
        """Lista todos los productos disponibles con filtros"""
        try:
            # Obtener parámetros de query string
            categoria_id = request.httprequest.args.get('categoria_id', type=int)
            etiqueta_id = request.httprequest.args.get('etiqueta_id', type=int)
            nombre = request.httprequest.args.get('nombre')
            precio_min = request.httprequest.args.get('precio_min', type=float)
            precio_max = request.httprequest.args.get('precio_max', type=float)
            ubicacion = request.httprequest.args.get('ubicacion')
            offset = request.httprequest.args.get('offset', 0, type=int)
            limit = request.httprequest.args.get('limit', 20, type=int)
            
            domain = [('estado_venta', '=', 'disponible')]
            
            if categoria_id:
                domain.append(('categoria_id', '=', categoria_id))
            if etiqueta_id:
                domain.append(('etiquetas_ids', 'in', [etiqueta_id]))
            if nombre:
                domain.append(('nombre_producto', 'ilike', nombre))
            if precio_min:
                domain.append(('precio', '>=', precio_min))
            if precio_max:
                domain.append(('precio', '<=', precio_max))
            if ubicacion:
                domain.append(('ubicacion', 'ilike', ubicacion))
            
            # Usar sudo() para evitar restricciones de permisos
            productos = request.env['pi.producto'].sudo().search(domain, offset=offset, limit=limit)
            total = request.env['pi.producto'].sudo().search_count(domain)
            
            return APIUtils.json_response({
                'total': total,
                'offset': offset,
                'limit': limit,
                'productos': [APIUtils.producto_to_dict(p) for p in productos]
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/productos/<int:producto_id>', type='json', auth='public', methods=['GET'])
    def obtener_producto(self, producto_id, **kwargs):
        """Obtiene un producto por ID (sin autenticación requerida)"""
        try:
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            return APIUtils.json_response(APIUtils.producto_to_dict(producto))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/productos', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_producto(self, **kwargs):
        """Crea un nuevo producto"""
        try:
            usuario = request.usuario_actual
            
            nombre = kwargs.get('nombre')
            descripcion = kwargs.get('descripcion')
            precio = kwargs.get('precio', type=float)
            categoria_id = kwargs.get('categoria_id', type=int)
            estado = kwargs.get('estado', 'nuevo')
            antiguedad = kwargs.get('antiguedad', 0, type=int)
            ubicacion = kwargs.get('ubicacion')
            etiquetas_ids = kwargs.get('etiquetas_ids', [])
            
            if not all([nombre, descripcion, precio, categoria_id, ubicacion]):
                return APIUtils.error_response('Parámetros requeridos faltantes', 400)
            
            if precio <= 0:
                return APIUtils.error_response('El precio debe ser mayor que 0', 400)
            
            producto = request.env['pi.producto'].sudo().create({
                'nombre_producto': nombre,
                'descripcion': descripcion,
                'precio': precio,
                'categoria_id': categoria_id,
                'estado': estado,
                'antiguedad_producto': antiguedad,
                'ubicacion': ubicacion,
                'propietario_id': usuario['id'],
                'etiquetas_ids': [(6, 0, etiquetas_ids)] if etiquetas_ids else None,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Producto creado exitosamente',
                'producto': APIUtils.producto_to_dict(producto)
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/productos/<int:producto_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_producto(self, producto_id, **kwargs):
        """Actualiza un producto existente"""
        try:
            usuario = request.usuario_actual
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id != usuario['id']:
                return APIUtils.error_response('No tienes permisos para actualizar este producto', 403)
            
            vals = {}
            if 'nombre' in kwargs:
                vals['nombre_producto'] = kwargs['nombre']
            if 'descripcion' in kwargs:
                vals['descripcion'] = kwargs['descripcion']
            if 'precio' in kwargs:
                vals['precio'] = float(kwargs['precio'])
            if 'ubicacion' in kwargs:
                vals['ubicacion'] = kwargs['ubicacion']
            if 'etiquetas_ids' in kwargs:
                vals['etiquetas_ids'] = [(6, 0, kwargs['etiquetas_ids'])]
            
            producto.sudo().write(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Producto actualizado exitosamente',
                'producto': APIUtils.producto_to_dict(producto)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/productos/<int:producto_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_producto(self, producto_id, **kwargs):
        """Elimina un producto"""
        try:
            usuario = request.usuario_actual
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id != usuario['id']:
                return APIUtils.error_response('No tienes permisos para eliminar este producto', 403)
            
            producto_nombre = producto.nombre_producto
            producto.sudo().unlink()
            
            return APIUtils.json_response({
                'mensaje': f'Producto "{producto_nombre}" eliminado exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)