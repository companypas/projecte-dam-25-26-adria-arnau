from odoo import models, fields, api
from odoo.exceptions import ValidationError


class PiMensaje(models.Model):
    _name = 'pi.mensaje'
    _description = 'Mensaje de conversación entre usuarios'
    _order = 'fecha_envio asc, id asc'

    conversacion_id = fields.Many2one(
        'pi.conversacion',
        string='Conversación',
        required=True,
        ondelete='cascade',
    )

    remitente_id = fields.Many2one(
        'pi.usuario',
        string='Remitente',
        required=True,
        ondelete='restrict',
    )

    es_de_comprador = fields.Boolean(
        string='Enviado por comprador',
        compute='_compute_flags_remitente',
        store=True,
    )
    es_de_vendedor = fields.Boolean(
        string='Enviado por vendedor',
        compute='_compute_flags_remitente',
        store=True,
    )

    contenido = fields.Text(
        string='Contenido',
        required=True,
    )

    fecha_envio = fields.Datetime(
        string='Fecha de envío',
        default=fields.Datetime.now,
        required=True,
    )

    leido = fields.Boolean(
        string='Leído',
        default=False,
    )

    tipo_contenido = fields.Selection(
        [
            ('texto', 'Texto'),
            ('sistema', 'Mensaje de sistema'),
        ],
        string='Tipo de contenido',
        default='texto',
        required=True,
    )

    @api.depends('remitente_id', 'conversacion_id.comprador_id', 'conversacion_id.vendedor_id')
    def _compute_flags_remitente(self):
        for record in self:
            record.es_de_comprador = (
                bool(record.remitente_id)
                and bool(record.conversacion_id)
                and record.remitente_id == record.conversacion_id.comprador_id
            )
            record.es_de_vendedor = (
                bool(record.remitente_id)
                and bool(record.conversacion_id)
                and record.remitente_id == record.conversacion_id.vendedor_id
            )

    @api.constrains('remitente_id', 'conversacion_id')
    def _check_remitente_en_conversacion(self):
        for record in self:
            if record.conversacion_id and record.remitente_id:
                if record.remitente_id not in (
                    record.conversacion_id.comprador_id,
                    record.conversacion_id.vendedor_id,
                ):
                    raise ValidationError(
                        'El remitente del mensaje debe ser el comprador o el vendedor de la conversación.'
                    )

