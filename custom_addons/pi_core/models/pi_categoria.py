from odoo import models, fields, api

class piCategoria(models.Model):
    _name = 'pi.categoria'
    _description = 'Categoría de Productos'
    _order = 'nombre'
    
    nombre = fields.Char(string='Nombre de la Categoría', required=True)
    descripcion = fields.Text(string='Descripción')
    imagen = fields.Binary(string='Imagen de la Categoría', attachment=True)
    activo = fields.Boolean(string='Activo', default=True)
    
    # Relaciones
    productos_ids = fields.One2many('pi.producto', 'categoria_id', string='Productos')
    
    # Campos computados
    total_productos = fields.Integer(string='Total de Productos', compute='_compute_total_productos')
    productos_activos = fields.Integer(string='Productos Disponibles', compute='_compute_productos_activos')
    
    @api.model
    def create(self, vals):
        if vals.get('id_categoria', 'Nuevo') == 'Nuevo':
            vals['id_categoria'] = self.env['ir.sequence'].next_by_code('pi.categoria') or 'CAT-NEW'
        return super(piCategoria, self).create(vals)
    
    @api.depends('productos_ids')
    def _compute_total_productos(self):
        for record in self:
            record.total_productos = len(record.productos_ids)
    
    @api.depends('productos_ids.estado_venta')
    def _compute_productos_activos(self):
        for record in self:
            record.productos_activos = len(record.productos_ids.filtered(lambda p: p.estado_venta == 'disponible'))
    
    def action_listar_productos(self):
        return {
            'type': 'ir.actions.act_window',
            'name': f'Productos de {self.nombre}',
            'res_model': 'pi.producto',
            'view_mode': 'list,form',
            'domain': [('categoria_id', '=', self.id)],
            'context': {'default_categoria_id': self.id}
        }
    
    _sql_constraints = [
        ('nombre_unique', 'unique(nombre)', 'El nombre de la categoría debe ser único.'),
        ('id_categoria_unique', 'unique(id_categoria)', 'El ID de categoría debe ser único.')
    ]


class piEtiqueta(models.Model):
    _name = 'pi.etiqueta'
    _description = 'Etiquetas de Productos'
    _order = 'nombre'
    
    nombre = fields.Char(string='Etiqueta', required=True)
    descripcion = fields.Char(string='Descripción')
    color = fields.Integer(string='Color')
    activo = fields.Boolean(string='Activo', default=True)
    
    # Relaciones
    productos_ids = fields.Many2many('pi.producto', string='Productos')
    
    # Campos computados
    total_productos = fields.Integer(string='Productos con esta Etiqueta', compute='_compute_total_productos')
    
    @api.depends('productos_ids')
    def _compute_total_productos(self):
        for record in self:
            record.total_productos = len(record.productos_ids)
    
    _sql_constraints = [
        ('nombre_unique', 'unique(nombre)', 'El nombre de la etiqueta debe ser único.')
    ]