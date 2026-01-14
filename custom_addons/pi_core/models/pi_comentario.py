from odoo import models, fields, api
from odoo.exceptions import ValidationError

class piComentario(models.Model):
    _name = 'pi.comentario'
    _description = 'Comentarios en Productos'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha desc'
    
    texto = fields.Text(string='Comentario', required=True)
    fecha = fields.Datetime(string='Fecha', default=fields.Datetime.now, required=True, readonly=True)
    editado = fields.Boolean(string='Editado', default=False, readonly=True)
    fecha_edicion = fields.Datetime(string='Fecha de Edición', readonly=True)
    activo = fields.Boolean(string='Activo', default=True)
    
    # Relaciones
    producto_id = fields.Many2one('pi.producto', string='Producto', required=True, ondelete='cascade')
    usuario_id = fields.Many2one('pi.usuario', string='Usuario', required=True, ondelete='cascade')
    reportes_ids = fields.One2many('pi.reporte', 'comentario_reportado_id', string='Reportes')
    
    # Campos computados
    nombre_usuario = fields.Char(string='Usuario', related='usuario_id.name', readonly=True)
    total_reportes = fields.Integer(string='Total Reportes', compute='_compute_total_reportes', store=True)
    
    @api.model
    def create(self, vals):
        if vals.get('id_comentario', 'Nuevo') == 'Nuevo':
            vals['id_comentario'] = self.env['ir.sequence'].next_by_code('pi.comentario') or 'COM-NEW'
        
        result = super(piComentario, self).create(vals)
        
        # Enviar notificación al propietario del producto
        result._enviar_notificacion_propietario()
        
        return result
    
    def write(self, vals):
        if 'texto' in vals and not vals.get('editado'):
            vals['editado'] = True
            vals['fecha_edicion'] = fields.Datetime.now()
        return super(piComentario, self).write(vals)
    
    @api.depends('reportes_ids')
    def _compute_total_reportes(self):
        for record in self:
            record.total_reportes = len(record.reportes_ids.filtered(lambda r: r.estado != 'resuelto'))
    
    def action_editar_comentario(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Editar Comentario',
            'res_model': 'pi.comentario',
            'view_mode': 'form',
            'res_id': self.id,
            'target': 'new',
        }
    
    def action_eliminar_comentario(self):
        self.activo = False
    
    def action_reportar_comentario(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Reportar Comentario',
            'res_model': 'pi.reporte',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_tipo_reporte': 'comentario',
                'default_comentario_reportado_id': self.id,
            }
        }
    
    def _enviar_notificacion_propietario(self):
        # Notificar al propietario del producto que hay un nuevo comentario
        propietario = self.producto_id.propietario_id
        
        if propietario:
            self.message_post(
                body=f'Nuevo comentario en tu producto "{self.producto_id.nombre_producto}" por {self.usuario_id.name}.',
                partner_ids=[propietario.id],
                message_type='notification'
            )
            
            # También se puede enviar email, SMS, etc.
            # self.env['mail.mail'].create({...})
    
    _sql_constraints = [
        ('id_comentario_unique', 'unique(id_comentario)', 'El ID de comentario debe ser único.')
    ]