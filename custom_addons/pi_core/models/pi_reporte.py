from odoo import models, fields, api
from odoo.exceptions import ValidationError


class PiReporte(models.Model):
    _name = 'pi.reporte'
    _description = 'Reportes de Usuarios'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha desc'

    motivo = fields.Text(string='Motivo del Reporte', required=True)
    fecha = fields.Datetime(
        string='Fecha del Reporte', 
        default=fields.Datetime.now, 
        required=True, 
        readonly=True
    )
    
    # Estado del reporte
    estado = fields.Selection([
        ('pendiente', 'Pendiente'),
        ('en_revision', 'En Revisión'),
        ('resuelto', 'Resuelto'),
        ('rechazado', 'Rechazado')
    ], string='Estado', default='pendiente', required=True, tracking=True)
    
    # Tipo de reporte
    tipo_reporte = fields.Selection([
        ('producto', 'Reporte de Producto'),
        ('usuario', 'Reporte de Usuario'),
        ('comentario', 'Reporte de Comentario')
    ], string='Tipo de Reporte', required=True)
    
    # Relaciones
    reportado_por_id = fields.Many2one('pi.usuario', string='Reportado Por', required=True, ondelete='cascade')
    producto_reportado_id = fields.Many2one('pi.producto', string='Producto Reportado', ondelete='cascade')
    usuario_reportado_id = fields.Many2one('pi.usuario', string='Usuario Reportado', ondelete='cascade')
    comentario_reportado_id = fields.Many2one('pi.comentario', string='Comentario Reportado', ondelete='cascade')
    
    # Resolución
    fecha_resolucion = fields.Datetime(string='Fecha de Resolución', readonly=True)
    notas_resolucion = fields.Text(string='Notas de Resolución')
    accion_tomada = fields.Selection([
        ('ninguna', 'Ninguna Acción'),
        ('advertencia', 'Advertencia'),
        ('eliminacion_contenido', 'Eliminación de Contenido'),
        ('suspension_temporal', 'Suspensión Temporal'),
        ('suspension_permanente', 'Suspensión Permanente')
    ], string='Acción Tomada')
    
    # Campos computados
    nombre_reportado_por = fields.Char(
        string='Reportado Por', 
        related='reportado_por_id.name', 
        readonly=True
    )
    referencia = fields.Char(string='Referencia', compute='_compute_referencia', store=True)
    display_name = fields.Char(string='Nombre', compute='_compute_display_name', store=True)
    
    @api.depends('tipo_reporte', 'producto_reportado_id', 'usuario_reportado_id', 'comentario_reportado_id')
    def _compute_referencia(self):
        for record in self:
            if record.tipo_reporte == 'producto' and record.producto_reportado_id:
                record.referencia = record.producto_reportado_id.nombre_producto
            elif record.tipo_reporte == 'usuario' and record.usuario_reportado_id:
                record.referencia = record.usuario_reportado_id.name
            elif record.tipo_reporte == 'comentario' and record.comentario_reportado_id:
                texto = record.comentario_reportado_id.texto or ''
                record.referencia = f"Comentario: {texto[:50]}..."
            else:
                record.referencia = 'Sin referencia'
    
    @api.depends('tipo_reporte', 'referencia')
    def _compute_display_name(self):
        for record in self:
            if record.id:
                tipo_label = dict(record._fields['tipo_reporte'].selection).get(record.tipo_reporte, 'Reporte')
                record.display_name = f"{tipo_label} - {record.referencia}"
            else:
                record.display_name = "Nuevo Reporte"
    
    @api.model
    def create(self, vals):
        record = super(PiReporte, self).create(vals)
        
        # Notificar a TODOS los empleados del grupo
        record._enviar_notificacion_empleados()
        
        return record
    
    @api.constrains('tipo_reporte', 'producto_reportado_id', 'usuario_reportado_id', 'comentario_reportado_id')
    def _check_referencia_reporte(self):
        for record in self:
            if record.tipo_reporte == 'producto' and not record.producto_reportado_id:
                raise ValidationError('Debes especificar el producto reportado.')
            if record.tipo_reporte == 'usuario' and not record.usuario_reportado_id:
                raise ValidationError('Debes especificar el usuario reportado.')
            if record.tipo_reporte == 'comentario' and not record.comentario_reportado_id:
                raise ValidationError('Debes especificar el comentario reportado.')
    
    def action_marcar_en_revision(self):
        self.ensure_one()
        self.estado = 'en_revision'
    
    def action_resolver_reporte(self):
        self.ensure_one()
        return {
            'type': 'ir.actions.act_window',
            'name': 'Resolver Reporte',
            'res_model': 'pi.reporte.resolver.wizard',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_reporte_id': self.id,
            }
        }
    
    def action_rechazar_reporte(self):
        self.ensure_one()
        self.estado = 'rechazado'
        self.fecha_resolucion = fields.Datetime.now()
        self.accion_tomada = 'ninguna'
    
    def _confirmar_resolucion(self, accion, notas):
        self.ensure_one()
        self.write({
            'estado': 'resuelto',
            'fecha_resolucion': fields.Datetime.now(),
            'accion_tomada': accion,
            'notas_resolucion': notas
        })
        
        if accion == 'eliminacion_contenido':
            self._eliminar_contenido_reportado()
        elif accion in ['suspension_temporal', 'suspension_permanente']:
            self._suspender_usuario()
    
    def _eliminar_contenido_reportado(self):
        self.ensure_one()
        if self.tipo_reporte == 'producto' and self.producto_reportado_id:
            self.producto_reportado_id.estado_venta = 'vendido'  # Marcar como no disponible
        elif self.tipo_reporte == 'comentario' and self.comentario_reportado_id:
            self.comentario_reportado_id.activo = False
    
    def _suspender_usuario(self):
        self.ensure_one()
        if self.tipo_reporte == 'usuario' and self.usuario_reportado_id:
            # Marcar el partner como inactivo
            self.usuario_reportado_id.active = False
    
    def _enviar_notificacion_empleados(self):
        """Enviar notificación a empleados (solo funciona desde UI de Odoo, no desde API)"""
        try:
            # Notificar al usuario admin
            admin_user = self.env.ref('base.user_admin', raise_if_not_found=False)
            if admin_user and admin_user.partner_id:
                tipo_label = dict(self._fields['tipo_reporte'].selection).get(self.tipo_reporte, 'Reporte')
                self.message_post(
                    body=f'NUEVO REPORTE<br/>'
                         f'<b>Tipo:</b> {tipo_label}<br/>'
                         f'<b>Referencia:</b> {self.referencia}<br/>'
                         f'<b>Motivo:</b> {self.motivo[:100] if self.motivo else ""}...',
                    partner_ids=[admin_user.partner_id.id],
                    message_type='notification',
                    subtype_xmlid='mail.mt_comment',
                )
        except Exception:
            # Silenciosamente ignorar errores cuando se llama desde la API sin usuario autenticado
            pass


# Wizard para resolver reportes
class PiReporteResolverWizard(models.TransientModel):
    _name = 'pi.reporte.resolver.wizard'
    _description = 'Wizard para Resolver Reportes'
    
    reporte_id = fields.Many2one('pi.reporte', string='Reporte', required=True, readonly=True)
    accion_tomada = fields.Selection([
        ('ninguna', 'Ninguna Acción'),
        ('advertencia', 'Advertencia'),
        ('eliminacion_contenido', 'Eliminación de Contenido'),
        ('suspension_temporal', 'Suspensión Temporal'),
        ('suspension_permanente', 'Suspensión Permanente')
    ], string='Acción a Tomar', required=True, default='ninguna')
    notas_resolucion = fields.Text(string='Notas de Resolución', required=True)
    
    def action_confirmar_resolucion(self):
        self.ensure_one()
        if self.reporte_id:
            self.reporte_id._confirmar_resolucion(self.accion_tomada, self.notas_resolucion)
        return {'type': 'ir.actions.act_window_close'}