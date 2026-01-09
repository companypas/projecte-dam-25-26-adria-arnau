from odoo import models, fields, api
from odoo.exceptions import ValidationError

class piProducto(models.Model):
    _name = 'pi.producto'
    _description = 'Producto del pi'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha_publicacion desc'
    
    # Campos básicos
    nombre_producto = fields.Char(string='Nombre del Producto', required=True, tracking=True)
    descripcion = fields.Text(string='Descripción', required=True)
    antiguedad_producto = fields.Integer(string='Antigüedad del Producto (meses)', required=True)
    estado = fields.Selection([
        ('nuevo', 'Nuevo'),
        ('segunda_mano', 'Segunda Mano'),
    ], string='Estado del Producto', required=True, default='nuevo')
    precio = fields.Float(string='Precio (€)', required=True, tracking=True)
    latitude = fields.Float(string='Latitud', digits=(10, 8))
    longitude = fields.Float(string='Longitud', digits=(10, 8))
    fecha_publicacion = fields.Datetime(string='Fecha de Publicación', default=fields.Datetime.now, readonly=True)
    
    # Relaciones
    propietario_id = fields.Many2one('pi.usuario', string='Propietario', required=True, ondelete='cascade')
    categoria_id = fields.Many2one('pi.categoria', string='Categoría', required=True, ondelete='restrict')
    etiquetas_ids = fields.Many2many('pi.etiqueta', string='Etiquetas') #Crear módulo etiqueta
    imagenes_ids = fields.One2many('pi.producto.imagen', 'producto_id', string='Imágenes')
    comentarios_ids = fields.One2many('pi.comentario', 'producto_id', string='Comentarios')
    compra_id = fields.One2many('pi.compra', 'producto_id', string='Compra')
    reportes_ids = fields.One2many('pi.reporte', 'producto_reportado_id', string='Reportes')
    
    # Estado de venta
    estado_venta = fields.Selection([
        ('disponible', 'Disponible'),
        ('vendido', 'Vendido'),
        ('eliminado', 'Eliminado')
    ], string='Estado de Venta', default='disponible', required=True, tracking=True)
    
    # Campos computados
    total_comentarios = fields.Integer(string='Total Comentarios', compute='_compute_total_comentarios')
    total_imagenes = fields.Integer(string='Total Imágenes', compute='_compute_total_imagenes')
    imagen_principal = fields.Binary(string='Imagen Principal', compute='_compute_imagen_principal')
    
    @api.model
    def create(self, vals):
        if vals.get('id_producto', 'Nuevo') == 'Nuevo':
            vals['id_producto'] = self.env['ir.sequence'].next_by_code('pi.producto') or 'PRD-NEW'
        return super(piProducto, self).create(vals)
    
    @api.depends('comentarios_ids')
    def _compute_total_comentarios(self):
        for record in self:
            record.total_comentarios = len(record.comentarios_ids)
    
    @api.depends('imagenes_ids')
    def _compute_total_imagenes(self):
        for record in self:
            record.total_imagenes = len(record.imagenes_ids)
    
    @api.depends('imagenes_ids.imagen')
    def _compute_imagen_principal(self):
        for record in self:
            if record.imagenes_ids:
                record.imagen_principal = record.imagenes_ids[0].imagen
            else:
                record.imagen_principal = False
    
    @api.constrains('etiquetas_ids')
    def _check_etiquetas_limit(self):
        for record in self:
            if len(record.etiquetas_ids) > 5:
                raise ValidationError('No puedes asignar más de 5 etiquetas a un producto.')
    
    @api.constrains('imagenes_ids')
    def _check_imagenes_limit(self):
        for record in self:
            if len(record.imagenes_ids) < 1:
                raise ValidationError('Debes subir al menos 1 imagen.')
            if len(record.imagenes_ids) > 10:
                raise ValidationError('No puedes subir más de 10 imágenes.')
    
    @api.constrains('precio')
    def _check_precio(self):
        for record in self:
            if record.precio <= 0:
                raise ValidationError('El precio debe ser mayor que 0.')
    
    def action_agregar_comentario(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Agregar Comentario',
            'res_model': 'pi.comentario',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_producto_id': self.id,
            }
        }
    
    def action_reportar_producto(self):
        return {
            'type': 'ir.actions.act_window',
            'name': 'Reportar Producto',
            'res_model': 'pi.reporte',
            'view_mode': 'form',
            'target': 'new',
            'context': {
                'default_tipo_reporte': 'producto',
                'default_producto_reportado_id': self.id,
            }
        }
    
    def action_marcar_vendido(self):
        self.estado_venta = 'vendido'
    
    def action_marcar_disponible(self):
        self.estado_venta = 'disponible'
    
    _sql_constraints = [
        ('id_producto_unique', 'unique(id_producto)', 'El ID de producto debe ser único.')
    ]


