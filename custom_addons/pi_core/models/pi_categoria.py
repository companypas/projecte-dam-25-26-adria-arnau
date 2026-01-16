from odoo import models, fields, api

class piCategoria(models.Model):
    _name = 'pi.categoria'
    _description = 'Categoría de Productos'
    _rec_name = 'nombre'
    _order = 'nombre'
    
    id_categoria = fields.Char(string='ID Categoría', required=True, copy=False, readonly=True, default='Nuevo')
    nombre = fields.Char(string='Nombre de la Categoría', required=True)
    descripcion = fields.Text(string='Descripción')
    imagen = fields.Binary(string='Imagen de la Categoría', attachment=True)
    
    # Relaciones
    productos_ids = fields.One2many('pi.producto', 'categoria_id', string='Productos')
    
    # Campos computados
    total_productos = fields.Integer(string='Total de Productos', compute='_compute_total_productos', store=True)
    productos_activos = fields.Integer(string='Productos Activos', compute='_compute_productos_activos', store=True)
    
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