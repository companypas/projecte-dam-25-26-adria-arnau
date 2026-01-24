from odoo import models, fields, api
from odoo.exceptions import ValidationError
import time
import random

class piProducto(models.Model):
    _name = 'pi.producto'
    _description = 'Producto del pi'
    _inherit = ['mail.thread', 'mail.activity.mixin']
    _order = 'fecha_publicacion desc'
    _rec_name = 'nombre_producto'
    
    # Campos básicos
    id_producto = fields.Char(string='ID Producto', required=True, copy=False, readonly=True, default='Nuevo')
    nombre_producto = fields.Char(string='Nombre del Producto', required=True, tracking=True)
    descripcion = fields.Text(string='Descripción', required=True)
    antiguedad_producto = fields.Integer(string='Antigüedad del Producto (meses)', required=True)
    estado = fields.Selection([
        ('nuevo', 'Nuevo'),
        ('segunda_mano', 'Segunda Mano'),
    ], string='Estado del Producto', required=True, default='nuevo')
    precio = fields.Float(string='Precio (€)', required=True, tracking=True)
    ubicacion = fields.Char(string='Ubicación', required=True)
    fecha_publicacion = fields.Datetime(string='Fecha de Publicación', default=fields.Datetime.now, readonly=True)
    company_currency_id = fields.Many2one('res.currency', string='Moneda', default=lambda self: self.env.company.currency_id)
    
    # Relaciones
    propietario_id = fields.Many2one('pi.usuario', string='Propietario', required=True, ondelete='cascade')
    categoria_id = fields.Many2one('pi.categoria', string='Categoría', required=True, ondelete='restrict')
    etiquetas_ids = fields.Many2many('pi.etiqueta', string='Etiquetas')
    imagenes_ids = fields.One2many('pi.producto.imagen', 'producto_id', string='Imágenes')
    comentarios_ids = fields.One2many('pi.comentario', 'producto_id', string='Comentarios')
    compra_id = fields.One2many('pi.compra', 'producto_id', string='Compra')
    reportes_ids = fields.One2many('pi.reporte', 'producto_reportado_id', string='Reportes')
    
    # Estado de venta
    estado_venta = fields.Selection([
        ('disponible', 'Disponible'),
        ('vendido', 'Vendido'),
    ], string='Estado de Venta', default='disponible', required=True, tracking=True)
    
    # Campos computados
    total_comentarios = fields.Integer(string='Total Comentarios', compute='_compute_total_comentarios')
    total_imagenes = fields.Integer(string='Total Imágenes', compute='_compute_total_imagenes')
    imagen_principal = fields.Binary(string='Imagen Principal', compute='_compute_imagen_principal', store=False)
    
    @api.model
    def create(self, vals):
        if vals.get('id_producto', 'Nuevo') == 'Nuevo':
            # Intentar obtener ID de la secuencia
            sequence_id = self.env['ir.sequence'].next_by_code('pi.producto')
            if sequence_id:
                vals['id_producto'] = sequence_id
            else:
                # Si la secuencia falla, generar un ID único usando timestamp
                timestamp = int(time.time() * 1000) % 100000000
                random_suffix = random.randint(1000, 9999)
                base_id = f'PRD-{timestamp:08d}-{random_suffix}'
                
                # Verificar que el ID generado sea único
                max_attempts = 10
                attempt = 0
                while self.search_count([('id_producto', '=', base_id)]) > 0 and attempt < max_attempts:
                    timestamp = int(time.time() * 1000) % 100000000
                    random_suffix = random.randint(1000, 9999)
                    base_id = f'PRD-{timestamp:08d}-{random_suffix}'
                    attempt += 1
                
                vals['id_producto'] = base_id
        return super(piProducto, self).create(vals)
    
    @api.depends('comentarios_ids')
    def _compute_total_comentarios(self):
        for record in self:
            record.total_comentarios = len(record.comentarios_ids)
    
    @api.depends('imagenes_ids')
    def _compute_total_imagenes(self):
        for record in self:
            record.total_imagenes = len(record.imagenes_ids)
    
    @api.depends('imagenes_ids.imagen', 'imagenes_ids.sequence')
    def _compute_imagen_principal(self):
        for record in self:
            if record.imagenes_ids:
                # Ordenar por sequence y tomar la primera
                imagenes_ordenadas = record.imagenes_ids.sorted('sequence')
                record.imagen_principal = imagenes_ordenadas[0].imagen
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
            # Verificar que haya al menos 1 imagen
            if len(record.imagenes_ids) < 1:
                raise ValidationError('Debes subir al menos 1 imagen.')
            # Verificar que no haya más de 10 imágenes en total
            if len(record.imagenes_ids) > 10:
                raise ValidationError('No puedes subir más de 10 imágenes en total.')
    
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
    
    def action_iniciar_chat(self):
        """Abre o crea un chat entre el usuario actual y el vendedor del producto"""
        self.ensure_one()
        
        # Obtener el usuario actual
        current_user = self.env.user
        vendedor_user = self.propietario_id.user_ids[0] if self.propietario_id.user_ids else False
        
        if not vendedor_user:
            raise ValidationError('El vendedor no tiene un usuario asociado.')
        
        if current_user == vendedor_user:
            raise ValidationError('No puedes iniciar un chat contigo mismo.')
        
        # Buscar si ya existe un canal entre estos usuarios para este producto
        channel_name = f'Producto: {self.nombre_producto}'
        channel = self.env['mail.channel'].search([
            ('name', '=', channel_name),
            ('channel_partner_ids', 'in', [current_user.partner_id.id, vendedor_user.partner_id.id])
        ], limit=1)
        
        # Si no existe, crear uno nuevo
        if not channel:
            channel = self.env['mail.channel'].create({
                'name': channel_name,
                'description': f'Chat sobre el producto: {self.nombre_producto}',
                'channel_type': 'chat',  # 'chat' para conversación privada
                'channel_partner_ids': [(4, current_user.partner_id.id), (4, vendedor_user.partner_id.id)],
            })
            
            # Enviar mensaje inicial
            channel.message_post(
                body=f'{current_user.partner_id.name} está interesado en tu producto: {self.nombre_producto}',
                message_type='notification',
                subtype_xmlid='mail.mt_comment',
            )
        
        # Abrir el chat
        return {
            'type': 'ir.actions.act_window',
            'name': f'Chat - {self.nombre_producto}',
            'res_model': 'mail.channel',
            'res_id': channel.id,
            'view_mode': 'form',
            'target': 'current',
        }
    
    def action_marcar_vendido(self):
        self.estado_venta = 'vendido'
    
    def action_marcar_disponible(self):
        self.estado_venta = 'disponible'
    
    _sql_constraints = [
        ('id_producto_unique', 'unique(id_producto)', 'El ID de producto debe ser único.')
    ]