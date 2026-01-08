# -*- coding: utf-8 -*-
# from odoo import http


# class PiCore(http.Controller):
#     @http.route('/pi_core/pi_core', auth='public')
#     def index(self, **kw):
#         return "Hello, world"

#     @http.route('/pi_core/pi_core/objects', auth='public')
#     def list(self, **kw):
#         return http.request.render('pi_core.listing', {
#             'root': '/pi_core/pi_core',
#             'objects': http.request.env['pi_core.pi_core'].search([]),
#         })

#     @http.route('/pi_core/pi_core/objects/<model("pi_core.pi_core"):obj>', auth='public')
#     def object(self, obj, **kw):
#         return http.request.render('pi_core.object', {
#             'object': obj
#         })

