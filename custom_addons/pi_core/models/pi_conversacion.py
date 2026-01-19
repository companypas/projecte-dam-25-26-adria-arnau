from odoo import models, fields, api
from odoo.exceptions import ValidationError


class PiConversacion(models.Model):
    _name = 'pi.conversacion'
    _description = 'Conversación de chat entre usuarios'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'last_message_date desc, create_date desc'

    # Identificación
    name = fields.Char(string='Asunto', required=True, tracking=True)
    active = fields.Boolean(string='Activo', default=True)

    # Relación principal de la conversación
    comprador_id = fields.Many2one(
        'pi.usuario',
        string='Comprador',
        required=True,
        ondelete='restrict',
        tracking=True,
    )
    vendedor_id = fields.Many2one(
        'pi.usuario',
        string='Vendedor',
        required=True,
        ondelete='restrict',
        tracking=True,
    )

    producto_id = fields.Many2one(
        'pi.producto',
        string='Producto relacionado',
        ondelete='set null',
    )
    compra_id = fields.Many2one(
        'pi.compra',
        string='Compra relacionada',
        ondelete='set null',
    )

    # Mensajes
    mensaje_ids = fields.One2many(
        'pi.mensaje',
        'conversacion_id',
        string='Mensajes',
    )

    # Estado de la conversación
    state = fields.Selection(
        [
            ('abierta', 'Abierta'),
            ('cerrada', 'Cerrada'),
            ('bloqueada', 'Bloqueada'),
        ],
        string='Estado',
        default='abierta',
        tracking=True,
        required=True,
    )

    last_message_date = fields.Datetime(
        string='Último mensaje',
        compute='_compute_last_message',
        store=True,
    )
    last_message_preview = fields.Text(
        string='Último mensaje (vista previa)',
        compute='_compute_last_message',
        store=True,
    )

    total_mensajes = fields.Integer(
        string='Total mensajes',
        compute='_compute_last_message',
        store=True,
    )

    @api.constrains('comprador_id', 'vendedor_id')
    def _check_usuarios_diferentes(self):
        for record in self:
            if record.comprador_id and record.vendedor_id and record.comprador_id == record.vendedor_id:
                raise ValidationError('El comprador y el vendedor no pueden ser el mismo usuario en una conversación.')

    @api.depends('mensaje_ids', 'mensaje_ids.fecha_envio', 'mensaje_ids.contenido')
    def _compute_last_message(self):
        for record in self:
            if record.mensaje_ids:
                last_msg = record.mensaje_ids.sorted('fecha_envio')[-1]
                record.last_message_date = last_msg.fecha_envio
                record.last_message_preview = (last_msg.contenido or '')[:200]
                record.total_mensajes = len(record.mensaje_ids)
            else:
                record.last_message_date = False
                record.last_message_preview = False
                record.total_mensajes = 0

    def action_cerrar_conversacion(self):
        for record in self:
            record.state = 'cerrada'

    def action_reabrir_conversacion(self):
        for record in self:
            if record.state == 'cerrada':
                record.state = 'abierta'

