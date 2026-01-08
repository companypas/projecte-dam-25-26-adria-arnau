# -*- coding: utf-8 -*-

# from odoo import models, fields, api


# class pi_core(models.Model):
#     _name = 'pi_core.pi_core'
#     _description = 'pi_core.pi_core'

#     name = fields.Char()
#     value = fields.Integer()
#     value2 = fields.Float(compute="_value_pc", store=True)
#     description = fields.Text()
#
#     @api.depends('value')
#     def _value_pc(self):
#         for record in self:
#             record.value2 = float(record.value) / 100

