# from odoo import models, fields, api

# class piEtiqueta(models.Model):
#     _name = 'pi.etiqueta'
#     _description = 'Etiquetas de Productos'
#     _order = 'nombre' 
    
#     nombre = fields.Char(string='Etiqueta', required=True)
#     descripcion = fields.Char(string='Descripción')
#     color = fields.Integer(string='Color')
#     activo = fields.Boolean(string='Activo', default=True)
    
#     # Relaciones
#     productos_ids = fields.Many2many('pi.producto', string='Productos')

#     # Campos computados
#     total_productos = fields.Integer(string='Productos con esta Etiqueta', compute='_compute_total_productos')

#     @api.depends('productos_ids')
#     def _compute_total_productos(self):
#         for record in self:
#             record.total_productos = len(record.productos_ids)
    
#     _sql_constraints = [
#         ('nombre_unique', 'unique(nombre)', 'El nombre de la etiqueta debe ser único.')
#     ]