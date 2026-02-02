import jwt
from datetime import datetime, timedelta
from functools import wraps
from odoo import http
from odoo.http import request

class JWTAuth:
    """Gestión de autenticación JWT con Bearer tokens"""
    
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
    def obtener_token_desde_header():
        """Extrae el token del header Authorization con formato Bearer"""
        auth_header = request.httprequest.headers.get('Authorization', '')
        
        if not auth_header:
            return None
        
        partes = auth_header.split()
        
        if len(partes) != 2 or partes[0].lower() != 'bearer':
            return None
        
        return partes[1]


def jwt_required(f):
    """Decorador para requerir JWT en las rutas (token desde header Authorizateion: Bearer <token>)"""
    @wraps(f)
    def decorated_function(*args, **kwargs):
        # Obtener token del header Authorization
        token = JWTAuth.obtener_token_desde_header()
        
        if not token:
            return {
                'error': 'Token no proporcionado en header Authorization',
                'status': 401
            }
        
        payload = JWTAuth.verificar_token(token)
        if 'error' in payload:
            return {
                'error': payload['error'],
                'status': 401
            }
        
        # Obtener usuario directamente del payload sin búsqueda en BD
        usuario_id = payload.get('usuario_id')
        usuario_email = payload.get('email')
        
        if not usuario_id:
            return {
                'error': 'Usuario no encontrado en token',
                'status': 401
            }
        
        # Guardar datos del usuario en request sin hacer búsqueda
        request.usuario_actual = {
            'id': usuario_id,
            'email': usuario_email
        }
        
        # Ejecutar la función original
        resultado = f(*args, **kwargs)
        
        # Generar nuevo token
        nuevo_token = JWTAuth.generar_token(usuario_id, usuario_email)
        
        # Si el resultado es un diccionario, agregar el nuevo token
        if isinstance(resultado, dict):
            resultado['nuevo_token'] = nuevo_token
        
        return resultado
    
    return decorated_function