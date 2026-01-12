from odoo import models, fields

class piProductoImagen(models.Model):
    _name = 'pi.producto.imagen'
    _description = 'Imágenes del Producto'
    _order = 'sequence, id'
    
    producto_id = fields.Many2one('pi.producto', string='Producto', required=True, ondelete='cascade')
    imagen = fields.Binary(string='Imagen', required=True, attachment=True)
    nombre = fields.Char(string='Nombre de Archivo')
    sequence = fields.Integer(string='Secuencia', default=10)