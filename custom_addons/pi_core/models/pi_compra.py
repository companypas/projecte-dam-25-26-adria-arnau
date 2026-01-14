from odoo import models, fields, api
from odoo.exceptions import ValidationError

class piCompra(models.Model):
    _name = 'pi.compra'
    _description = 'Compra entre Usuarios'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha desc'
    
    # Campos básicos
    id_compra = fields.Char(string='ID Compra', required=True, copy=False, readonly=True, default='Nuevo')
    fecha = fields.Datetime(string='Fecha de Compra', default=fields.Datetime.now, required=True, readonly=True)
    monto = fields.Monetary(string='Monto', currency_field='currency_id', required=True)
    currency_id = fields.Many2one('res.currency', string='Moneda', default=lambda self: self.env.company.currency_id)
    active = fields.Boolean(string='Activo', default=True)
    
    # Relaciones
    comprador_id = fields.Many2one('pi.usuario', string='Comprador', required=True, ondelete='restrict')
    vendedor_id = fields.Many2one('pi.usuario', string='Vendedor', required=True, ondelete='restrict')
    producto_id = fields.Many2one('pi.producto', string='Producto', required=True, ondelete='restrict')
    
    # Estado de la compra
    estado = fields.Selection([
        ('pendiente', 'Pendiente'),
        ('procesando', 'Procesando'),
        ('confirmada', 'Confirmada'),
        ('valorada_comprador', 'Valorada por Comprador'),
        ('valorada_vendedor', 'Valorada por Vendedor')
    ], string='Estado', default='pendiente', required=True, tracking=True)
    
    # Valoraciones
    valoracion_comprador_id = fields.Many2one('pi.valoracion', string='Valoración del Comprador', readonly=True)
    valoracion_vendedor_id = fields.Many2one('pi.valoracion', string='Valoración del Vendedor', readonly=True)
    
    # Campos computados
    comprador_valorado = fields.Boolean(string='Comprador Valorado', compute='_compute_valoraciones_estado', store=True)
    vendedor_valorado = fields.Boolean(string='Vendedor Valorado', compute='_compute_valoraciones_estado', store=True)
    
    @api.model
    def create(self, vals):
        if vals.get('id_compra', 'Nuevo') == 'Nuevo':
            vals['id_compra'] = self.env['ir.sequence'].next_by_code('pi.compra') or 'CMP-NEW'
        
        # Establecer el monto desde el producto
        if vals.get('producto_id'):
            producto = self.env['pi.producto'].browse(vals['producto_id'])
            vals['monto'] = producto.precio
            vals['vendedor_id'] = producto.propietario_id.id
        
        return super(piCompra, self).create(vals)
    
    @api.depends('valoracion_comprador_id', 'valoracion_vendedor_id')
    def _compute_valoraciones_estado(self):
        for record in self:
            record.comprador_valorado = bool(record.valoracion_vendedor_id)
            record.vendedor_valorado = bool(record.valoracion_comprador_id)
            
            # Actualizar estado si ambos han valorado
            if record.comprador_valorado and record.vendedor_valorado:
                if record.estado != 'valorada_ambos':
                    record.estado = 'valorada_ambos'
            elif record.comprador_valorado:
                if record.estado not in ['valorada_ambos', 'valorada_vendedor']:
                    record.estado = 'valorada_vendedor'
            elif record.vendedor_valorado:
                if record.estado not in ['valorada_ambos', 'valorada_comprador']:
                    record.estado = 'valorada_comprador'
    
    @api.constrains('comprador_id', 'vendedor_id')
    def _check_usuarios_diferentes(self):
        for record in self:
            if record.comprador_id == record.vendedor_id:
                raise ValidationError('El comprador y el vendedor no pueden ser la misma persona.')
    
    def action_procesar_compra(self):
        self.ensure_one()
        if self.estado == 'pendiente':
            self.estado = 'procesando'
            # Marcar producto como reservado
            self.producto_id.estado_venta = 'reservado'
    
    def action_confirmar_compra(self):
        self.ensure_one()
        if self.estado in ['pendiente', 'procesando']:
            self.estado = 'confirmada'
            # Marcar producto como vendido
            self.producto_id.estado_venta = 'vendido'
            
            # Enviar notificaciones para valorar
            self._enviar_notificacion_valoracion()
    
    def action_valorar_vendedor(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Valorar al Vendedor',
            'res_model': 'pi.valoracion',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_compra_id': self.id,
                'default_usuario_valorado_id': self.vendedor_id.id,
                'default_usuario_valorador_id': self.comprador_id.id,
                'default_tipo_valoracion': 'vendedor',
            }
        }
    
    def action_valorar_comprador(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Valorar al Comprador',
            'res_model': 'pi.valoracion',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_compra_id': self.id,
                'default_usuario_valorado_id': self.comprador_id.id,
                'default_usuario_valorador_id': self.vendedor_id.id,
                'default_tipo_valoracion': 'comprador',
            }
        }
    
    def _enviar_notificacion_valoracion(self):
        # Enviar notificación al comprador
        self.message_post(
            body=f'Compra confirmada. Por favor, valora al vendedor {self.vendedor_id.partner_id.name}.',
            partner_ids=[self.comprador_id.partner_id.id]
        )
        
        # Enviar notificación al vendedor
        self.message_post(
            body=f'Compra confirmada. Por favor, valora al comprador {self.comprador_id.partner_id.name}.',
            partner_ids=[self.vendedor_id.partner_id.id]
        )
    
    _sql_constraints = [
        ('id_compra_unique', 'unique(id_compra)', 'El ID de compra debe ser único.')
    ]