import base64
import json
from odoo.http import request

class APIUtils:
    """Utilidades para la API"""
    
    @staticmethod
    def json_response(data, status=200):
        """Respuesta JSON estándar para rutas type='json'"""
        # Para type='json', simplemente devolver el diccionario
        # Odoo se encarga de la serialización
        return data
    
    @staticmethod
    def error_response(mensaje, status=400):
        """Respuesta de error"""
        # Devolver un diccionario con la estructura de error
        return {
            'error': mensaje,
            'status': status
        }
    
    @staticmethod
    def usuario_to_dict(usuario):
        """Convierte un usuario a diccionario"""
        return {
            'id': usuario.id,
            'id_usuario': usuario.id_usuario,
            'nombre': usuario.name,
            'email': usuario.email,
            'telefono': usuario.phone or '',
            'ubicacion': usuario.street or '',
            'fecha_registro': usuario.fecha_registro.isoformat() if usuario.fecha_registro else None,
            'antiguedad': usuario.antiguedad,
            'valoracion_promedio': round(usuario.valoracion_promedio, 2),
            'total_valoraciones': usuario.total_valoraciones,
            'total_productos_venta': usuario.total_productos_venta,
            'total_productos_vendidos': usuario.total_productos_vendidos,
            'total_productos_comprados': usuario.total_productos_comprados,
        }
    
    @staticmethod
    def producto_to_dict(producto, include_all_images=False):
        """Convierte un producto a diccionario
        
        Args:
            producto: Registro de producto
            include_all_images: Si True, incluye todas las imágenes (para detalle).
                              Si False, solo incluye imagen_principal (para listados).
        """
        import logging
        _logger = logging.getLogger(__name__)
        
        imagen_principal = None
        imagenes = []
        try:
            if producto.imagenes_ids:
                imagenes_ordenadas = producto.imagenes_ids.sorted('sequence')
                for img in imagenes_ordenadas:
                    img_data = img.imagen
                    if img_data:
                        if isinstance(img_data, str):
                            img_str = img_data
                        elif isinstance(img_data, (bytes, memoryview)):
                            img_str = bytes(img_data).decode('utf-8')
                        else:
                            continue
                        if include_all_images:
                            imagenes.append(img_str)
                        if imagen_principal is None:
                            imagen_principal = img_str
                            if not include_all_images:
                                break
        except Exception as e:
            _logger.warning(f"Error getting product image for id={producto.id}: {e}")
        
        result = {
            'id': producto.id,
            'id_producto': producto.id_producto,
            'nombre': producto.nombre_producto,
            'descripcion': producto.descripcion,
            'precio': producto.precio,
            'estado': producto.estado,
            'antiguedad_meses': producto.antiguedad_producto,
            'ubicacion': producto.ubicacion,
            'estado_venta': producto.estado_venta,
            'categoria': {
                'id': producto.categoria_id.id,
                'nombre': producto.categoria_id.nombre,
            } if producto.categoria_id else None,
            'propietario': {
                'id': producto.propietario_id.id,
                'nombre': producto.propietario_id.name,
                'valoracion': round(producto.propietario_id.valoracion_promedio, 2),
            } if producto.propietario_id else None,
            'etiquetas': [{'id': e.id, 'nombre': e.nombre} for e in producto.etiquetas_ids],
            'total_comentarios': producto.total_comentarios,
            'total_imagenes': producto.total_imagenes,
            'imagen_principal': imagen_principal,
            'fecha_publicacion': producto.fecha_publicacion.isoformat() if producto.fecha_publicacion else None,
        }
        
        if include_all_images:
            result['imagenes'] = imagenes
        
        return result
    
    @staticmethod
    def comentario_to_dict(comentario):
        """Convierte un comentario a diccionario"""
        return {
            'id': comentario.id,
            'id_comentario': comentario.id_comentario,
            'texto': comentario.texto,
            'fecha': comentario.fecha.isoformat() if comentario.fecha else None,
            'editado': comentario.editado,
            'usuario': {
                'id': comentario.usuario_id.id,
                'nombre': comentario.usuario_id.name,
            } if comentario.usuario_id else None,
            'total_reportes': comentario.total_reportes,
        }
    
    @staticmethod
    def mensaje_to_dict(mensaje):
        """Convierte un mensaje a diccionario"""
        return {
            'id': mensaje.id,
            'contenido': mensaje.contenido,
            'fecha_envio': mensaje.fecha_envio.isoformat() if mensaje.fecha_envio else None,
            'leido': mensaje.leido,
            'remitente': {
                'id': mensaje.remitente_id.id,
                'nombre': mensaje.remitente_id.name,
            } if mensaje.remitente_id else None,
            'es_de_comprador': mensaje.es_de_comprador,
            'es_de_vendedor': mensaje.es_de_vendedor,
        }