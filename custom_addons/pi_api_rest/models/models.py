# -*- coding: utf-8 -*-

# from odoo import models, fields, api


# class pi_api_rest(models.Model):
#     _name = 'pi_api_rest.pi_api_rest'
#     _description = 'pi_api_rest.pi_api_rest'

#     name = fields.Char()
#     value = fields.Integer()
#     value2 = fields.Float(compute="_value_pc", store=True)
#     description = fields.Text()
#
#     @api.depends('value')
#     def _value_pc(self):
#         for record in self:
#             record.value2 = float(record.value) / 100

