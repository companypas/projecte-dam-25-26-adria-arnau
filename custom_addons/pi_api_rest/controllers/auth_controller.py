from odoo import http, fields
from odoo.http import request
import hashlib

class AuthController(http.Controller):
    
    @http.route('/api/auth/registro', type='json', auth='public', methods=['POST'])
    def registro(self, **kwargs):
        """Registra un nuevo usuario"""
        try:
            nombre = kwargs.get('nombre')
            email = kwargs.get('email')
            password = kwargs.get('password')
            telefono = kwargs.get('telefono')
            ubicacion = kwargs.get('ubicacion')
            
            if not all([nombre, email, password]):
                return APIUtils.error_response('Nombre, email y contraseña son requeridos', 400)
            
            # Verificar que el email sea único
            usuario_existente = request.env['pi.usuario'].search([
                ('email', '=', email)
            ], limit=1)
            if usuario_existente:
                return APIUtils.error_response('El email ya está registrado', 400)
            
            # Crear usuario
            usuario = request.env['pi.usuario'].create({
                'name': nombre,
                'email': email,
                'password': password,
                'phone': telefono or '',
                'street': ubicacion or '',
                'es_usuario_marketplace': True,
            })
            
            # Generar token
            token = JWTAuth.generar_token(usuario.id_usuario, email)
            
            return APIUtils.json_response({
                'mensaje': 'Usuario registrado exitosamente',
                'token': token,
                'usuario': APIUtils.usuario_to_dict(usuario)
            }, 201)
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/auth/login', type='json', auth='public', methods=['POST'])
    def login(self, **kwargs):
        """Login de usuario"""
        try:
            email = kwargs.get('email')
            password = kwargs.get('password')
            
            if not email or not password:
                return APIUtils.error_response('Email y contraseña son requeridos', 400)
            
            usuario = request.env['pi.usuario'].search([
                ('email', '=', email)
            ], limit=1)
            
            if not usuario:
                return APIUtils.error_response('Credenciales inválidas', 401)
            
            # Verificar contraseña (hash)
            password_hash = hashlib.sha256(password.encode()).hexdigest()
            if usuario.password != password_hash:
                return APIUtils.error_response('Credenciales inválidas', 401)
            
            # Generar token
            token = JWTAuth.generar_token(usuario.id_usuario, email)
            
            return APIUtils.json_response({
                'mensaje': 'Login exitoso',
                'token': token,
                'usuario': APIUtils.usuario_to_dict(usuario)
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
    
    @http.route('/api/auth/refresh', type='json', auth='public', methods=['POST'])
    def refresh_token(self, **kwargs):
        """Refrescar token JWT"""
        try:
            token = kwargs.get('token')
            if not token:
                return APIUtils.error_response('Token no proporcionado', 400)
            
            payload = JWTAuth.verificar_token(token)
            if 'error' in payload:
                return APIUtils.error_response(payload['error'], 401)
            
            usuario = request.env['pi.usuario'].search([
                ('id_usuario', '=', payload.get('usuario_id'))
            ], limit=1)
            
            if not usuario:
                return APIUtils.error_response('Usuario no encontrado', 401)
            
            nuevo_token = JWTAuth.generar_token(usuario.id_usuario, usuario.email)
            
            return APIUtils.json_response({
                'mensaje': 'Token refrescado',
                'token': nuevo_token
            })
            
        except Exception as e:
            return APIUtils.error_response(str(e), 500)

