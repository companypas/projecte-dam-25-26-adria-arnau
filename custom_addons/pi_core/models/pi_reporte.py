from odoo import models, fields, api
from odoo.exceptions import ValidationError

class piReporte(models.Model):
    _name = 'pi.reporte'
    _description = 'Reportes de Usuarios'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha desc'
    
    motivo = fields.Text(string='Motivo del Reporte', required=True)
    fecha = fields.Datetime(string='Fecha del Reporte', default=fields.Datetime.now, required=True, readonly=True)
    
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
    empleado_asignado_id = fields.Many2one('res.users', string='Empleado Asignado', domain=[('groups_id', 'in', [ref('pi_c2c.group_pi_employee')])], tracking=True)
    
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
    nombre_reportado_por = fields.Char(string='Reportado Por', related='reportado_por_id.partner_id.name', readonly=True)
    referencia = fields.Char(string='Referencia', compute='_compute_referencia')
    
    @api.model
    def create(self, vals):
        if vals.get('id_reporte', 'Nuevo') == 'Nuevo':
            vals['id_reporte'] = self.env['ir.sequence'].next_by_code('pi.reporte') or 'REP-NEW'
        
        result = super(piReporte, self).create(vals)
        
        # Enviar notificación a empleados
        result._enviar_notificacion_empleados()
        
        return result
    
    @api.depends('tipo_reporte', 'producto_reportado_id', 'usuario_reportado_id', 'comentario_reportado_id')
    def _compute_referencia(self):
        for record in self:
            if record.tipo_reporte == 'producto' and record.producto_reportado_id:
                record.referencia = record.producto_reportado_id.nombre_producto
            elif record.tipo_reporte == 'usuario' and record.usuario_reportado_id:
                record.referencia = record.usuario_reportado_id.partner_id.name
            elif record.tipo_reporte == 'comentario' and record.comentario_reportado_id:
                record.referencia = f"Comentario: {record.comentario_reportado_id.texto[:50]}..."
            else:
                record.referencia = 'Sin referencia'
    
    @api.constrains('tipo_reporte', 'producto_reportado_id', 'usuario_reportado_id', 'comentario_reportado_id')
    def _check_referencia_reporte(self):
        for record in self:
            if record.tipo_reporte == 'producto' and not record.producto_reportado_id:
                raise ValidationError('Debes especificar el producto reportado.')
            if record.tipo_reporte == 'usuario' and not record.usuario_reportado_id:
                raise ValidationError('Debes especificar el usuario reportado.')
            if record.tipo_reporte == 'comentario' and not record.comentario_reportado_id:
                raise ValidationError('Debes especificar el comentario reportado.')
    
    def action_asignar_empleado(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Asignar Empleado',
            'res_model': 'pi.reporte',
            'view_mode': 'form',
            'res_id': self.id,
            'target': 'new',
        }
    
    def action_marcar_en_revision(self):
        self.estado = 'en_revision'
    
    def action_resolver_reporte(self):
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
        self.estado = 'rechazado'
        self.fecha_resolucion = fields.Datetime.now()
        self.accion_tomada = 'ninguna'
    
    def _confirmar_resolucion(self, accion, notas):
        self.estado = 'resuelto'
        self.fecha_resolucion = fields.Datetime.now()
        self.accion_tomada = accion
        self.notas_resolucion = notas
        
        # Ejecutar la acción correspondiente
        if accion == 'eliminacion_contenido':
            self._eliminar_contenido_reportado()
        elif accion in ['suspension_temporal', 'suspension_permanente']:
            self._suspender_usuario()
    
    def _eliminar_contenido_reportado(self):
        if self.tipo_reporte == 'producto' and self.producto_reportado_id:
            self.producto_reportado_id.estado_venta = 'eliminado'
        elif self.tipo_reporte == 'comentario' and self.comentario_reportado_id:
            self.comentario_reportado_id.activo = False
    
    def _suspender_usuario(self):
        if self.tipo_reporte == 'usuario' and self.usuario_reportado_id:
            self.usuario_reportado_id.activo = False
    
    def _enviar_notificacion_empleados(self):
        # Notificar a los empleados sobre el nuevo reporte
        empleados = self.env['res.users'].search([
            ('groups_id', 'in', [self.env.ref('pi_c2c.group_pi_employee').id])
        ])
        
        if empleados:
            partner_ids = [emp.partner_id.id for emp in empleados if emp.partner_id]
            self.message_post(
                body=f'Nuevo reporte de {self.tipo_reporte}: {self.referencia}',
                partner_ids=partner_ids,
                message_type='notification'
            )
    
    _sql_constraints = [
        ('id_reporte_unique', 'unique(id_reporte)', 'El ID de reporte debe ser único.')
    ]


# Wizard para resolver reportes
class piReporteResolverWizard(models.TransientModel):
    _name = 'pi.reporte.resolver.wizard'
    _description = 'Wizard para Resolver Reportes'
    
    reporte_id = fields.Many2one('pi.reporte', string='Reporte', required=True)
    accion_tomada = fields.Selection([
        ('ninguna', 'Ninguna Acción'),
        ('advertencia', 'Advertencia'),
        ('eliminacion_contenido', 'Eliminación de Contenido'),
        ('suspension_temporal', 'Suspensión Temporal'),
        ('suspension_permanente', 'Suspensión Permanente')
    ], string='Acción a Tomar', required=True)
    notas_resolucion = fields.Text(string='Notas de Resolución', required=True)
    
    def action_confirmar_resolucion(self):
        self.reporte_id._confirmar_resolucion(self.accion_tomada, self.notas_resolucion)
        return {'type': 'ir.actions.act_window_close'}