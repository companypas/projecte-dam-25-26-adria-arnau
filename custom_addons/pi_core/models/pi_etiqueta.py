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
        # Normalizar el valor del color
        if 'color' in vals:
            color_value = vals['color']
            # Manejar False, None o valores vacíos
            if color_value is False or color_value is None:
                # Si no se proporciona color, usar el valor por defecto del campo
                vals['color'] = 1
            # Si es string hexadecimal (#RRGGBB o RRGGBB)
            elif isinstance(color_value, str):
                # Si es string vacío, usar valor por defecto
                if not color_value.strip():
                    vals['color'] = 1
                else:
                    try:
                        # Eliminar el # si existe
                        color_hex = color_value.lstrip('#')
                        # Convertir hexadecimal a entero
                        vals['color'] = int(color_hex, 16)
                    except (ValueError, TypeError):
                        # Si falla la conversión, usar un valor por defecto
                        vals['color'] = 1
            # Si es entero, mantenerlo tal cual (incluyendo 0 que es válido)
            elif isinstance(color_value, int):
                # El valor ya es un entero, mantenerlo (0 es válido para el primer color)
                pass  # No hacer nada, mantener el valor
            else:
                # Tipo no reconocido, usar valor por defecto
                vals['color'] = 1
        return super(piEtiqueta, self).create(vals)
    
    def write(self, vals):
        # Normalizar el valor del color
        if 'color' in vals:
            color_value = vals['color']
            # Manejar False, None o valores vacíos
            if color_value is False or color_value is None:
                # Si se intenta establecer False/None, mantener el valor actual o usar por defecto
                if self.color:
                    vals.pop('color', None)  # Mantener el valor actual
                else:
                    vals['color'] = 1  # Usar valor por defecto
            # Si es string hexadecimal (#RRGGBB o RRGGBB)
            elif isinstance(color_value, str):
                # Si es string vacío, mantener valor actual o usar por defecto
                if not color_value.strip():
                    if self.color:
                        vals.pop('color', None)
                    else:
                        vals['color'] = 1
                else:
                    try:
                        # Eliminar el # si existe
                        color_hex = color_value.lstrip('#')
                        # Convertir hexadecimal a entero
                        vals['color'] = int(color_hex, 16)
                    except (ValueError, TypeError):
                        # Si falla la conversión, mantener el valor actual o usar por defecto
                        if self.color:
                            vals.pop('color', None)
                        else:
                            vals['color'] = 1
            # Si es entero, mantenerlo tal cual (incluyendo 0 que es válido)
            elif isinstance(color_value, int):
                # El valor ya es un entero, mantenerlo (0 es válido para el primer color)
                pass  # No hacer nada, mantener el valor
            else:
                # Tipo no reconocido, mantener el valor actual o usar por defecto
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