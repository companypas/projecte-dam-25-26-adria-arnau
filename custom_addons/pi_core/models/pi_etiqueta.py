from odoo import models, fields, api

class piEtiqueta(models.Model):
    _name = 'pi.etiqueta'
    _description = 'Etiquetas de Productos'
    _order = 'nombre' 
    
    nombre = fields.Char(string='Etiqueta', required=True)
    descripcion = fields.Char(string='Descripción')
    color = fields.Integer(string='Color', default=1)
    activo = fields.Boolean(string='Activo', default=True)
    
    # Relaciones
    productos_ids = fields.Many2many('pi.producto', string='Productos')

    # Campos computados
    total_productos = fields.Integer(string='Productos con esta Etiqueta', compute='_compute_total_productos')

    @api.model
    def create(self, vals):
        # Convertir color hexadecimal a entero si es necesario
        if 'color' in vals and isinstance(vals['color'], str) and vals['color'].startswith('#'):
            try:
                # Convertir #RRGGBB a entero
                color_hex = vals['color'].lstrip('#')
                vals['color'] = int(color_hex, 16)
            except (ValueError, TypeError):
                # Si falla la conversión, usar un valor por defecto
                vals['color'] = 1
        return super(piEtiqueta, self).create(vals)
    
    def write(self, vals):
        # Convertir color hexadecimal a entero si es necesario
        if 'color' in vals and isinstance(vals['color'], str) and vals['color'].startswith('#'):
            try:
                # Convertir #RRGGBB a entero
                color_hex = vals['color'].lstrip('#')
                vals['color'] = int(color_hex, 16)
            except (ValueError, TypeError):
                # Si falla la conversión, mantener el valor actual
                if self.color:
                    vals.pop('color', None)
                else:
                    vals['color'] = 1
        return super(piEtiqueta, self).write(vals)

    @api.depends('productos_ids')
    def _compute_total_productos(self):
        for record in self:
            record.total_productos = len(record.productos_ids)
    
    _sql_constraints = [
        ('nombre_unique', 'unique(nombre)', 'El nombre de la etiqueta debe ser único.')
    ]