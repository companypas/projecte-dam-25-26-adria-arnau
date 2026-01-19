from odoo import models, fields, api
import time
import random

class PiUsuario(models.Model):
    _name = 'pi.usuario'
    _description = 'Usuario del Marketplace'
    _inherits = {'res.partner': 'partner_id'}
    _inherit = ['mail.thread', 'mail.activity.mixin']
    
    # Campo requerido para la herencia por delegación
    partner_id = fields.Many2one('res.partner', string='Partner', required=True, ondelete='cascade', auto_join=True)
    
    # Campos básicos específicos del marketplace
    id_usuario = fields.Char(string='ID Usuario', required=True, copy=False, readonly=True, default='Nuevo')
    password = fields.Char(string='Contraseña')
    antiguedad = fields.Integer(string='Antigüedad (días)', compute='_compute_antiguedad', store=True)
    fecha_registro = fields.Date(string='Fecha de Registro', default=fields.Date.today, required=True)
    es_usuario_marketplace = fields.Boolean(string='Es Usuario Marketplace', default=True)
    
    # Relaciones
    valoraciones_recibidas = fields.One2many('pi.valoracion', 'usuario_valorado_id', string='Valoraciones Recibidas')
    valoraciones_realizadas = fields.One2many('pi.valoracion', 'usuario_valorador_id', string='Valoraciones Realizadas')
    productos_venta = fields.One2many('pi.producto', 'propietario_id', string='Productos en Venta', 
                                     domain=[('estado_venta', '=', 'disponible')])
    productos_vendidos = fields.One2many('pi.compra', 'vendedor_id', string='Productos Vendidos')
    productos_comprados = fields.One2many('pi.compra', 'comprador_id', string='Productos Comprados')
    reportes_realizados = fields.One2many('pi.reporte', 'reportado_por_id', string='Reportes Realizados')
    reportes_recibidos = fields.One2many('pi.reporte', 'usuario_reportado_id', string='Reportes Recibidos')
    conversaciones_comprador_ids = fields.One2many(
        'pi.conversacion',
        'comprador_id',
        string='Conversaciones como Comprador',
    )
    conversaciones_vendedor_ids = fields.One2many(
        'pi.conversacion',
        'vendedor_id',
        string='Conversaciones como Vendedor',
    )
    mensajes_enviados_ids = fields.One2many(
        'pi.mensaje',
        'remitente_id',
        string='Mensajes enviados',
    )
    
    # Campos computados para estadísticas
    valoracion_promedio = fields.Float(string='Valoración Promedio', compute='_compute_valoracion_promedio', store=True)
    total_valoraciones = fields.Integer(string='Total Valoraciones', compute='_compute_valoracion_promedio', store=True)
    total_productos_venta = fields.Integer(string='Productos en Venta', compute='_compute_estadisticas', store=True)
    total_productos_vendidos = fields.Integer(string='Productos Vendidos', compute='_compute_estadisticas', store=True)
    total_productos_comprados = fields.Integer(string='Productos Comprados', compute='_compute_estadisticas', store=True)
    
    @api.model
    def create(self, vals):
        # Si no se proporciona un partner_id, crear uno automáticamente
        if not vals.get('partner_id'):
            partner_vals = {
                'name': vals.get('name', 'Nuevo Usuario'),
                'email': vals.get('email', False),
                'phone': vals.get('phone', False),
                'is_company': False,
            }
            partner = self.env['res.partner'].create(partner_vals)
            vals['partner_id'] = partner.id
        
        # Generar ID de usuario si es necesario (verificar si está vacío, es False, o es 'Nuevo')
        id_usuario = vals.get('id_usuario')
        if not id_usuario or id_usuario == 'Nuevo':
            # Intentar obtener ID de la secuencia
            sequence_id = self.env['ir.sequence'].next_by_code('pi.usuario')
            if sequence_id:
                vals['id_usuario'] = sequence_id
            else:
                # Si la secuencia falla, generar un ID único usando timestamp
                timestamp = int(time.time() * 1000) % 100000000
                random_suffix = random.randint(1000, 9999)
                base_id = f'USR-{timestamp:08d}-{random_suffix}'
                
                # Verificar que el ID generado sea único, si no, generar uno nuevo
                max_attempts = 10
                attempt = 0
                while self.search_count([('id_usuario', '=', base_id)]) > 0 and attempt < max_attempts:
                    timestamp = int(time.time() * 1000) % 100000000
                    random_suffix = random.randint(1000, 9999)
                    base_id = f'USR-{timestamp:08d}-{random_suffix}'
                    attempt += 1
                
                vals['id_usuario'] = base_id
        
        return super(PiUsuario, self).create(vals)
    
    @api.depends('fecha_registro')
    def _compute_antiguedad(self):
        for record in self:
            if record.fecha_registro:
                delta = fields.Date.today() - record.fecha_registro
                record.antiguedad = delta.days
            else:
                record.antiguedad = 0
    
    @api.depends('valoraciones_recibidas.valoracion_numerica')
    def _compute_valoracion_promedio(self):
        for record in self:
            valoraciones = record.valoraciones_recibidas
            if valoraciones:
                # Usar valoracion_numerica en lugar de valoracion (que es string)
                total = sum(v.valoracion_numerica for v in valoraciones)
                record.valoracion_promedio = total / len(valoraciones) if len(valoraciones) > 0 else 0.0
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
            'res_model': 'pi.reporte',
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
            'res_model': 'pi.usuario',
            'view_mode': 'form',
            'res_id': self.id,
            'target': 'current',
        }
    
    @api.model
    def _fix_existing_nuevo_ids(self):
        """Método para corregir usuarios existentes con ID 'Nuevo'"""
        usuarios_con_nuevo = self.search([('id_usuario', '=', 'Nuevo')])
        for usuario in usuarios_con_nuevo:
            # Generar un nuevo ID único
            sequence_id = self.env['ir.sequence'].next_by_code('pi.usuario')
            if sequence_id:
                usuario.id_usuario = sequence_id
            else:
                # Si la secuencia falla, generar un ID único usando timestamp
                timestamp = int(time.time() * 1000) % 100000000
                random_suffix = random.randint(1000, 9999)
                base_id = f'USR-{timestamp:08d}-{random_suffix}'
                
                # Verificar que el ID generado sea único
                max_attempts = 10
                attempt = 0
                while self.search_count([('id_usuario', '=', base_id), ('id', '!=', usuario.id)]) > 0 and attempt < max_attempts:
                    timestamp = int(time.time() * 1000) % 100000000
                    random_suffix = random.randint(1000, 9999)
                    base_id = f'USR-{timestamp:08d}-{random_suffix}'
                    attempt += 1
                
                usuario.id_usuario = base_id
    
    _sql_constraints = [
        ('id_usuario_unique', 'unique(id_usuario)', 'El ID de usuario debe ser único.')
    ]