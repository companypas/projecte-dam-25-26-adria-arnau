import jwt
import hashlib
from datetime import datetime, timedelta
from functools import wraps
from odoo import http
from odoo.http import request, Response
import json
import base64

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
    """Decorador para requerir JWT en las rutas (token desde body con rotación)"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Obtener token del body - primero intentar kwargs, luego jsonrequest
        token = kwargs.get('token')
        
        # Si no está en kwargs, intentar obtenerlo de request.jsonrequest
        if not token and hasattr(request, 'jsonrequest'):
            token = request.jsonrequest.get('token')
        
        if not token:
            return {
                'error': 'Token no proporcionado en el body',
                'status': 401
            }
        
        payload = JWTAuth.verificar_token(token)
        if 'error' in payload:
            return {
                'error': payload['error'],
                'status': 401
            }
        
        usuario = JWTAuth.obtener_usuario_desde_token(token)
        if not usuario:
            return {
                'error': 'Usuario no encontrado',
                'status': 401
            }
        
        request.usuario_actual = usuario
        
        # Ejecutar la función original
        resultado = f(*args, **kwargs)
        
        # Generar nuevo token
        nuevo_token = JWTAuth.generar_token(usuario.id_usuario, usuario.email)
        
        # Si el resultado es un diccionario, agregar el nuevo token
        if isinstance(resultado, dict):
            resultado['nuevo_token'] = nuevo_token
        
        return resultado
    
    return decorated_function
