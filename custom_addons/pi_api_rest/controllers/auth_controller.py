from odoo import http, fields
from odoo.http import request
import hashlib
import json
from .auth import JWTAuth
from .utils import APIUtils

class AuthController(http.Controller):
    
    @http.route('/api/auth/registro', type='json', auth='public', methods=['POST'], csrf=False)
    def registro(self, **kwargs):
        """Registra un nuevo usuario"""
        try:
            # CORRECCIÓN: acceder a los datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            nombre = data.get('nombre')
            email = data.get('email')
            password = data.get('password')
            telefono = data.get('telefono')
            ubicacion = data.get('ubicacion')
            
            if not all([nombre, email, password]):
                return APIUtils.error_response('Nombre, email y contraseña son requeridos', 400)
            
            # Verificar que el email sea único
            usuario_existente = request.env['pi.usuario'].sudo().search([
                ('email', '=', email)
            ], limit=1)
            if usuario_existente:
                return APIUtils.error_response('El email ya está registrado', 400)
            
            # Crear usuario
            usuario = request.env['pi.usuario'].sudo().create({
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
    
    @http.route('/api/auth/login', type='json', auth='public', methods=['POST'], csrf=False)
    def login(self, **kwargs):
        """Login de usuario"""
        try:
            # CORRECCIÓN: acceder a los datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            email = data.get('email')
            password = data.get('password')
            
            if not email or not password:
                return APIUtils.error_response('Email y contraseña son requeridos', 400)
            
            usuario = request.env['pi.usuario'].sudo().search([
                ('email', '=', email)
            ], limit=1)
            
            if not usuario:
                return APIUtils.error_response('Credenciales inválidas', 401)
            
            # Verificar contraseña (sin hash por ahora)
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
    
    @http.route('/api/auth/refresh', type='json', auth='public', methods=['POST'], csrf=False)
    def refresh_token(self, **kwargs):
        """Refrescar token JWT"""
        try:
            # CORRECCIÓN: acceder a los datos JSON correctamente
            data = kwargs if kwargs else (request.jsonrequest if hasattr(request, 'jsonrequest') else json.loads(request.httprequest.data))
            
            token = data.get('token')
            if not token:
                return APIUtils.error_response('Token no proporcionado', 400)
            
            payload = JWTAuth.verificar_token(token)
            if 'error' in payload:
                return APIUtils.error_response(payload['error'], 401)
            
            usuario = request.env['pi.usuario'].sudo().search([
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