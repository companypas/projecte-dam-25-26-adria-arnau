# -*- coding: utf-8 -*-
from odoo import http
from odoo.http import request
import jwt
import datetime
import hashlib
import json
import logging

_logger = logging.getLogger(__name__)

SECRET_KEY = "mi_clave_secreta_super_segura_que_deberia_estar_en_config"  # TODO: Mover a configuración

class PiLoginController(http.Controller):

    @http.route('/api/login', type='json', auth='none', methods=['POST'], csrf=False)
    def login(self, **post):
        """
        Endpoint para autenticación de usuarios.
        Recibe un JSON con 'login' (usuario) y 'password'.
        Retorna un token JWT si las credenciales son válidas.
        """
        login = post.get('login')
        password = post.get('password')

        if not login or not password:
            return {"error": "Usuario y contraseña requeridos"}
        
        # Hashear la contraseña recibida para compararla con la almacenada
        hashed_password = hashlib.sha256(password.encode()).hexdigest()

        # Buscar usuario por login (id_usuario) y contraseña hasheada
        # Asumimos que el campo 'id_usuario' es el nombre de usuario
        user = request.env['pi.usuario'].sudo().search([
            ('id_usuario', '=', login),
            ('password', '=', hashed_password)
        ], limit=1)

        if not user:
            # Opción: Intentar también con el usuario del partner asociado si id_usuario no es el login principal
            # O simplemente retornar error
            return {"error": "Credenciales inválidas"}

        # Crear payload del token
        payload = {
            "sub": user.id_usuario, # Usamos el id_usuario público
            "uid": user.id, # ID interno de Odoo
            "name": user.name,
            "iat": datetime.datetime.utcnow(),
            "exp": datetime.datetime.utcnow() + datetime.timedelta(hours=24) # Expiración de 24 horas
        }

        # Generar token
        try:
            token = jwt.encode(payload, SECRET_KEY, algorithm="HS256")
            # En versiones nuevas de PyJWT, encode devuelve str, pero en antiguas bytes.
            # Aseguramos que sea string para JSON
            if isinstance(token, bytes):
                token = token.decode('utf-8')
                
            return {
                "token": token,
                "expires_in": 86400,
                "user_id": user.id_usuario
            }
        except Exception as e:
            _logger.error(f"Error generando token JWT: {str(e)}")
            return {"error": "Error interno al generar token"}

    def verify_token(self, auth_header):
        """
        Método auxiliar para verificar tokens en otros endpoints.
        """
        if not auth_header or not auth_header.startswith("Bearer "):
            raise Exception("Token no proporcionado o formato inválido")

        token = auth_header.split(" ")[1]

        try:
            decoded = jwt.decode(token, SECRET_KEY, algorithms=["HS256"])
            return decoded
        except jwt.ExpiredSignatureError:
            raise Exception("Token expirado")
        except jwt.InvalidTokenError:
            raise Exception("Token inválido")
            
