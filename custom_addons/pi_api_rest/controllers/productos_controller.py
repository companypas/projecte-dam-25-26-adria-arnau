from odoo import http
from odoo.http import request
from .auth import jwt_required, JWTAuth
from .utils import APIUtils
import json

class ProductosController(http.Controller):
    
    @http.route('/api/v1/productos/listar', type='json', auth='none', methods=['GET', 'POST'])
    @jwt_required
    def listar_productos(self, **kwargs):
        """Lista todos los productos disponibles con filtros"""
        try:
            # Obtener parámetros de kwargs (JSON-RPC body) o query string
            categoria_id = kwargs.get('categoria_id') or request.httprequest.args.get('categoria_id', type=int)
            etiqueta_id = kwargs.get('etiqueta_id') or request.httprequest.args.get('etiqueta_id', type=int)
            nombre = kwargs.get('nombre') or request.httprequest.args.get('nombre')
            precio_min = kwargs.get('precio_min') or request.httprequest.args.get('precio_min', type=float)
            precio_max = kwargs.get('precio_max') or request.httprequest.args.get('precio_max', type=float)
            ubicacion = kwargs.get('ubicacion') or request.httprequest.args.get('ubicacion')
            estado_venta = kwargs.get('estado_venta') or request.httprequest.args.get('estado_venta')
            offset = kwargs.get('offset', 0) if 'offset' in kwargs else request.httprequest.args.get('offset', 0, type=int)
            limit = kwargs.get('limit', 20) if 'limit' in kwargs else request.httprequest.args.get('limit', 20, type=int)
            
            # Por defecto solo mostrar productos disponibles, a menos que se especifique otro estado_venta
            domain = []
            if estado_venta:
                domain.append(('estado_venta', '=', estado_venta))
            else:
                domain.append(('estado_venta', '=', 'disponible'))
            
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
    
    @http.route('/api/v1/productos/<int:producto_id>', type='json', auth='none', methods=['GET', 'POST'])
    @jwt_required
    def obtener_producto(self, producto_id, **kwargs):
        """Obtiene un producto por ID (sin autenticación requerida)"""
        try:
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            return APIUtils.json_response(APIUtils.producto_to_dict(producto, include_all_images=True))
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos', type='json', auth='none', methods=['POST'])
    @jwt_required
    def crear_producto(self, **kwargs):
        """Crea un nuevo producto"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            nombre = data.get('nombre')
            descripcion = data.get('descripcion')
            precio = data.get('precio')
            categoria_id = data.get('categoria_id')
            estado = data.get('estado', 'nuevo')

            ubicacion = data.get('ubicacion')
            etiquetas_ids = data.get('etiquetas_ids', [])
            imagenes = data.get('imagenes', [])  # Lista de imágenes en base64
            
            if not all([nombre, descripcion, precio, categoria_id, ubicacion]):
                return APIUtils.error_response('Parámetros requeridos faltantes', 400)
            
            # Convertir y validar tipos
            try:
                precio = float(precio)
                categoria_id = int(categoria_id)
            except (ValueError, TypeError) as e:
                return APIUtils.error_response(f'Error en formato de datos: {str(e)}', 400)
            
            if precio <= 0:
                return APIUtils.error_response('El precio debe ser mayor que 0', 400)
            
            # Preparar datos de imágenes para la creación
            imagenes_vals = []
            if imagenes:
                for i, img_base64 in enumerate(imagenes):
                    if img_base64:
                        imagenes_vals.append((0, 0, {
                            'imagen': img_base64,
                            'nombre': f'{nombre}_imagen_{i+1}',
                            'sequence': (i + 1) * 10
                        }))
            
            producto = request.env['pi.producto'].sudo().create({
                'nombre_producto': nombre,
                'descripcion': descripcion,
                'precio': precio,
                'categoria_id': categoria_id,
                'estado': estado,
                'estado_venta': 'disponible',  # Nuevo producto siempre disponible

                'ubicacion': ubicacion,
                'propietario_id': usuario.id,
                'etiquetas_ids': [(6, 0, etiquetas_ids)] if etiquetas_ids else None,
                'imagenes_ids': imagenes_vals if imagenes_vals else None,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Producto creado exitosamente',
                'producto': APIUtils.producto_to_dict(producto)
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos/<int:producto_id>', type='json', auth='none', methods=['PUT'])
    @jwt_required
    def actualizar_producto(self, producto_id, **kwargs):
        """Actualiza un producto existente"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para actualizar este producto', 403)
            
            # Obtener datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            vals = {}
            if 'nombre' in data:
                vals['nombre_producto'] = data['nombre']
            if 'descripcion' in data:
                vals['descripcion'] = data['descripcion']
            if 'precio' in data:
                vals['precio'] = float(data['precio'])
            if 'ubicacion' in data:
                vals['ubicacion'] = data['ubicacion']
            if 'etiquetas_ids' in data:
                vals['etiquetas_ids'] = [(6, 0, data['etiquetas_ids'])]
            
            # Verificar que hay datos para actualizar
            if not vals:
                return APIUtils.error_response('No hay datos para actualizar', 400)
            
            # Actualizar el producto
            producto.sudo().write(vals)
            
            # Invalidar caché y hacer commit para asegurar persistencia
            producto.invalidate_recordset()
            request.env.cr.commit()
            
            # Refrescar el producto desde la base de datos
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            return APIUtils.json_response({
                'mensaje': 'Producto actualizado exitosamente',
                'producto': APIUtils.producto_to_dict(producto)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/v1/productos/<int:producto_id>', type='json', auth='none', methods=['DELETE'])
    @jwt_required
    def eliminar_producto(self, producto_id, **kwargs):
        """Elimina un producto"""
        try:
            usuario_data = request.usuario_actual
            
            # Buscar el usuario por id_usuario para obtener su ID numérico de Odoo
            usuario = request.env['pi.usuario'].sudo().search([('id_usuario', '=', usuario_data['id'])], limit=1)
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 404)
            
            producto = request.env['pi.producto'].sudo().browse(producto_id)
            
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.propietario_id.id != usuario.id:
                return APIUtils.error_response('No tienes permisos para eliminar este producto', 403)
            
            producto_nombre = producto.nombre_producto
            producto.sudo().unlink()
            
            return APIUtils.json_response({
                'mensaje': f'Producto "{producto_nombre}" eliminado exitosamente'
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)