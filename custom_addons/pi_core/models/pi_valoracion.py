from odoo import models, fields, api
from odoo.exceptions import ValidationError

class piValoracion(models.Model):
    _name = 'pi.valoracion'
    _description = 'Valoración de Usuarios'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha desc'
    
    # Campo ID faltante
    id_valoracion = fields.Char(string='ID Valoración', required=True, copy=False, readonly=True, default='Nuevo')
    
    valoracion = fields.Selection([
        ('1', '1 Estrella'),
        ('2', '2 Estrellas'),
        ('3', '3 Estrellas'),
        ('4', '4 Estrellas'),
        ('5', '5 Estrellas')
    ], string='Valoración', required=True)
    valoracion_numerica = fields.Integer(string='Valoración Numérica', compute='_compute_valoracion_numerica', store=True)
    comentario = fields.Text(string='Comentario')
    fecha = fields.Datetime(string='Fecha', default=fields.Datetime.now, required=True, readonly=True)
    
    # Relaciones
    usuario_valorado_id = fields.Many2one('pi.usuario', string='Usuario Valorado', required=True, ondelete='cascade')
    usuario_valorador_id = fields.Many2one('pi.usuario', string='Usuario Valorador', required=True, ondelete='cascade')
    compra_id = fields.Many2one('pi.compra', string='Compra Relacionada', required=True, ondelete='cascade')
    producto_id = fields.Many2one('pi.producto', string='Producto', related='compra_id.producto_id', store=True, readonly=True)
    
    # Tipo de valoración
    tipo_valoracion = fields.Selection([
        ('comprador', 'Valoración a Comprador'),
        ('vendedor', 'Valoración a Vendedor')
    ], string='Tipo de Valoración', required=True)
    
    @api.model
    def create(self, vals):
        if vals.get('id_valoracion', 'Nuevo') == 'Nuevo':
            vals['id_valoracion'] = self.env['ir.sequence'].next_by_code('pi.valoracion') or 'VAL-NEW'
        
        result = super(piValoracion, self).create(vals)
        
        # Actualizar la referencia de valoración en la compra
        if result.tipo_valoracion == 'vendedor':
            result.compra_id.write({'valoracion_comprador_id': result.id})
        elif result.tipo_valoracion == 'comprador':
            result.compra_id.write({'valoracion_vendedor_id': result.id})
        
        return result
    
    @api.depends('valoracion')
    def _compute_valoracion_numerica(self):
        for record in self:
            record.valoracion_numerica = int(record.valoracion) if record.valoracion else 0
    
    @api.constrains('usuario_valorado_id', 'usuario_valorador_id')
    def _check_usuarios_diferentes(self):
        for record in self:
            if record.usuario_valorado_id == record.usuario_valorador_id:
                raise ValidationError('Un usuario no puede valorarse a sí mismo.')
    
    @api.constrains('usuario_valorador_id', 'compra_id', 'tipo_valoracion')
    def _check_valoracion_unica(self):
        for record in self:
            # Verificar que no exista otra valoración del mismo tipo para la misma compra
            domain = [
                ('compra_id', '=', record.compra_id.id),
                ('tipo_valoracion', '=', record.tipo_valoracion),
                ('id', '!=', record.id)
            ]
            if self.search_count(domain) > 0:
                raise ValidationError('Ya existe una valoración de este tipo para esta compra.')
    
    def action_modificar_valoracion(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Modificar Valoración',
            'res_model': 'pi.valoracion',
            'view_mode': 'form',
            'res_id': self.id,
            'target': 'new',
        }
    
    _sql_constraints = [
        ('id_valoracion_unique', 'unique(id_valoracion)', 'El ID de valoración debe ser único.')
    ]