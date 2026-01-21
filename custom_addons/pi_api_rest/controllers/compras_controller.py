class ComprasController(http.Controller):
    
    @http.route('/api/compras', type='json', auth='public', methods=['POST'])
    @jwt_required
    def crear_compra(self, **kwargs):
        """Crea una nueva compra"""
        try:
            usuario = request.usuario_actual
            
            producto_id = kwargs.get('producto_id', type=int)
            if not producto_id:
                return APIUtils.error_response('ID de producto requerido', 400)
            
            producto = request.env['pi.producto'].browse(producto_id)
            if not producto.exists():
                return APIUtils.error_response('Producto no encontrado', 404)
            
            if producto.estado_venta != 'disponible':
                return APIUtils.error_response('El producto no está disponible', 400)
            
            if producto.propietario_id.id == usuario.id:
                return APIUtils.error_response('No puedes comprar tu propio producto', 400)
            
            compra = request.env['pi.compra'].create({
                'comprador_id': usuario.id,
                'producto_id': producto_id,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Compra creada exitosamente',
                'compra_id': compra.id,
                'estado': compra.estado
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/compras/<int:compra_id>', type='json', auth='public', methods=['GET'])
    @jwt_required
    def obtener_compra(self, compra_id, **kwargs):
        """Obtiene los detalles de una compra"""
        try:
            usuario = request.usuario_actual
            compra = request.env['pi.compra'].browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.comprador_id.id != usuario.id and compra.vendedor_id.id != usuario.id:
                return APIUtils.error_response('No tienes acceso a esta compra', 403)
            
            return APIUtils.json_response({
                'id': compra.id,
                'id_compra': compra.id_compra,
                'estado': compra.estado,
                'monto': compra.monto,
                'fecha': compra.fecha.isoformat() if compra.fecha else None,
                'comprador': APIUtils.usuario_to_dict(compra.comprador_id),
                'vendedor': APIUtils.usuario_to_dict(compra.vendedor_id),
                'producto': APIUtils.producto_to_dict(compra.producto_id),
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/compras/<int:compra_id>/confirmar', type='json', auth='public', methods=['POST'])
    @jwt_required
    def confirmar_compra(self, compra_id, **kwargs):
        """Confirma una compra"""
        try:
            usuario = request.usuario_actual
            compra = request.env['pi.compra'].browse(compra_id)
            
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.vendedor_id.id != usuario.id:
                return APIUtils.error_response('Solo el vendedor puede confirmar la compra', 403)
            
            compra.action_confirmar_compra()
            
            return APIUtils.json_response({
                'mensaje': 'Compra confirmada exitosamente',
                'estado': compra.estado
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)


# =====================================================
# api/valoraciones_controller.py - Controladores de Valoraciones
# =====================================================

class ValoracionesController(http.Controller):
    
    @http.route('/api/valoraciones', type='json', auth='public', methods=['POST'])
    @jwt_required
    def crear_valoracion(self, **kwargs):
        """Crea una valoración de usuario"""
        try:
            usuario = request.usuario_actual
            
            usuario_valorado_id = kwargs.get('usuario_valorado_id', type=int)
            compra_id = kwargs.get('compra_id', type=int)
            valoracion = kwargs.get('valoracion')
            comentario = kwargs.get('comentario', '')
            tipo_valoracion = kwargs.get('tipo_valoracion')
            
            if not all([usuario_valorado_id, compra_id, valoracion, tipo_valoracion]):
                return APIUtils.error_response('Parámetros requeridos faltantes', 400)
            
            if valoracion not in ['1', '2', '3', '4', '5']:
                return APIUtils.error_response('Valoración debe ser entre 1 y 5', 400)
            
            compra = request.env['pi.compra'].browse(compra_id)
            if not compra.exists():
                return APIUtils.error_response('Compra no encontrada', 404)
            
            if compra.estado != 'confirmada':
                return APIUtils.error_response('La compra debe estar confirmada para valorar', 400)
            
            val = request.env['pi.valoracion'].create({
                'usuario_valorado_id': usuario_valorado_id,
                'usuario_valorador_id': usuario.id,
                'compra_id': compra_id,
                'valoracion': valoracion,
                'comentario': comentario,
                'tipo_valoracion': tipo_valoracion,
            })
            
            return APIUtils.json_response({
                'mensaje': 'Valoración creada exitosamente',
                'valoracion_id': val.id
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
