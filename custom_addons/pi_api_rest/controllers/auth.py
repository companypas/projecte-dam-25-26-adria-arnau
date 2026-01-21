import jwt
import hashlib
from datetime import datetime, timedelta
from functools import wraps
from odoo import http
from odoo.http import request, Response
import json

class JWTAuth:
    """Gestión de autenticación JWT"""
    
    SECRET_KEY = 'tu_clave_secreta_muy_segura_cambiar_en_produccion'
    ALGORITHM = 'HS256'
    TOKEN_EXPIRY = 24  # horas
    
    @staticmethod
    def generar_token(usuario_id, email):
        """Genera un token JWT"""
        payload = {
            'usuario_id': usuario_id,
            'email': email,
            'iat': datetime.utcnow(),
            'exp': datetime.utcnow() + timedelta(hours=JWTAuth.TOKEN_EXPIRY)
        }
        token = jwt.encode(payload, JWTAuth.SECRET_KEY, algorithm=JWTAuth.ALGORITHM)
        return token
    
    @staticmethod
    def verificar_token(token):
        """Verifica y decodifica un token JWT"""
        try:
            payload = jwt.decode(token, JWTAuth.SECRET_KEY, algorithms=[JWTAuth.ALGORITHM])
            return payload
        except jwt.ExpiredSignatureError:
            return {'error': 'Token expirado'}
        except jwt.InvalidTokenError:
            return {'error': 'Token inválido'}
    
    @staticmethod
    def obtener_usuario_desde_token(token):
        """Obtiene el usuario desde el token"""
        payload = JWTAuth.verificar_token(token)
        if 'error' in payload:
            return None
        
        usuario = request.env['pi.usuario'].search([
            ('id_usuario', '=', payload.get('usuario_id'))
        ], limit=1)
        return usuario if usuario else None


def jwt_required(f):
    """Decorador para requerir JWT en las rutas"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        token = None
        
        # Obtener token del header Authorization
        if 'Authorization' in request.headers:
            auth_header = request.headers['Authorization']
            try:
                token = auth_header.split(" ")[1]
            except IndexError:
                return request.make_response(
                    json.dumps({'error': 'Formato de Authorization inválido'}),
                    400,
                    [('Content-Type', 'application/json')]
                )
        
        if not token:
            return request.make_response(
                json.dumps({'error': 'Token no proporcionado'}),
                401,
                [('Content-Type', 'application/json')]
            )
        
        payload = JWTAuth.verificar_token(token)
        if 'error' in payload:
            return request.make_response(
                json.dumps(payload),
                401,
                [('Content-Type', 'application/json')]
            )
        
        request.usuario_actual = JWTAuth.obtener_usuario_desde_token(token)
        if not request.usuario_actual:
            return request.make_response(
                json.dumps({'error': 'Usuario no encontrado'}),
                401,
                [('Content-Type', 'application/json')]
            )
        
        return f(*args, **kwargs)
    
    return decorated_function
