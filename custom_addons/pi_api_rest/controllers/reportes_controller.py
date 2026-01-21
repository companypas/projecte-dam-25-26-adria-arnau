class ReportesController(http.Controller):
    
    @http.route('/api/reportes', type='json', auth='public', methods=['POST'])
    @jwt_required
    def crear_reporte(self, **kwargs):
        """Crea un reporte de producto, usuario o comentario"""
        try:
            usuario = request.usuario_actual
            
            tipo_reporte = kwargs.get('tipo_reporte')
            motivo = kwargs.get('motivo')
            
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
                producto_id = kwargs.get('producto_id', type=int)
                if not producto_id:
                    return APIUtils.error_response('ID de producto requerido', 400)
                vals['producto_reportado_id'] = producto_id
                
            elif tipo_reporte == 'usuario':
                usuario_id = kwargs.get('usuario_id', type=int)
                if not usuario_id:
                    return APIUtils.error_response('ID de usuario requerido', 400)
                if usuario_id == usuario.id:
                    return APIUtils.error_response('No puedes reportarte a ti mismo', 400)
                vals['usuario_reportado_id'] = usuario_id
                
            elif tipo_reporte == 'comentario':
                comentario_id = kwargs.get('comentario_id', type=int)
                if not comentario_id:
                    return APIUtils.error_response('ID de comentario requerido', 400)
                vals['comentario_reportado_id'] = comentario_id
            
            reporte = request.env['pi.reporte'].create(vals)
            
            return APIUtils.json_response({
                'mensaje': 'Reporte creado exitosamente',
                'reporte_id': reporte.id
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

