from odoo import models, fields, api

class MarketplaceUsuario(models.Model):
    _name = 'marketplace.usuario'
    _description = 'Usuario del Marketplace'
    _inherit = ['res.partner', 'mail.thread', 'mail.activity.mixin']
    
    # Campos básicos específicos del marketplace
    id_usuario = fields.Char(string='ID Usuario', required=True, copy=False, readonly=True, default='Nuevo')
    antiguedad = fields.Integer(string='Antigüedad (días)', compute='_compute_antiguedad', store=True)
    fecha_registro = fields.Date(string='Fecha de Registro', default=fields.Date.today, required=True)
    es_usuario_marketplace = fields.Boolean(string='Es Usuario Marketplace', default=True)
    
    # Relaciones
    valoraciones_recibidas = fields.One2many('marketplace.valoracion', 'usuario_valorado_id', string='Valoraciones Recibidas')
    valoraciones_realizadas = fields.One2many('marketplace.valoracion', 'usuario_valorador_id', string='Valoraciones Realizadas')
    productos_venta = fields.One2many('marketplace.producto', 'propietario_id', string='Productos en Venta', 
                                      domain=[('estado_venta', '=', 'disponible')])
    productos_vendidos = fields.One2many('marketplace.compra', 'vendedor_id', string='Productos Vendidos')
    productos_comprados = fields.One2many('marketplace.compra', 'comprador_id', string='Productos Comprados')
    reportes_realizados = fields.One2many('marketplace.reporte', 'reportado_por_id', string='Reportes Realizados')
    reportes_recibidos = fields.One2many('marketplace.reporte', 'usuario_reportado_id', string='Reportes Recibidos')
    
    # Campos computados para estadísticas
    valoracion_promedio = fields.Float(string='Valoración Promedio', compute='_compute_valoracion_promedio', store=True)
    total_valoraciones = fields.Integer(string='Total Valoraciones', compute='_compute_valoracion_promedio', store=True)
    total_productos_venta = fields.Integer(string='Productos en Venta', compute='_compute_estadisticas')
    total_productos_vendidos = fields.Integer(string='Productos Vendidos', compute='_compute_estadisticas')
    total_productos_comprados = fields.Integer(string='Productos Comprados', compute='_compute_estadisticas')
    
    @api.model
    def create(self, vals):
        if vals.get('id_usuario', 'Nuevo') == 'Nuevo':
            vals['id_usuario'] = self.env['ir.sequence'].next_by_code('marketplace.usuario') or 'USR-NEW'
        return super(MarketplaceUsuario, self).create(vals)
    
    @api.depends('fecha_registro')
    def _compute_antiguedad(self):
        for record in self:
            if record.fecha_registro:
                delta = fields.Date.today() - record.fecha_registro
                record.antiguedad = delta.days
            else:
                record.antiguedad = 0
    
    @api.depends('valoraciones_recibidas.valoracion')
    def _compute_valoracion_promedio(self):
        for record in self:
            valoraciones = record.valoraciones_recibidas
            if valoraciones:
                record.valoracion_promedio = sum(v.valoracion for v in valoraciones) / len(valoraciones)
                record.total_valoraciones = len(valoraciones)
            else:
                record.valoracion_promedio = 0.0
                record.total_valoraciones = 0
    
    @api.depends('productos_venta', 'productos_vendidos', 'productos_comprados')
    def _compute_estadisticas(self):
        for record in self:
            record.total_productos_venta = len(record.productos_venta)
            record.total_productos_vendidos = len(record.productos_vendidos)
            record.total_productos_comprados = len(record.productos_comprados)
    
    def action_reportar_usuario(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Reportar Usuario',
            'res_model': 'marketplace.reporte',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_tipo_reporte': 'usuario',
                'default_usuario_reportado_id': self.id,
            }
        }
    
    def action_ver_perfil_publico(self):
        return {
            'type': 'ir.actions.act_window',
            'name': f'Perfil de {self.name}',
            'res_model': 'marketplace.usuario',
            'view_mode': 'form',
            'res_id': self.id,
            'target': 'current',
        }
    
    _sql_constraints = [
        ('id_usuario_unique', 'unique(id_usuario)', 'El ID de usuario debe ser único.')
    ]