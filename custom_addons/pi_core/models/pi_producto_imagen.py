from odoo import models, fields, api

class piProductoImagen(models.Model):
    _name = 'pi.producto.imagen'
    _description = 'Imágenes del Producto'
    _order = 'sequence, id'
    
    producto_id = fields.Many2one('pi.producto', string='Producto', required=True, ondelete='cascade')
    imagen = fields.Binary(string='Imagen', required=True, attachment=True)
    nombre = fields.Char(string='Nombre de Archivo')
    sequence = fields.Integer(string='Secuencia', default=10)
    
    @api.model
    def create(self, vals):
        # Si no se especifica sequence, asignar el siguiente número disponible
        if 'sequence' not in vals or not vals.get('sequence'):
            if vals.get('producto_id'):
                producto = self.env['pi.producto'].browse(vals['producto_id'])
                max_sequence = max([img.sequence for img in producto.imagenes_ids] or [0])
                vals['sequence'] = max_sequence + 10
        return super(piProductoImagen, self).create(vals)