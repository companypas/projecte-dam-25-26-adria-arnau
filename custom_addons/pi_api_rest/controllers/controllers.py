# -*- coding: utf-8 -*-
# from odoo import http


# class PiApiRest(http.Controller):
#     @http.route('/pi_api_rest/pi_api_rest', auth='public')
#     def index(self, **kw):
#         return "Hello, world"

#     @http.route('/pi_api_rest/pi_api_rest/objects', auth='public')
#     def list(self, **kw):
#         return http.request.render('pi_api_rest.listing', {
#             'root': '/pi_api_rest/pi_api_rest',
#             'objects': http.request.env['pi_api_rest.pi_api_rest'].search([]),
#         })

#     @http.route('/pi_api_rest/pi_api_rest/objects/<model("pi_api_rest.pi_api_rest"):obj>', auth='public')
#     def object(self, obj, **kw):
#         return http.request.render('pi_api_rest.object', {
#             'object': obj
#         })

