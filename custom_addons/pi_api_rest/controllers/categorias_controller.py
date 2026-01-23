from odoo import http
from odoo.http import request
import base64
from .utils import APIUtils
from .auth import jwt_required

class CategoriasController(http.Controller):

    @http.route('/api/categorias', type='json', auth='none', methods=['GET'])
    @jwt_required
    def listar_categorias(self, **kwargs):
        try:
            # sudo() evita chequeos de usuario
            categorias = request.env['pi.categoria'].sudo().search([])
            
            resultado = []
            for cat in categorias:
                imagen_b64 = None
                if cat.imagen:
                    imagen_b64 = base64.b64encode(cat.imagen).decode()
                
                resultado.append({
                    'id': cat.id,
                    'id_categoria': cat.id_categoria,
                    'nombre': cat.nombre,
                    'descripcion': cat.descripcion,
                    'total_productos': cat.total_productos,
                    'imagen': imagen_b64,
                })
            
            return APIUtils.json_response({
                'total': len(resultado),
                'categorias': resultado
            })
        except Exception as e:
            return APIUtils.error_response(str(e), 500)
